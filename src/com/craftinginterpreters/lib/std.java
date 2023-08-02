package com.craftinginterpreters.lib;

import com.craftinginterpreters.lox.Interpreter;
import com.craftinginterpreters.lox.LoxCallable;

import java.util.ArrayList;
import java.util.List;

public class std {
    
    public static class math {
        public static class max implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Double v1 = (Double) args.get(0);
                Double v2 = (Double) args.get(1);
                return Math.max( v1, v2 );
            }

            @Override
            public int arity() {
                return 2;
            }

            @Override
            public String toString() { return "<native fun - max>"; }
        }

        public static class min implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Double v1 = (Double) args.get(0);
                Double v2 = (Double) args.get(1);
                return Math.min( v1, v2 );
            }

            @Override
            public int arity() {
                return 2;
            }

            @Override
            public String toString() { return "<native fun - max>"; }
        }

    }

    public static class io {

        public static java.util.Scanner s = new java.util.Scanner(System.in);
       
        public static class cin implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {

                return s.next();

            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() { return "<native fun - cin>"; }
        }


    }

    public static class str {

        public static class len implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Object v1 =  args.get(0);
                if (v1 instanceof ArrayList<?> c){
                    return (double)c.size();
                } else {
                    return ((String) v1).length();
                }
            }

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public String toString() {
                return "<native fun - length>";
            }
        }

        public static class substr implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                String v1 = (String) args.get(0);
                Integer v2 = (Integer) args.get(1);
                Integer v3 = (Integer) args.get(2);
                return v1.substring(v2, v3);
            }

            @Override
            public int arity() {
                return 3;
            }

            @Override
            public String toString() {
                return "<native fun - substr>";
            }
        }

        public static class concat implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                String v1 = (String) args.get(0);
                String v2 = (String) args.get(1);
                return v1.concat(v2);
            }

            @Override
            public int arity() {
                return 2;
            }

            @Override
            public String toString() {
                return "<native fun - concat>";
            }
        }

        public static class split implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                String v1 = (String) args.get(0);
                String v2 = (String) args.get(1);
                return v1.split(v2);
            }

            @Override
            public int arity() {
                return 2;
            }

            @Override
            public String toString() {
                return "<native fun - split>";
            }
        }

        public static class replace implements LoxCallable {

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                String v1 = (String) args.get(0);
                String regex = (String) args.get(1);
                String replacement = (String) args.get(2);
                return v1.replace(regex, replacement);
            }

            @Override
            public int arity() {
                return 3;
            }

            @Override
            public String toString() {
                return "<native fun - replace>";
            }
        }
    }

}

