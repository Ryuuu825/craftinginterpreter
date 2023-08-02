package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private record AnalysisInfo(Token t , Boolean declareInfo, Boolean hadUsed) {
    }
    private final Interpreter interpreter;
    private final Stack<Map<String , AnalysisInfo>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void reportUsedVariable(Map<String, AnalysisInfo> scope) {

        for ( String var : scope.keySet() ) {
            if ( scope.get(var).hadUsed == Boolean.FALSE && ! scope.get(var).t.lexeme.startsWith("_")) {
                Lox.error( scope.get(var).t.line , "Variable<" + scope.get(var).t.lexeme + "> was never used" );
            }
        }

    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expr);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitMultiLiteralExpr(Expr.MultiLiteral expr) {
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) != null && (scopes.peek().get(expr.name.lexeme).declareInfo == Boolean.FALSE )) {
            Lox.error(expr.name , "Can't read local variable in its own initializer");
        }

        resolveLocal(expr , expr.name);
        return null;
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                AnalysisInfo info = scopes.get(i).get(name.lexeme);
                scopes.get(i).put(name.lexeme , new AnalysisInfo(info.t , info.declareInfo , true));
                return;
            }
        }

    }

    @Override
    public Void visitGlobalVariableExpr(Expr.GlobalVariable expr) {
        return null;
    }

    @Override
    public Void visitAssignmentExpr(Expr.Assignment expr) {
        resolve(expr.value);
        resolveLocal( expr , expr.name );
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        resolve(expr.condition);
        resolve(expr.thenValue);
        resolve(expr.elseValue);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr arg : expr.args) {
            resolve(arg);
        }
        return null;
    }

    @Override
    public Void visitAnonymousFuncExpr(Expr.AnonymousFunc expr) {
        resolveFunction(expr.func , FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get get) {
        resolve(get.Obj);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set set) {
        resolve(set.value);
        resolve(set.obj);
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        resolveLocal(expr , expr.keyword);
        return null;
    }

    @Override
    public Void visitSubscriptExpr(Expr.Subscript subscript) {
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expr);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.value != null ) {
            resolve(stmt.value);
        }
        define(stmt.name);
        return null;
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme , new AnalysisInfo(name , true , false));
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, AnalysisInfo> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name , "Already a variable with name <" + name.lexeme + "> in this scope");
        }
        scope.put(name.lexeme, new AnalysisInfo( name ,false,false));
    }


    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    private void endScope() {
        reportUsedVariable(scopes.pop());
    }

    private void beginScope() {
        scopes.push(new HashMap<String, AnalysisInfo>());
    }

    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    void resolve(Stmt statement) {
        statement.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt , FunctionType.FUNCTION);

        return null;
    }

    private void resolveFunction(Stmt.Function stmt , FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : stmt.params) {
            declare(param);
            define(param);
        }
        resolve(stmt.body);
        endScope();
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) throws Interpreter.BreakFromBlock {
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) throws Interpreter.BreakFromBlock {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword , "Can't return from top level code");
        }
        if (stmt.value != null) {
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) {
        declare(stmt.name);
        define(stmt.name);

        if (stmt.superclass != null) {
            resolve(stmt.superclass);
        }

        beginScope();
        scopes.peek().put("this" , new AnalysisInfo(null , true, true ));

        for (Stmt.Function method : stmt.funcs) {
            FunctionType declaration = FunctionType.METHOD;
            resolveFunction(method, declaration);
        }

        endScope();

        return null;
    }

    @Override
    public Void visitUseLibStmt(Stmt.UseLib useLib) {
        return null;
    }
}
