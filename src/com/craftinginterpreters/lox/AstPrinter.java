package com.craftinginterpreters.lox;

import java.util.Arrays;

abstract class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expr);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitMultiLiteralExpr(Expr.MultiLiteral expr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object e : expr.values) {
            sb.append(e + " ,");
        }
        sb.append("]");

        return sb.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "Variable : " + expr.name;
    }

    @Override
    public String visitGlobalVariableExpr(Expr.GlobalVariable expr) {
        return "Global Variable " + expr.name;
    }

    @Override
    public String visitAssignmentExpr(Expr.Assignment expr) {
        return "Assignment of Variable(" + expr.name + ") with value : " + expr.value;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return null;
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return null;
    }

    @Override
    public String visitAnonymousFuncExpr(Expr.AnonymousFunc expr) {
        return null;
    }


    private String parenthesize(String name , Expr... exprs) {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(name);
        for (Expr e : (exprs)) {
            sb.append(" ");
            sb.append(e.accept(this));
        }
        sb.append(")");

        return sb.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary (
                new Expr.Unary(
                    new Token(TokenType.MINUS, "-", null, 1),
                    new Expr.Literal(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                    new Expr.Literal(45.67)
                )
        );

        // System.out.println(new AstPrinter().print(expression));
    }

}
