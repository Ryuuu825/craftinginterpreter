package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

public class Parser {

    private static class ParseError extends RuntimeException{}


    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {

            if (match(CLASS)) return classDeclaration();
            if (match(BREAK)) {
                consume(SEMICOLON , "Expect ';' after break;");
                return new Stmt.Break();
            }
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            if (match(USE)) return useLib();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt useLib() {
        Token name = consume(USE , "Expect an name for the class");
        consume(SEMICOLON , "Expect a ';' in the end");
        return new Stmt.UseLib(name);
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER , "Expect an name for the class");

        Expr.Variable superclass = null;
        if (match(LESS)) {
            Token superclassname = consume(IDENTIFIER , "Expect superclass name");
            superclass = new Expr.Variable(superclassname);
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");


        List<Stmt.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE)) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body.");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Function function(String kind) {
        Token name = null;
        if (! kind.equals("anonymous") ) {
            name = consume(IDENTIFIER , "Expect " + kind + " name" );
        } else {
            name = new Token(IDENTIFIER , "anonymous." + kind , "" , -1 );
        }

        List<Token> parameters = new ArrayList<>();
        Boolean isGetterFlag = !check(LEFT_PAREN);

        if ( !isGetterFlag) {
            consume(LEFT_PAREN, "Expect '(' after " + kind + " name.");

            if (!check(RIGHT_PAREN)) {
                do {
                    if (parameters.size() >= 255) {
                        error(peek(), "Can't have more than 255 parameters.");
                    }

                    Token t = consume(IDENTIFIER, "Expect parameter name.");

                    parameters.add(t);

                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expect ')' after parameters.");

        }


        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, parameters, body);
    }


    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER , "Expected variable name");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON , "Expect ';' after variable declaration.");
        return new Stmt.Var(name , initializer);
    }

    private Stmt statement() {

        if (match(BREAK)) {
            consume(SEMICOLON , "Expect ';' after break;");
            return new Stmt.Break();
        }
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        // if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON , "Expect ';' after return value");


        if ( peek().type == EOF ) {
            Lox.error( peek() , "Can't return from top level code");
        }
        else if ( !check(RIGHT_BRACE)) {
            Lox.error(peek() , "\"Don't expect any expression after return\"");
        }
        return new Stmt.Return(keyword , value);
    }


    private Stmt forStatement() {

        consume(LEFT_PAREN , "Expect '(' after 'for");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        } else {
            condition = new Expr.Literal(true);
        }

        consume(SEMICOLON , "Expect ';' after loop condition");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }

        consume(RIGHT_PAREN , "Expect ')' after loop condition");

        Stmt body = statement();

        if ( increment != null ) {
            body = new Stmt.Block(
                Arrays.asList(
                        body,
                        new Stmt.Expression(increment)
                )
            );
        }

        body = new Stmt.While(condition, body);

        if ( initializer != null ) {
            body = new Stmt.Block(Arrays.asList(initializer , body));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN , "Expect '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PAREN , "Expect ')' after while's condition.");
        Stmt body = statement();

        return new Stmt.While(condition , body);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN , "Expect '(' after 'if'");
        Expr condition = expression();
        consume(RIGHT_PAREN , "Expect ')' after if condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition , thenBranch , elseBranch);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while(!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE , "Expect '}' after block");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON , "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        Expr e = null;
        if (match(FUN)) {
            e = new Expr.AnonymousFunc( (Stmt.Function) function("anonymous") );
        } else {
            e = ternary();
        }

        return e;
    }



    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();

            Expr value = assignment();

            if ( expr instanceof Expr.Variable ) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assignment(name , value);
            } else if (expr instanceof Expr.Get get) {
                return new Expr.Set(get.Obj , get.name , value);
            }

            error(equals , "Invalid assignment target");
        }

        return expr;
    }


    private Expr or() {
        Expr expr = and();

        while(match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr , operator , right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while(match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr , operator , right);
        }

        return expr;
    }

    private Expr ternary() {
        // ( expression ) ? ( expression ) : ( expression )
        Expr expr = assignment();

        if (match(QUESTIONMARK)) {

            Expr thenBranch = equality();

            consume(COLON , "Expect an ':' in ternary expression ");

            Expr elseBranch = equality();


            return new Expr.Ternary( expr , thenBranch , elseBranch);


        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while(match(BANG_EQUAL , EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr,operator,right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while(match(SLASH , STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr , operator , right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG , MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator , right);
        }

        return call();
    }

    private Expr call() {
        Expr expr = primary();

        while ( true ) {
            if ( match(LEFT_PAREN) ) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER
                 , "Expect property name after '.'");
                expr = new Expr.Get(expr , name);
            }
            else { break; }
        }

        return expr;
    }

    private Expr finishCall(Expr expr) {
        List<Expr> args = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (args.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }

                args.add(expression());


            } while (match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN , "Expect ')' after argument(s)");

        return new Expr.Call(expr , paren , args);
    }

    private Expr multiLiteral() {
        ArrayList<Object> values = new ArrayList<>();


        values.add(advance().literal);

        if ( check(COMMA) ) {

            while( check(COMMA) ) {
                consume(COMMA , "Expect ',' to separate values");
                Token t = advance();
                values.add(t.literal);
            }

        }

        consume(RIGHT_SQ_BRACE , "Expect ']' after array declaration");

        return new Expr.MultiLiteral(values);
    }

    private Expr checkIfMultiLiteral(TokenType types) {
        Object firstMatchValue = previous().literal;

//        if ( check(COMMA) ) {
//            // ( previous ( , previous )+
//            ArrayList<Object> list = new ArrayList<>();
//            list.add(firstMatchValue);
//
//            while( check(COMMA) ) {
//                consume(COMMA , "Expect ',' to separate values");
//                Token t = advance();
//                list.add(t.literal);
//            }
//
//            return new Expr.MultiLiteral(list);
//        }

        return new Expr.Literal(firstMatchValue);
    }


    private Expr primary() {
        if (match(LEFT_SQ_BRACE)) { return multiLiteral(); }
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER)) { return checkIfMultiLiteral(NUMBER); }
        if (match(STRING)) { return checkIfMultiLiteral(STRING); }
        if (match(THIS)) return new Expr.This(previous());
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        if ( match(GLOBAL) ) {
            return new Expr.GlobalVariable(advance());
        }
        if ( match(IDENTIFIER) ) {
            if ((peek().type == PLUS || peek().type == MINUS) && peek().type == peekNext().type) {

                Expr e = new Expr.Assignment(
                        previous() ,
                        new Expr.Binary(
                                new Expr.Variable(previous()),
                                peek(),
                                new Expr.Literal(1.0)
                        )
                );
                advance(); advance();
                return e;
            } else if (peek().type == LEFT_SQ_BRACE) {
                Token name = previous();
                consume(LEFT_SQ_BRACE , "terst");
                Expr index = expression();
                consume(RIGHT_SQ_BRACE , "terst");

                return new Expr.Subscript(name , index);
            }
            return new Expr.Variable(previous());
        }

        throw error(peek() , "Expect expression.");
    }

    private Token consume(TokenType type , String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while(!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();

        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private boolean check(TokenType... types) {
        if (isAtEnd()) return false;
        for (TokenType type : types) {
            if (check(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkCurr(TokenType type) {
        if (isAtEnd()) return false;
        return tokens.get(current-1).type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token peekNext() {
        return tokens.get(current+1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }




}
