package com.craftinginterpreters.lox;

import java.util.List;

abstract public class Stmt {

    interface Visitor<R> {
        R visitPrintStmt(Print stmt);
        R visitExpressionStmt(Expression stmt);

        R visitVarStmt(Var stmt);

        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitFunctionStmt(Function stmt);
        R visitBreakStmt(Break stmt) throws Interpreter.BreakFromBlock;
        R visitReturnStmt(Return stmt) throws Interpreter.BreakFromBlock;

        R visitClassStmt(Class stmt);

        R visitUseLibStmt(UseLib useLib);
    }

    static class Return extends Stmt {

        final Token keyword;
        final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor)  {
            return visitor.visitReturnStmt(this);
        }
    }

    static class Break extends Stmt {

        @Override
        <R> R accept(Visitor<R> visitor) throws Interpreter.BreakFromBlock {
            return visitor.visitBreakStmt(this);
        }
    }

    static class Print extends Stmt {
        final Expr expr;

        public Print(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }



    static class Expression extends Stmt {
        final Expr expr;

        public Expression(Expr expr) {
            this.expr = expr;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Var extends Stmt {
        final Token name;
        final Expr value;

        public Var(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    static class Block extends Stmt {
        final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class Function extends Stmt {

        final Token name;
        final List<Token> params;
        final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    static class If extends Stmt {

        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class While extends Stmt {

        final Expr condition;

        final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    static class Class extends Stmt {

        final Token name;
        final List<Stmt.Function> funcs;
        final Expr.Variable superclass;

        public Class(Token name, Expr.Variable superclass, List<Function> funcs) {
            this.name = name;
            this.superclass = superclass;
            this.funcs = funcs;
        }

        @Override
        <R> R accept(Visitor<R> visitor) throws Interpreter.BreakFromBlock {
            return visitor.visitClassStmt(this);
        }
    }

    static class UseLib extends Stmt {
        final Token name;

        public UseLib(Token name) {
            this.name = name;
        }

        // std::all -> std , all.java
        public String resolveNamespace() {
            String[] arr = name.lexeme.split("::");
            StringBuilder sb = new StringBuilder();
            for ( int i = 0 ; i < arr.length - 1; i++ ) {
                sb.append(arr[i] + "$");
            }
            return sb.toString();
        }

        public String resolveFile() {
            String[] arr = name.lexeme.split("::");
            return arr[arr.length-1];
        }

        @Override
        <R> R accept(Visitor<R> visitor)  {
            return visitor.visitUseLibStmt(this);
        }
    }




    abstract <R> R accept(Visitor<R> visitor) throws Interpreter.BreakFromBlock;

}
