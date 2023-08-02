package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    final LoxClass superclass;

    final Map<String , LoxFunction> funcs;


    public LoxClass(String name,  LoxClass superclass , Map<String, LoxFunction> funcs) {
        this.name = name;
        this.funcs = funcs;
        this.superclass = superclass;
    }

    @Override
    public String toString() {
        return "<class " + name + ">";
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        LoxInstance instance = new LoxInstance(this , interpreter.globals);
        LoxFunction initializer = funcs.get("init");
        if (initializer == null) {
            initializer = superclass.funcs.get("init");
        }

        if ( initializer != null ) {
            initializer.bind(instance).call(interpreter, args);
        }


        return instance;
    }

    @Override
    public int arity() {
        LoxFunction initializer = funcs.get("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }
}
