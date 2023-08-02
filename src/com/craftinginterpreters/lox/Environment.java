package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {



    static class EnvironmentStackTooMuchError extends RuntimeException {}

    public static int innerEnvCount = 0;
    final Environment enclosing;

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
        innerEnvCount++;
    }

    // the global scope
    public Environment() {
        this.enclosing = null;
    }

    private final Map<String, Object> values = new HashMap<>();

    public Object getAt(Integer distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(Integer distance) {
        Environment env = this;

        if ( distance > 0 ) {
            for (int i = 0 ; i < distance; i++ ) {
                env = env.enclosing;
            }
        }

        return env;
    }

    public void debug() {
        for (String a : values.keySet()) {
            System.out.println(a);
        }
    }

    public void debug(int depth) {
        for (String a : ancestor(depth).values.keySet()) {
            System.out.println(a);
        }
    }

    public void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme , value);
    }


    void define(String name , Object value) {
        values.put(name , value);
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    Object getFromGlobal(Token name) {

        if (enclosing != null) return enclosing.get(name);

        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value) {
        if ( values.containsKey( name.lexeme )) {
            values.put( name.lexeme , value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name , "Undefined variable '" + name.lexeme + "'.");
    }
}
