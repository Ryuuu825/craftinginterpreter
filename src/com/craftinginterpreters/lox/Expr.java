package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

abstract public class Expr {

    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitMultiLiteralExpr(MultiLiteral expr);

        R visitUnaryExpr(Unary expr);

        R visitVariableExpr(Variable expr);

        R visitGlobalVariableExpr(GlobalVariable expr);

        R visitAssignmentExpr(Assignment expr);

        R visitLogicalExpr(Logical expr);
        R visitTernaryExpr(Ternary expr);
        R visitCallExpr(Call expr);
        R visitAnonymousFuncExpr(AnonymousFunc expr);

        R visitGetExpr(Get get);

        R visitSetExpr(Set set);

        R visitThisExpr(This aThis);

        R visitSubscriptExpr(Subscript subscript);
    }

    static class Subscript extends Expr {
        final Token name;
        final Expr index;

        public Subscript(Token name, Expr index) {
            this.name = name;
            this.index = index;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSubscriptExpr(this);
        }
    }

    static class AnonymousFunc extends Expr {
        final Stmt.Function func;

        public AnonymousFunc(Stmt.Function func) {
            this.func = func;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAnonymousFuncExpr(this);
        }
    }



    static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> args;

        public Call(Expr callee, Token paren, List<Expr> args) {

            this.callee = callee;
            this.paren = paren;
            this.args = args;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpr(this);
        }
    }


    static class Binary extends Expr {
        final Expr left;
        final Expr right;

        final Token operator;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    static class Grouping extends Expr {
        final Expr expr;

        public Grouping(Expr expr) {
            this.expr = expr;
        }


        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }
    }

    static class Literal extends Expr {
        final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }
    }

    static class MultiLiteral extends Expr {
        final ArrayList<Object> values;

        public MultiLiteral(ArrayList<Object> values) {
            this.values = values;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitMultiLiteralExpr(this);
        }
    }

    static class Unary extends Expr {
        final Token operator;
        final Expr right;

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }
    }

    static class Variable extends Expr {
        final Token name;

        public Variable(Token value) {
            this.name = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpr(this);
        }
    }

    static class Assignment extends Expr {

        final Token name;
        final Expr value;

        public Assignment(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignmentExpr(this);
        }
    }

    public static class GlobalVariable extends Expr {
        final Token name;

        public GlobalVariable(Token name) {
            this.name = name;
        }


        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGlobalVariableExpr(this);
        }
    }

    public static class Logical extends Expr {

        final Expr left;
        final Token operator;
        final Expr right;

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpr(this);
        }
    }

    static class Ternary extends Expr {

        final Expr condition;
        final Expr thenValue;
        final Expr elseValue;

        public Ternary(Expr condition, Expr thenValue, Expr elseValue) {

            this.condition = condition;
            this.thenValue = thenValue;
            this.elseValue = elseValue;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }
    }

    static class Get extends Expr {

        final Expr Obj;
        final Token name;

        public Get(Expr obj, Token name) {
            Obj = obj;
            this.name = name;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpr(this);
        }
    }

    static class Set extends Expr {
        final Expr obj;
        final Token name;
        final Expr value;

        public Set(Expr obj, Token name, Expr value) {
            this.obj = obj;
            this.name = name;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpr(this);
        }
    }

    static class This extends Expr {
        final Token keyword;

        public This(Token keyword) {

            this.keyword = keyword;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpr(this);
        }
    }
}
