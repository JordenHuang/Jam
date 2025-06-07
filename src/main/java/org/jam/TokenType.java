package org.jam;

public enum TokenType {
    // html code
    HTML,
    INCLUDE,

    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_BRACKET, RIGHT_BRACKET,
    COMMA, PLUS, MINUS, STAR, SLASH, PERCENT, DOT, SEMICOLON,
    QUESTION_MARK,

    // One or two character tokens
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Logical operators
    AND, OR,

    // Equals
    PLUS_EQUAL, MINUS_EQUAL, STAR_EQUAL, SLASH_EQUAL, PERCENT_EQUAL,
//    BITWISE_NOT_EQUAL, BITWISE_AND_EUQAL, BITWISE_OR_EUQAL,
//    LEFT_SHIFT_EQUAL, RIGHT_SHIFT_EQUAL, UNSIGNED_RIGHT_SHIFT_EQUAL,

    // ++ and --
//    PLUS_PLUS, MINUS_MINUS,

    // Literals
    IDENTIFIER, NUMBER,
    CHAR, STRING,
    TRUE, FALSE,

    // Keywords
    IF, ELSE, FOR, WHILE, NULL, RETURN,

    // Primitive types
    INT_TYPE, DOUBLE_TYPE, CHAR_TYPE, STRING_TYPE, BOOLEAN_TYPE,

    EOF;
}
