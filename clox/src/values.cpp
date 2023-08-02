#include "values.h"

int ConstPool::add(Value value) {
    values.push_back(value);
    return values.size() - 1;
}