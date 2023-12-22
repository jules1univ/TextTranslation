#include "linker.h"

LinkTable CreateLinkTable(const std::string &defaultRootPath, const std::string &outRootPath, std::unordered_set<std::string> &targets)
{
	LinkTable table = {};

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
			table.keys[items[2]].insert(items[1]);
		}
		else
		{
			table.values.emplace_back(std::make_pair(items[1], items[2]));
		}
	}
	defaultRootFile.close();

	std::ofstream outRootFile(outRootPath);
	if (!outRootFile.good())
		return table;

	outRootFile << table.keys.size() << '\n';
	for (const auto &[key, links] : table.keys)
	{
		outRootFile << key;
		for (const auto &link : links)
		{
			outRootFile << '=' << link;
		}
		outRootFile << '\n';
	}
	for (const auto &[key, value] : table.values)
	{
		outRootFile << key << '=' << value << '\n';
	}
	outRootFile.close();

	return table;
}

LinkTable LoadLinkTable(const std::string &outRootPath)
{
	LinkTable table;

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
				table.keys[items[0]].insert(items[i]);
			}
		}
		else
		{
			std::vector<std::string> items = Split(line, '=');
			table.values.emplace_back(std::make_pair(items[0], items[1]));
		}
	}
	inRootFile.close();

	return table;
}

void CreateLinkTree(const std::string &defaultRootPath, const std::string &outRootPath, const std::string &outLinkPath, std::unordered_set<std::string> &targets)
{

	LinkTable table{};

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
		MEASURE("create_table", {
			table = CreateLinkTable(defaultRootPath, outRootPath, targets);
		});
	}
	else
	{
		MEASURE("load_table", {
			table = LoadLinkTable(outRootPath);
		});
	}

	std::ofstream outLinkFile(outLinkPath);
	if (!outLinkFile.good())
		return;

	MEASURE("link_table", {
		size_t i = 0;
		for (const auto &[key, links] : table.keys)
		{
			MEASURE("group (" << key << ')', {
				for (const auto &link : links)
				{
					std::unordered_set<std::string> nodes = {link};
					GroupLinkNodes(link, nodes, table);

					for (const std::string &node : nodes)
					{
						outLinkFile << node << '=' << key << '\n';
					}
				}
			});
			std::cout << (i / (float)table.keys.size()) * 100.f << "%\n";
			i++;
		}
	});

	outLinkFile.close();

	MEASURE("sort_table", {
		SortFile(outLinkPath);
	});
}

void GroupLinkNodes(const std::string &target, std::unordered_set<std::string> &nodes, LinkTable &table)
{
	for (size_t i = 0; i < table.values.size(); i++)
	{
		if (nodes.find(table.values[i].second) == nodes.end() && table.values[i].first == target)
		{
			std::string next_target = table.values[i].second;
			table.values.erase(table.values.begin() + i);
			i--;

			nodes.insert(next_target);
			GroupLinkNodes(next_target, nodes, table);
		}
		else if (nodes.find(table.values[i].first) == nodes.end() && table.values[i].second == target)
		{

			std::string next_target = table.values[i].first;
			table.values.erase(table.values.begin() + i);
			i--;

			nodes.insert(next_target);
			GroupLinkNodes(next_target, nodes, table);
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