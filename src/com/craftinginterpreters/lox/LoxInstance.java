package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private LoxClass klass;
    Environment env;
    private final Map<String, Object> fields = new HashMap<>();
    private final LoxInstance superClass = null;
    public LoxInstance(LoxClass loxClass, Environment env) {
        this.klass = loxClass;
        this.env = new Environment(env);
        this.env.define("this" , this);
    }

    public LoxInstance(LoxClass loxClass, LoxClass superClass ,Environment env) {
        this.klass = loxClass;
        this.env = new Environment(env);
        this.env.define("this" , this);
    }

    Object get(Token name) {
        if ( superClass != null) {
            superClass.get(name);
        }
        if (fields.containsKey(name.lexeme)) {
            Object values = fields.get(name.lexeme);
            try {
                ((LoxFunction) values).bind(this);
            } catch (ClassCastException ignored) {}
            return values;
        }


        throw new RuntimeError(name , "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }
    void set(String name, Object value) {
        fields.put(name, value);
    }

    public String toString() {
        return "<" + klass.name + " instance>";
    }


}
