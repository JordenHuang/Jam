package org.jam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("int"      , TokenType.INT_TYPE);
        keywords.put("double"   , TokenType.DOUBLE_TYPE);
        keywords.put("char"     , TokenType.CHAR_TYPE);
        keywords.put("boolean"  , TokenType.BOOLEAN_TYPE);
        keywords.put("Integer"  , TokenType.INT_TYPE);
        keywords.put("Double"   , TokenType.DOUBLE_TYPE);
        keywords.put("Character", TokenType.CHAR_TYPE);
        keywords.put("Boolean"  , TokenType.BOOLEAN_TYPE);
        keywords.put("String"   , TokenType.STRING_TYPE);
        keywords.put("null"     , TokenType.NULL);
        keywords.put("true"     , TokenType.TRUE);
        keywords.put("false"    , TokenType.FALSE);
        keywords.put("if"       , TokenType.IF);
        keywords.put("else"     , TokenType.ELSE);
        keywords.put("for"      , TokenType.FOR);
        keywords.put("while"    , TokenType.WHILE);
        keywords.put("return"   , TokenType.RETURN);
        keywords.put("include"  , TokenType.INCLUDE);
    }

    private final String source;
    private final Reporter reporter;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    // FIXME:
    private boolean isJavaCode = false;
//    private boolean isJavaCode = true;
    public Lexer(String source, Reporter reporter) {
        this.source = source;
        this.reporter = reporter;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        if (isJavaCode) {
            reporter.error(line, "Unterminated Java code block.");
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        boolean isAllWhitespaces = true;

        while (!isJavaCode) {
            switch (c) {
                case '{':
                    if (peek() == '%') {
                        isJavaCode = true;
                        String value = source.substring(start, current-1);
                        if (!isAllWhitespaces) {
                            addToken(TokenType.HTML, value);
                        }
                        // Consume the '%'
                        advance();
                        return;
                    }
                    break;
                case ' ':
                case '\r':
                case '\t':
                    // Ignore whitespace.
                    break;
                case '\n':
                    line += 1;
                    break;
                default:
                    isAllWhitespaces = false;
                    break;
            }

            if (!isAtEnd()) c = advance();
            else {
                String value = source.substring(start, current);
                addToken(TokenType.HTML, value);
                break;
            }
        }

        if (isJavaCode) {
            switch (c) {
                case '%':
                    // Check if Java code block ends
                    if (peek() == '}') {
                        advance();
                        isJavaCode = false;
                        return;
                    } else if (match('=')) {
                        addToken(TokenType.PERCENT_EQUAL);
                    } else {
                        addToken(TokenType.PERCENT);
                    }
                    break;
                case '(':
                    addToken(TokenType.LEFT_PAREN);
                    break;
                case ')':
                    addToken(TokenType.RIGHT_PAREN);
                    break;
                case '{':
                    addToken(TokenType.LEFT_BRACE);
                    break;
                case '}':
                    addToken(TokenType.RIGHT_BRACE);
                    break;
                case '[':
                    addToken(TokenType.LEFT_BRACKET);
                    break;
                case ']':
                    addToken(TokenType.RIGHT_BRACKET);
                    break;
                case ',':
                    addToken(TokenType.COMMA);
                    break;
                case '+':
                    addToken(match('=') ? TokenType.PLUS_EQUAL : TokenType.PLUS);
                    break;
                case '-':
                    addToken(match('=') ? TokenType.MINUS_EQUAL : TokenType.MINUS);
                    break;
                case '*':
                    addToken(match('=') ? TokenType.STAR_EQUAL : TokenType.STAR);
                    break;
                case '.':
                    addToken(TokenType.DOT);
                    break;
                case ';':
                    addToken(TokenType.SEMICOLON);
                    break;
                case '!':
                    addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                    break;
                case '?':
                    addToken(TokenType.QUESTION_MARK);
                    break;
                case '=':
                    addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                    break;
                case '<':
                    addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                    break;
                case '>':
                    addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                    break;
                case '/':
                    if (match('/')) {
                        // A comment goes until the end of the line.
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else if (match('=')) {
                        addToken(TokenType.SLASH_EQUAL);
                    } else {
                        addToken(TokenType.SLASH);
                    }
                    break;
                case ' ':
                case '\r':
                case '\t':
                    // Ignore whitespace.
                    break;
                case '\n':
                    line++;
                    break;
                case '\'': character(); break;
                case '"': string(); break;
                case '|':
                    if (match('|')) {
                        addToken(TokenType.OR);
                    } else {
                        reporter.error(line, "Unexpected character: " + c );
                    }
                    break;
                case '&':
                    if (match('&')) {
                        addToken(TokenType.AND);
                    } else {
                        reporter.error(line, "Unexpected character: " + c );
                    }
                    break;
                default:
                    if (isDigit(c)) {
                        number();
                    } else if (isAlpha(c)) {
                        identifier();
                    } else {
                        reporter.error(line, "Unexpected character: " + c );
                    }
            }
        }
    }

    private void character() {
        // The character
        char c = advance();
        if (c == '\'') {
            reporter.error(line, "Invalid. Character length should be equal to 1.");
            // Check termination
        }
        if (peek() != '\'') {
            while (peek() != '\'' && !isAtEnd()) {
                if (peek() == '\n') line++;
                advance();
            }
            reporter.error(line, "Unterminated character.");
            if (isAtEnd()) return;
        }

        // The closing '.
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.CHAR, value);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            reporter.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(
                TokenType.NUMBER,
                Double.parseDouble(source.substring(start, current))
        );
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    // Helper functions
    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected){
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
