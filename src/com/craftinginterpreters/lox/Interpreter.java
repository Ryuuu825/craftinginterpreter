package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Interpreter implements Expr.Visitor<Object> , Stmt.Visitor<Void> {

    public static class BreakFromBlock extends RuntimeException {}
    class Return extends RuntimeException {
        final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

    final Environment globals = new Environment();
    private Environment environment = globals;

    private final Map<Expr, Integer> locals = new HashMap<>();
    private Stmt current = null;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double)System.currentTimeMillis() / 1000.0;
            }

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public String toString() { return "<native fun - clock>"; }
        });

        globals.define("print", new LoxCallable() {
            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                for (Object arg : args) {
                    System.out.print(stringify(arg) + " ");
                }
                System.out.println();
                return null;
            };

            @Override
            public int arity() {
                return -1;
            }

            @Override
            public String toString() { return "<native fun - print>"; }
        });

        
        String[] libs = {"math" , "io" , "str"};
        for (String lib : libs) {
            try {
                Class<?> clazz = Class.forName("com.craftinginterpreters.lib.std$" + lib);
                for (Class<?> c : clazz.getClasses()) {
                    LoxCallable instance = (LoxCallable) c.newInstance();
                    globals.define( c.getSimpleName() , instance);
                }
            } catch (Exception e) {
                System.err.println("Could not locate target file under library dir: " + lib);
            }
        }
    }


    public void resolve(Expr expr, int depth) {
        locals.put(expr , depth);
    }


    void interpret(List<Stmt> stmts) {
        try {
            for (Stmt s : stmts) {
                 try {
                     s.accept(this);
                 } catch (BreakFromBlock e) {
                     return;
                 }
            }
        }
        catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    public String stringify(Object obj) {
        if (obj == null) return "nil";

        if (obj instanceof Double) {
            String text = obj.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return obj.toString();
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);

        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator , left , right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator , left , right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator , left , right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator , left , right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case MINUS:
                checkNumberOperand(expr.operator ,  right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperands(expr.operator , left , right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator , left , right);
                return (double)left * (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }

                if (left instanceof String && right instanceof Double) {
                    return (String)left + stringify(right);
                }


                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");

        }

        // Unreachable.
        return null;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expr);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return ((Expr.Literal) expr).value;
    }

    @Override
    public Object visitMultiLiteralExpr(Expr.MultiLiteral expr) {
        return expr.values;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.operator.type) {
            case MINUS -> {
                return -(double)right;
            }
            case BANG -> {
                return !isTrusty(right);
            }
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.name , expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);

        if (distance != null) {
            return environment.getAt(distance , name.lexeme);
        } else {
            return globals.get(name);
        }
    }

    @Override
    public Object visitGlobalVariableExpr(Expr.GlobalVariable expr) {
        return environment.getFromGlobal(expr.name);
    }

    @Override
    public Object visitAssignmentExpr(Expr.Assignment expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null ) {
            environment.assignAt(distance , expr.name , value);
        } else {
            globals.assign(expr.name , value);
        }
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTrusty(left)) return left;
        } else {
            if (!isTrusty(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        if (isTrusty(evaluate(expr.condition))) {
            return evaluate(expr.thenValue);
        } else {
            return evaluate(expr.elseValue);
        }
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> args = new ArrayList<>(expr.args.stream().map(this::evaluate).toList());

        if (!(callee instanceof LoxCallable function)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }

        if (function.arity() >= 0 && args.size() != function.arity() ) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    args.size() + ".");
        }

        Object object = function.call(this , args);

        if ( object instanceof LoxInstance c) {
            try {
                for(LoxFunction func : ((LoxClass) callee).superclass.funcs.values()) {
                    c.set(func.funName(), new LoxFunction( func.declaration , c.env ,  func.funName().equals("init")));
                }

                for(LoxFunction func : ((LoxClass) callee).funcs.values()) {
                    c.set(func.funName(), new LoxFunction( func.declaration , c.env ,  func.funName().equals("init")));
                }

                return c;

            } catch(ClassCastException ignored) {}
        }



        return object;
    }

    @Override
    public Object visitAnonymousFuncExpr(Expr.AnonymousFunc expr) {
        return new LoxFunction( new Stmt.Function( expr.func.name , expr.func.params , expr.func.body) , this.globals , false );
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        Object object = evaluate(expr.Obj);
        if (object instanceof LoxInstance) {
            return ((LoxInstance) object).get(expr.name);
        }

        throw new RuntimeError(expr.name,
                "Only instances have properties.");
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        Object obj = evaluate(expr.obj);

        if (!(obj instanceof LoxInstance)) {
            throw new RuntimeError(expr.name,
                    "Only instances have fields.");
        }

        Object value = evaluate(expr.value);
        ((LoxInstance)obj).set(expr.name, value);
        return value;
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        return lookUpVariable(expr.keyword , expr);
    }

    @Override
    public Object visitSubscriptExpr(Expr.Subscript subscript) {
        ArrayList<Object> array = (ArrayList<Object>) environment.get(subscript.name);
        try {
            return stringify(array.get(Integer.parseInt(stringify(evaluate(subscript.index)))));
        } catch (IndexOutOfBoundsException e) {
            throw new RuntimeError( subscript.name , "index out of bound");
        }
    }

    private void checkNumberOperand(Token op , Object operand ) {
        if ( operand instanceof Double ) return;
        throw new RuntimeError( op , "Operand must be a number");
    }

    private void checkNumberOperands(Token operator,
                                     Object left, Object right) {

        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }


    private boolean isTrusty(Object obj) {

        if (obj == null) return false ;
        if (obj instanceof Boolean) return (boolean) obj;
        if (obj instanceof Expr.Binary) return evaluate(((Expr.Binary)obj).left).equals(evaluate(((Expr.Binary)obj).right));

        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expr);
        // System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expr);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if ( stmt.value != null ) {
            value = evaluate(stmt.value);
        }

        environment.define(stmt.name.lexeme , value);

        return null;

    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {

        executeBlock(stmt.statements , new Environment(environment));

        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTrusty(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        try {
            while (isTrusty(evaluate(stmt.condition))) {
                execute(stmt.body);
            }
        } catch (BreakFromBlock ignored) {}
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        globals.define(stmt.name.lexeme , new LoxFunction(stmt, environment,false));
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) throws BreakFromBlock {
        throw new Interpreter.BreakFromBlock();
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) throws BreakFromBlock {
        Object value = null;
        if (stmt.value != null ) value = evaluate(stmt.value);

        throw new Return(value);
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        Object superclass = null;
        Map<String , LoxFunction> map = new HashMap<>();


        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);

            if (!(superclass instanceof LoxClass sc)) {
                throw new RuntimeError(stmt.superclass.name,
                        "Superclass must be a class.");
            }

            map.put("init" , ((LoxClass) superclass).funcs.get("init"));

        }


        environment.define( stmt.name.lexeme , null );
        for (Stmt.Function f: stmt.funcs ) {
            map.put( f.name.lexeme , new LoxFunction(f, globals, f.name.lexeme.equals("init")));
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme,
                (LoxClass) superclass, map);
        environment.assign(stmt.name, klass);

        return null;
    }

    @Override
    public Void visitUseLibStmt(Stmt.UseLib useLib) {
        try {
            Class<?> clazz = Class.forName("com.craftinginterpreters.lib." + useLib.resolveNamespace() + useLib.resolveFile());
            LoxCallable instance = (LoxCallable) clazz.newInstance();
            globals.define( useLib.resolveFile() , instance);
        } catch (Exception e) {
            System.err.println("Could not found target file under library: " + useLib.resolveNamespace() + useLib.resolveFile());
        }
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) throws BreakFromBlock {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt s : statements) {
                execute(s);
            }

        }
        finally {
            this.environment = previous;
        }
    }

    private void execute(Stmt statements) throws BreakFromBlock {
        current = statements;
        statements.accept(this);
    }


}
