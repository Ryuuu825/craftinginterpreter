package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;
import static java.lang.Character.isAlphabetic;
import static java.lang.Character.isDigit;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String s) {
        this.source = s;
    }

    public List<Token> scanTokens() {
        while(!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF , "" , null , line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': {
                addToken( match('>') ? ARROW : MINUS);
                break;
            }
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '?': addToken(QUESTIONMARK); break;
            case ':': addToken(COLON); break;
            case '[': addToken(LEFT_SQ_BRACE); break;
            case ']': addToken(RIGHT_SQ_BRACE); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ' : case '\r' : case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                }
                else {
                    Lox.error(line, "Unexpected character." + c);
                }
        }

    }

    private void identifier() {
        while(isAlphabetic(peek()) || match(':') || match('{') || match('}')) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        if (text.contains(":")) {
            type = USE;
        }
        // check if any space or ; follow by identifier, or else print10 will valid
        char next = peek();
        if ( isAlpha(next) ) {
            Lox.error(line , "Expect an space after identifier<" + text + ">");
        } else if (type == USE) {
            addToken(USE, text);
        }
        else {
            addToken(type);
        }


    }

    private void number() {
        while (isDigit(peek())) advance();

        if ( peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(source.substring(start , current)));

    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()) {
            Lox.error(line , "Unterminated String.");
            return;
        }

        advance(); // ignore the " at trailing

        String value = source.substring(start + 1 , current - 1); // ignore the "
        addToken(STRING , value);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean match(char c) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != c) return false;
        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type , null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start , current);
        tokens.add(new Token(type , text , literal , line));
    }

    private boolean isAtEnd() {
        return current == source.length();
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }


    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        // keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
        keywords.put("global",  GLOBAL);
        keywords.put("break",  BREAK);
        keywords.put("use",  USE);
        keywords.put("import",  IMPORT);
    }
}
