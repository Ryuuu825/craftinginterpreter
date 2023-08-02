#include "common.h"
#include "vm.h"


VM vm;

int main(int argc , const char* argv[])
{
    Chunk chunk;
    chunk.write(OP_CONST_LONG, 1);
    chunk.add_const(Value(1.2), 1);
    chunk.write(OP_RETURN, 1);

    vm.init();

    vm.interpret(&chunk);

    vm.free();
}