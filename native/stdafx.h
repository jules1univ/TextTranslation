#pragma once
#include <iostream>

#include <fstream>
#include <sstream>

#include <thread>
#include <mutex>
#include <shared_mutex>

#include <string>
#include <chrono>

#include <iterator>
#include <optional>
#include <execution>
#include <algorithm>

#include <vector>
#include <set>
#include <unordered_set>
#include <unordered_map>

#if defined(_WIN32)
#include <windows.h>
#elif defined(__linux__)
#include <sys/stat.h>
#else
inline bool file_exists(const std::string& path)
{
    std::ifstream file(path);
    bool result = file.good();
    file.close();
    return result;
}
#endif

#include <omp.h>