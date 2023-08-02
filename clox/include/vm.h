#ifndef clox_vm_h
#define clox_vm_h

#include "../common.h"
#include "chunk.h"


struct VM {

  typedef std::vector<Value> Stack; // stack doesn't support .data() method

  Chunk* chunk;
  uint8_t* ip;
  Stack stack;
  Value* stack_top;


  public:
      enum InterpretResult {
          INTERPRET_OK,
          INTERPRET_COMPILE_ERROR,
          INTERPRET_RUNTIME_ERROR
      };
  
  public:
    void init();
    void free();

    InterpretResult interpret(Chunk *source);
    static InterpretResult run();
};


#endif