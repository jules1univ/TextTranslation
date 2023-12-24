#include "linker.h"

static std::mutex _bufferMutex;

std::unique_ptr<LinkTable> CreateLinkTable(const std::string &defaultRootPath, const std::string &outRootPath, std::unordered_set<std::string> &targets)
{
	auto table = std::make_unique<LinkTable>();

	std::ifstream defaultRootFile(defaultRootPath);
	if (!defaultRootFile.good())
		return table;

	std::string line;
	while (std::getline(defaultRootFile, line))
	{
		if (line.size() <= MIN_LINE_CONTENT || line[0] == '%')
		{
			continue;
		}

		std::vector<std::string> items = Split(line, '\t');
		if (items.size() < MIN_ROOT_ITEMS)
		{
			continue;
		}

		if (items[1].compare(items[2]) == 0)
		{
			continue;
		}

		if (targets.find(items[2]) != targets.end())
		{
			table->keys[items[2]].insert(items[1]);
		}
		else
		{
			table->values.insert(items[1], items[2]);
		}
	}
	defaultRootFile.close();

	std::ofstream outRootFile(outRootPath);
	if (!outRootFile.good())
		return table;

	outRootFile << table->keys.size() << '\n';
	for (const auto &[key, links] : table->keys)
	{
		outRootFile << key;
		for (const auto &link : links)
		{
			outRootFile << '=' << link;
		}
		outRootFile << '\n';
	}
	for (const auto& [left, right] : table->values.get_map())
	{

		outRootFile << left << '=' << *right << '\n';
	}
	outRootFile.close();

	return table;
}

std::unique_ptr<LinkTable> LoadLinkTable(const std::string &outRootPath)
{
	auto table = std::make_unique<LinkTable>();

	std::ifstream inRootFile(outRootPath);
	if (!inRootFile.good())
		return table;

	std::string line;
	int lines = -1;
	while (std::getline(inRootFile, line))
	{
		if (lines == -1)
		{
			lines = std::atoi(line.c_str());
		}
		else if (lines > 0)
		{
			lines--;
			std::vector<std::string> items = Split(line, '=');
			for (size_t i = 1; i < items.size(); i++)
			{
				table->keys[items[0]].insert(items[i]);
			}
		}
		else
		{
			std::vector<std::string> items = Split(line, '=');
			table->values.insert(items[0], items[1]);
		}
	}
	inRootFile.close();

	return table;
}

void CreateLinkTree(const std::string& defaultRootPath, const std::string& outRootPath, const std::string& outLinkPath, std::unordered_set<std::string>& targets)
{
	std::unique_ptr<LinkTable> table;

#if defined(_WIN32)
	DWORD attr = GetFileAttributesA(outRootPath.c_str());
	if (!(attr != INVALID_FILE_ATTRIBUTES && !(attr & FILE_ATTRIBUTE_DIRECTORY)))
#elif defined(__linux__)
	struct stat buffer;
	if (stat(outRootPath.c_str(), &buffer) != 0)
#else
	if (!file_exists(outRootPath))
#endif
	{
		table = CreateLinkTable(defaultRootPath, outRootPath, targets);
	}
	else
	{
		table = LoadLinkTable(outRootPath);
	}


	std::ostringstream buffer;

	std::vector<std::string> keys;
	keys.reserve(table->keys.size());

	std::transform(table->keys.begin(), table->keys.end(), std::back_inserter(keys), [](const auto& pair) { return pair.first; });

	for (auto it = table->keys.begin(); it != table->keys.end(); it++)
	{

		std::unordered_set<std::string> nodes;
		for (const auto& link : it->second)
		{
			nodes.insert(link);
			GroupLinkNodes(link, nodes, table);
		}

		for (const std::string& node : nodes)
		{
			buffer << node << '=' << it->first << '\n';
		}
	}

	
	size_t size = std::thread::hardware_concurrency();
	std::vector<std::thread> threads(size);
	size_t part = (table->keys.size()/size);

	for (size_t i = 0; i < size - 1; i++)
	{
		threads.emplace_back([&, i]() {
			CreateGroupLinkRange((part * i), (part * (i+1)), keys, table, buffer);
		});
	}

	threads.emplace_back([&]() {
		CreateGroupLinkRange(table->keys.size() - part, table->keys.size(), keys,  table, buffer);
	});

	
	for (auto& thread : threads)
	{
		if (thread.joinable())
		{
			thread.join();
		}
	}
	
	

	std::ofstream outLinkFile(outLinkPath);
	if (!outLinkFile.good())
		return;

	outLinkFile << buffer.str();
	outLinkFile.close();

	SortFile(outLinkPath);
}

void CreateGroupLinkRange(size_t start, size_t end, std::vector<std::string>& keys, std::unique_ptr<LinkTable>& table, std::ostringstream& buffer)
{
	for (size_t i = start; i < end; i++)
	{
		std::unordered_set<std::string> nodes;
		for (const auto& link : table->keys[keys[i]])
		{
			nodes.insert(link);
			GroupLinkNodes(link, nodes, table);
		}

		_bufferMutex.lock();
		for (const std::string& node : nodes)
		{
			buffer << node << '=' << keys[i] << '\n';
		}
		_bufferMutex.unlock();
	}
}



void GroupLinkNodes(const std::string &target, std::unordered_set<std::string> &nodes, std::unique_ptr<LinkTable>& table)
{
	if (table->values.left_contains(target))
	{
		const auto& nextTarget = table->values.get_left(target);
		if (!nodes.contains(nextTarget))
		{
			nodes.insert(nextTarget);
			GroupLinkNodes(nextTarget, nodes, table);
		}

	}else if (table->values.right_contains(target))
	{
		const auto& nextTarget = table->values.get_right(target);
		if (!nodes.contains(nextTarget))
		{
			nodes.insert(nextTarget);
			GroupLinkNodes(nextTarget, nodes, table);
		}
	}
}

std::vector<std::string> Split(const std::string &s, char delim)
{
	std::vector<std::string> items;
	items.reserve(std::count(s.begin(), s.end(), delim) + 1);

	std::istringstream ss(s);
	for (std::string item; std::getline(ss, item, delim); items.push_back(item));

	return items;
}

void SortFile(const std::string& path)
{
	std::ifstream infile(path);
	if (!infile.good())
		return;

	std::vector<std::string> lines;
	for (std::string line; std::getline(infile, line); lines.push_back(line));

	infile.close();

	std::sort(lines.begin(), lines.end());

	std::ofstream outfile(path);
	
	if (!outfile.good())
		return;

	for (const std::string& line : lines)
	{
		outfile << line << '\n';
	}

	outfile.close();
}