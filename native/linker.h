#pragma once
#include "stdafx.h"
#include "bmap.h"

#define MIN_LINE_CONTENT 5
#define MIN_ROOT_ITEMS 3
#define LINK_AVERAGE_DEPTH 3


typedef struct LinkTable
{
    std::unordered_map<std::string, std::unordered_set<std::string>> keys;
    BiStrMap values;
} LinkTable;

std::unique_ptr<LinkTable> CreateLinkTable(const std::string &defaultRootPath, const std::string &outRootPath, std::unordered_set<std::string> &targets);
std::unique_ptr<LinkTable> LoadLinkTable(const std::string &outRootPath);

void CreateGroupLinkRange(size_t start, size_t end, std::vector<std::string>& keys, std::unique_ptr<LinkTable>& table, std::ostringstream& buffer);
void CreateLinkTree(const std::string &defaultRootPath, const std::string &outRootPath, const std::string &outLinkPath, std::unordered_set<std::string> &targets);
void GroupLinkNodes(const std::string &target, std::unordered_set<std::string> &nodes, std::unique_ptr<LinkTable>& table);

std::vector<std::string> Split(const std::string &s, char delim);
void SortFile(const std::string &path);