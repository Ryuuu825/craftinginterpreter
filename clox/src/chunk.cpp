#include <iostream>

#include "chunk.h"

void Chunk::write(uint8_t byte, int line) {
    code.push_back(byte);
    lines.push_back(line);
}

void Chunk::free() {
    code.clear();
}

int Chunk::add_const(const Value& value, int line) {
    int index = constants.add(value);
    // store the info as a 3 byte number
    // 3 bytes for the index
    uint8_t byte1 = (index >> 16) & 0xff;
    uint8_t byte2 = (index >> 8) & 0xff;
    uint8_t byte3 = index & 0xff;
    code.push_back(byte1);
    code.push_back(byte2);
    code.push_back(byte3);

    lines.push_back(line);


    return index;
}


void Chunk::disassemble(const char* name) {
    printf("== %s ==\n", name);
    
    for (int offset = 0; offset < code.size() ;) {
        offset = disassemble(offset);
    }
}

int Chunk::disassemble(int offset) {
    printf( "%04d ", offset);

    if ( offset > 0 && lines[offset] == lines[offset - 1]) {
        printf("   | ");
    } else {
        printf("%4d ", lines[offset]);
    }

    uint8_t instruction = code[offset];
    switch (instruction) {
        case OP_RETURN:
            return simpleInstruction("OP_RETURN", offset);
        case OP_CONST:
            return constantInstruction("OP_CONST", this , offset);
        case OP_CONST_LONG:
            return constantLongInstruction("OP_CONST_LONG", this , offset);
        default:
             printf("Unknown opcode %d\n", instruction);
        
        return offset + 1;
    }
}

int Chunk::simpleInstruction(const char* name, int offset) {
  printf("%s\n", name);
  return offset + 1;
}

int Chunk::constantInstruction(const char* name, const Chunk* chunk, int offset) {
  uint8_t constant = chunk->code.at(offset++);
  printf("%-16s %4d '", name, constant);
  printf("%g", (*chunk).constants.values.at(constant) );
  printf("'\n");
  return offset + 1;

}

int Chunk::constantLongInstruction(const char* name, const Chunk* chunk, int offset) {
    uint32_t constant = (chunk->code.at(offset + 1) << 16) | (chunk->code.at(offset + 2) << 8) | chunk->code.at(offset + 3);
    printf("%-16s %4d '", name, constant);
    printf("%g", (*chunk).constants.values.at(constant) );
    printf("'\n");
    return offset + 4;

}

