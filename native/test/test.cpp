#include "../stdafx.h"
#include "../linker.h"

int main(int argc, const char** argv)
{
	std::unordered_set<std::string> targets = { "KEY1", "KEY0" };

	
	MEASURE("total", {
		CreateLinkTree("racines.txt", "root.txt", "link.txt", targets);
	});
	
	

	return EXIT_SUCCESS;
}
