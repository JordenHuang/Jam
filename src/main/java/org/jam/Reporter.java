package org.jam;

public class Reporter {
    public boolean hadError = false;
    public boolean hadRuntimeError = false;
    private boolean debug = false;

    public void setDebug(boolean value) {
        debug = value;
    }

    // Message logger methods
    public void log(String msg) {
        if (debug) System.out.println(msg);
    }

    // Error reporter methods
    public void error(int line, String message) {
        report(line, "", message);
    }

    public void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    public void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + " [line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public void runtimeException(RuntimeException error) {
        System.err.println(error.getMessage());
        hadRuntimeError = true;
    }
}
