#include "../stdafx.h"
#include "../linker.h"

int main(int argc, const char** argv)
{
	const std::string inTargetsPath = "data/default.txt";
	const std::string inRootsPath = "data/racines.txt";

	const std::string outRootPath = "data/root.txt";
	const std::string outLinkPath = "data/link.txt";
    
	std::ifstream targetFile(inTargetsPath);
	if (!targetFile.good())
		return EXIT_FAILURE;

	std::unordered_set<std::string> targets;

	std::string line;
	size_t index = 0;
	while (std::getline(targetFile, line))
	{
		if (index % 2 == 0)
		{
			targets.insert(line);
		}
		index++;
	}

	auto start = std::chrono::high_resolution_clock::now();
	
	CreateLinkTree(inRootsPath, outRootPath, outLinkPath, targets);
	
	auto end = std::chrono::high_resolution_clock::now();
	double duration = std::chrono::duration_cast<std::chrono::microseconds>(end - start).count();
	std::cout << (duration / 1000.0) << " ms\n";

	return EXIT_SUCCESS;
}
