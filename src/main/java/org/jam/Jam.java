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
import org.jam.outputType.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Jam {
    private Environment env;
    private Reporter reporter;
    private List<Token> tokens;
    private List<Stmt> statements;

    public Jam() {
        env = new Environment();
        reporter = new Reporter();
    }

    public Jam(Environment env) {
        this.env = env;
        reporter = new Reporter();
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public List<Stmt> getStatements() {
        return statements;
    }

    public void dumpTokens() {
        System.out.println("===== Tokens =====");
        for (Token token : tokens) {
            System.out.println(token);
        }
        System.out.println("==================");
    }

    public void renderTemplate(String path, IOutput outputMethod) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            byte[] result = run(new String(bytes, Charset.defaultCharset()));
            outputMethod.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void renderTemplate(String path, IOutput outputMethod, Map<String, Object> context) {
        try {
            for (Map.Entry<String, Object> pair : context.entrySet()) {
                env.addToEnv(pair.getKey(), pair.getValue());
            }
            byte[] bytes = Files.readAllBytes(Paths.get(path));
            byte[] result = run(new String(bytes, Charset.defaultCharset()));
            outputMethod.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void clearEnv() {
        if (env != null) env = new Environment();
    }

    public void runInteractiveShell(IOutput outputMethod) throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        System.out.println("Jam interactive shell");
        System.out.println("Each input is evaluate in a separated environment.");
        System.out.println("Type \"exit\" to exit.");
        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line.equals("exit")) break;
            if (line.isEmpty()) {
                continue;
            }
            byte[] result = run(line);
            outputMethod.write(result);
            if (!(reporter.hadError || reporter.hadRuntimeError)) System.out.print("\n");
            reporter.hadError = false;
            reporter.hadRuntimeError = false;
            clearEnv();
        }
    }

    public void runBatchPrompt(String[] prompts, IOutput outputMethod) {
        int i = 0;
        for (String prompt : prompts) {
            System.out.printf("%2d> %s\n", i, prompt);
            byte[] result = run(prompt);
            outputMethod.write(result);
            reporter.hadError = false;
            reporter.hadRuntimeError = false;
            clearEnv();
        }
    }

    public byte[] run(String source) {
        Lexer lexer = new Lexer(source, reporter);
        tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens, reporter);
        statements = parser.parse();
        Interpreter interpreter = new Interpreter(env, reporter);

        // Stop if there was a syntax error.
        if (reporter.hadError) {
            System.err.println("[ERROR] Error happens during scanning or parsing, abort.");
            return null;
        }

        byte[] result = interpreter.interpret(statements);
        if (reporter.hadRuntimeError) {
            System.err.println("[ERROR] Error happens during evaluating, abort.");
            return null;
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        Jam jam = new Jam();
        if (args.length > 1) {
            System.out.println("Usage: jam [template path]");
            System.exit(64);
        } else if (args.length == 1) {
            jam.renderTemplate(args[0], new StandardOutput());
        } else {
            jam.runInteractiveShell(new StandardOutput());
        }
    }
}
