package org.jam;

public class Token {
    public static String convertTypeToString(TokenType type) {
        String s = type.toString();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String getTypeString() {
        return Token.convertTypeToString(this.type);
    }

    @Override
    public String toString() {
//        return type + "|" + literal;
//        return type + "|" + lexeme + "|" + literal;
        return type + "|" + lexeme.replace("\n", "") + "|" + literal;
    }
}
