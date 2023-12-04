#pragma once
#include "stdafx.h"


#ifdef _DEBUG
#define MEASURE(name, x)                                                                 \
    {                                                                                    \
        LARGE_INTEGER start, end, frequency;                                             \
        long long elapsed_time;                                                          \
        QueryPerformanceFrequency(&frequency);                                           \
        QueryPerformanceCounter(&start);                                                 \
        x;                                                                               \
        QueryPerformanceCounter(&end);                                                   \
        elapsed_time = (end.QuadPart - start.QuadPart) * 100000000 / frequency.QuadPart; \
        std::cout << '[' << name << "]: " << (elapsed_time / 100000.f) << " ms\n";       \
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

void CreateLinkTree(const std::string& defaultRootPath, const std::string& outRootPath, const std::string& outLinkPath, std::unordered_set<std::string>& targets);
void GroupLinkNodes(const std::string& target, std::unordered_set<std::string>& nodes, LinkTable& table);

std::vector<std::string> Split(const std::string &s, char delim);
void SortFile(const std::string& path);