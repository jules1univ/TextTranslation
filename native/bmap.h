#pragma once
#include "stdafx.h"

class BiStrMap {
public:
    void insert(const std::string& left, const std::string& right)
    {
        auto shared_string = std::make_shared<std::string>(right);

        {
            std::unique_lock<std::shared_mutex> lock(mutex_);
            this->left[left] = shared_string;
            this->right[right] = shared_string;
        }
    }

    std::string get_left(const std::string& key) const
    {
        std::shared_lock<std::shared_mutex> lock(mutex_);
        return *(this->left.at(key));
    }

    std::string get_right(const std::string& key) const
    {
        std::shared_lock<std::shared_mutex> lock(mutex_);
        return *(this->right.at(key));
    }

    bool right_contains(const std::string& key) const
    {
        std::shared_lock<std::shared_mutex> lock(mutex_);
        return this->right.find(key) != this->right.end();
    }

    bool left_contains(const std::string& key) const
    {
        std::shared_lock<std::shared_mutex> lock(mutex_);
        return this->left.find(key) != this->left.end();
    }

    std::unordered_map<std::string, std::shared_ptr<std::string>>& get_map()
    {
        return this->left;
    }

private:
    mutable std::shared_mutex mutex_;

    std::unordered_map<std::string, std::shared_ptr<std::string>> left;
    std::unordered_map<std::string, std::shared_ptr<std::string>> right;
};
