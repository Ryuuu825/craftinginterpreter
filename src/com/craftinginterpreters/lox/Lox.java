package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {

        System.out.println(Paths.get("./").toAbsolutePath());

        if (args.length > 1 ) {
            System.out.println("Usage : jlox [script]");
            System.exit(64);
        } else if ( args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }

    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader sr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(sr);

        for(;;) {
            System.out.print("> ");
            String line = br.readLine();
            if (line == null) break;
            run(line);
            hadError = false; // do not kill entire session
        }
    }


    private static void run(String s) {

        Scanner scanner = new Scanner(s);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> expression = parser.parse();

        if (hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(expression);

        interpreter.interpret(expression);

    }

    static void error(int line , String message) {
        report(line , "" , message);
    }

    static void error(String callee , String message) {
        report( "" , message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at end" , message);
        } else {
            report(token.line, "at '" + token.lexeme + "'" , message);
        }
    }

    private static void report(int line, String where , String message) {
        System.err.printf("\n[Line %d] Error %s : %s\n" , line , where , message);
        hadError = true;
    }

    private static void report(String where , String message) {
        System.err.printf("\nError %s : %s\n" ,  where , message);
        hadError = true;
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(String.format("\n[line %s] : " , error.token.line) + error.getMessage()  );
        hadRuntimeError = true;
    }

    public static void runtimeError(String error) {
        System.err.println(String.format("\n%s" , error ));
        hadRuntimeError = true;
    }
}
