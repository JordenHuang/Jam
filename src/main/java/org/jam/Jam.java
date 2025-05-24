package org.jam;

// Reference:
// - https://craftinginterpreters.com/scanning.html
// - https://craftinginterpreters.com/representing-code.html
// - https://www.youtube.com/watch?v=4oQ-ZPaQs3k&list=PL_2VhOvlMk4UHGqYCLWc6GO8FaPl8fQTh&index=2

// Steps:
// 1. Tokenize
// 2. Parsing
// 3. Evaluating
// 4. Output

import org.jam.nodes.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Jam {
    private Reporter reporter;
    private List<Token> tokens;
    private List<Stmt> statements;

    public Jam() {
        reporter = new Reporter();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Stmt> getStatements() {
        return statements;
    }

    public void printTokens() {
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public void renderTemplate(String path) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            run(new String(bytes, Charset.defaultCharset()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code.
        if (reporter.hadError) System.exit(65);
        if (reporter.hadRuntimeError) System.exit(70);
    }

    public void runInteractiveShell() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        // TODO: remove below

        System.out.print("Jam interactive shell\n");
        System.out.print("Type \"exit\" to exit.");
        for (;;) {
            System.out.print("\n> ");
            String line = reader.readLine();
            if (line.equals("exit")) break;
            if (line.isEmpty()) continue;
            run(line);
            reporter.hadError = false;
            reporter.hadRuntimeError = false;
        }
    }

    public void runBatchPrompt(String[] prompts) {
//        String[] testCodes = new String[]{
//                "",
//                "<tr> {% int i = 1; while ( i < 3) {  i  i = i+1;} %} </tr>",
//                "<tr> {% for (int i = 1; i < 10; i=i+1) { %} <th>{% i %}</th> {% } %} </tr>",
//                "<p>{%int i = 0; double j = 1.0; i+j%}</p>",
//                "{%int i = -1; i%}",
//        };
        int i = 0;
        for (String prompt : prompts) {
            System.out.printf("%2d> %s\n", i, prompt);
            run(prompt);
        }
    }

    public void run(String source) {
        Lexer lexer = new Lexer(source, reporter);
        tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens, reporter);
        statements = parser.parse();
        Interpreter interpreter = new Interpreter(reporter);

        // Stop if there was a syntax error.
        if (reporter.hadError) {
            System.err.println("[ERROR] Error happens during scanning or parsing, abort.");
            return;
        }

        interpreter.interpret(statements);
        if (reporter.hadRuntimeError) {
            System.err.println("[ERROR] Error happens during evaluating, abort.");
            return;
        }
    }

    public static void main(String[] args) throws IOException {
        Jam jam = new Jam();
        if (args.length > 1) {
            System.out.println("Usage: jam [script]");
            System.exit(64);
        } else if (args.length == 1) {
            jam.runFile(args[0]);
        } else {
//            String basePath = "src/main/templates/";
//            String templateFileName = "9x9.template";
//            runFile(basePath + templateFileName);
            jam.runInteractiveShell();
        }
    }
}
