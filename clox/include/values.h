#ifndef clox_value_h
#define clox_value_h

#include <vector>

#include "../common.h"


typedef double Value;

struct ConstPool {
    public:
        std::vector<Value> values;
        int add(Value value);
};

#endif