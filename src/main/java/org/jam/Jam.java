package org.jam;

// Reference:
// - https://craftinginterpreters.com/scanning.html
// - https://www.youtube.com/watch?v=4oQ-ZPaQs3k&list=PL_2VhOvlMk4UHGqYCLWc6GO8FaPl8fQTh&index=2
// - https://craftinginterpreters.com/representing-code.html

import org.jam.nodes.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Jam {
    public static boolean debug = false;
//    public static boolean debug = true;
    public static void log(String msg) {
        if (debug) System.out.println(msg);
    }
    public static void setDebug(boolean value) {
        debug = value;
    }

    static boolean hadError;
    static boolean hadRuntimeError = false;

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + " [line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // TODO: remove below
        String[] testCodes = new String[]{
                "",
//                "<tr> {% int i = 1; while ( i < 3) {  i  i = i+1;} %} </tr>",
                "<tr> {% for (int i = 1; i < 10; i=i+1) { %} <th>{% i %}</th> {% } %} </tr>",
//                "<p>{%int i = 0; double j = 1.0; i+j%}</p>",
//                "{%int i = -1; i%}",
        };
        for (String code : testCodes) {
            System.out.println("> " + code);
            run(code);
        }

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line.equals("exit")) break;
            if (line.isEmpty()) continue;
            run(line);
            hadError = false;
        }
    }
    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        Interpreter interpreter = new Interpreter();

        // For now, just print the tokens.
//        for (Token token : tokens) {
//            System.out.println(token);
//        }

//        Parser<String> parserString = new Parser<String>(tokens);
//        Expr<String> expressionString = parserString.parse();

//        // Stop if there was a syntax error.
//        if (hadError) {
//            System.out.println();
//            return;
//        }
//        System.out.println("[INFO] Ast printer:");
//        System.out.println(new AstPrinter().print(expressionString));


        Parser parser = new Parser(tokens);
//        Expr<Object> expression = parser.parse();
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) {
            System.out.println("[ERROR] HAD ERROR");
            return;
        }

//        interpreter.interpret(expression);
        interpreter.interpret(statements);

        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
//            String basePath = "src/main/templates/";
//            String templateFileName = "9x9.template";
//            runFile(basePath + templateFileName);
            runPrompt();
        }
    }
}
