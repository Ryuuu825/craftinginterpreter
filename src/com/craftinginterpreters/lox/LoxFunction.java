package com.craftinginterpreters.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {

    final Stmt.Function declaration;
    private final Environment enclosing;


    private final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment enclosing, boolean isInitializer) {

        this.isInitializer = isInitializer;
        this.declaration = declaration;
        this.enclosing = enclosing;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        try {

            Environment environment = new Environment(enclosing);

            for (int i = 0; i < declaration.params.size(); i++) {
                environment.define(declaration.params.get(i).lexeme,
                        args.get(i));
            }

            interpreter.executeBlock(declaration.body , environment);

        } catch (Environment.EnvironmentStackTooMuchError e) {
             Lox.error(declaration.name,"Too many environment are created, you may stuck in infinity loops.");
        } catch (Interpreter.Return returnValue ) {
            return returnValue.value;
        }

        if (isInitializer) {
            return enclosing.getAt(0,"this");
        }

        return null;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(enclosing);
        environment.define("this", instance);
        return new LoxFunction( declaration , environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    public String toString() {
        return "<fun " + declaration.name.lexeme + ">";
    }

    public String funName() {
        return declaration.name.lexeme;
    }

}
