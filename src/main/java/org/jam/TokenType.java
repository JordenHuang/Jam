package org.jam;

public enum TokenType {
    // html code
    HTML,
    INCLUDE,

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, PLUS, MINUS, STAR, SLASH, DOT, SEMICOLON,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals
    IDENTIFIER, NUMBER,
    CHAR, STRING,
    TRUE, FALSE,

    // Keywords
    IF, ELSE, FOR, WHILE, NULL, RETURN, AND, OR,

    // Primitive types
    INT_TYPE, DOUBLE_TYPE, CHAR_TYPE, STRING_TYPE, BOOLEAN_TYPE,

    EOF;
}
