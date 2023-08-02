#ifndef clox_common_h
#define clox_common_h

#include <stdlib.h>
#include <stdint.h>

#ifdef DEBUG_TRACE_EXECUTION
    // print the text with red color  
    #define debug(...) printf("\033[0;31m"); printf(__VA_ARGS__); printf("\033[0m");
#endif

#endif