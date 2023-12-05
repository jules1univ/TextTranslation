#pragma once
#include "stdafx.h"

#if defined(_DEBUG)
#define MEASURE(name, x)                                                                                \
{                                                                                                       \
        auto start = std::chrono::high_resolution_clock::now();                                         \
        x;                                                                                              \
        auto end = std::chrono::high_resolution_clock::now();                                           \
        double duration = std::chrono::duration_cast<std::chrono::microseconds>(end - start).count();     \
        std::cout << '[' << name << "]: " << (duration / 1000.0) << " ms\n";                            \
}

#else
#define MEASURE(name, x) x;
#endif // _DEBUG

typedef struct LinkTable
{
    std::unordered_map<std::string, std::unordered_set<std::string>> keys;
    std::vector<std::pair<std::string, std::string>> values;
} LinkTable;

LinkTable CreateLinkTable(const std::string &defaultRootPath, const std::string &outRootPath, std::unordered_set<std::string> &targets);
LinkTable LoadLinkTable(const std::string &outRootPath);

void CreateLinkTree(const std::string &defaultRootPath, const std::string &outRootPath, const std::string &outLinkPath, std::unordered_set<std::string> &targets);
void GroupLinkNodes(const std::string &target, std::unordered_set<std::string> &nodes, LinkTable &table);

std::vector<std::string> Split(const std::string &s, char delim);
void SortFile(const std::string &path);