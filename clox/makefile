files:=./src/*.cpp main.cpp
cc:=clang++ -std=c++11 -I ./include -g
flags:=-Wall -Wextra -Werror -Wno-unused-parameter -Wno-unused-variable -Wno-unused-function -Wno-unused-private-field -Wno-
debug:= -DDEBUG_TRACE_EXECUTION

run: build
	./clox

debug: $(files) clean
	$(cc) $(files) $(debug) -o clox
	./clox

clean: 
	rm -f clox
	rm -f *.o

build: $(files) clean
	$(cc) $(files) -o clox 

