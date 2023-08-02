#include "vm.h"
#include <iostream>

#define READ_CONSTANT() (vm.chunk->constants.values[READ_LONG()])

extern VM vm;

void VM::init() {
    // reset stack
    stack.clear();
    vm.stack_top = nullptr;

}
void VM::free() {

}

VM::InterpretResult VM::interpret(Chunk* chunk) {
    vm.chunk = chunk;
    vm.ip = vm.chunk->code.data();
    return run();
}

VM::InterpretResult VM::run() {

#define READ_BYTE() (*vm.ip++)
#define READ_LONG() (*vm.ip++ << 16 | *vm.ip++ << 8 | *vm.ip++)

  for (;;) {

    #ifdef DEBUG_TRACE_EXECUTION
      debug("          ");
        for(Value value : vm.stack) {
            debug("[ ");
            debug("%g", value);
            debug(" ]");
        }
        debug("\n");
        vm.chunk->disassemble((int) ( vm.ip - vm.chunk->code.data() ) );
    #endif

    uint8_t instruction;
    switch (instruction = READ_BYTE()) {
      case OP_RETURN: {
        printf("%g\n", vm.stack.back() );
        vm.stack.pop_back();
        return INTERPRET_OK;
      }
      case OP_CONST_LONG: {
        Value constant = READ_CONSTANT();
        vm.stack.push_back(constant);
        break;
      }
    }
  }

#undef READ_BYTE
#undef READ_LONG
}