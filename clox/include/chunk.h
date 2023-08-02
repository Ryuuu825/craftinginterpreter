#ifndef clox_chunk_h
#define clox_chunk_h

#include <vector>

#include "../common.h"
#include "values.h"



enum OpCode {
    OP_CONST,
    OP_RETURN,
    OP_CONST_LONG,
} ;

struct Chunk {  

    std::vector<uint8_t> code;
    std::vector<int> lines;
    ConstPool constants;

    public: 
        
        void write(uint8_t byte, int line);
        void free();

        void disassemble(const char* name);
        int disassemble(int offset);

        int add_const(const Value& value, int line);

        static int simpleInstruction(const char* name, int offset);
        static int constantInstruction(const char* name, const Chunk* chunk, int offset);
        static int constantLongInstruction(const char* name, const Chunk* chunk, int offset);
} ;



#endif