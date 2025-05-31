package org.jam;

import org.jam.nodes.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private final Reporter reporter;
    private int current = 0;

    public Parser(List<Token> tokens, Reporter reporter) {
        this.tokens = tokens;
        this.reporter = reporter;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            if (check(TokenType.HTML))
                statements.add(htmlStatement());
            else
                statements.add(declaration());
        }

        return statements;
    }

    private Expr expression() {
        return assignmentExpr();
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.CHAR_TYPE, TokenType.BOOLEAN_TYPE, TokenType.STRING_TYPE)) {
                return varDeclaration();
            }
            if (match(TokenType.INCLUDE))
                return includeStatement();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token typeToken = previous();
        String typeName;// = Token.convertTypeToString(typeToken.type);
        switch (typeToken.type) {
            case INT_TYPE -> typeName = "Integer";
            case DOUBLE_TYPE -> typeName = "Double";
            case CHAR_TYPE -> typeName = "Character";
            case STRING_TYPE -> typeName = "String";
            default -> typeName = typeToken.lexeme;
        }
        reporter.log("typename: " + typeName);

        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        // Consume the ';'s
        do {
            consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        } while (check(TokenType.SEMICOLON));

        return new VarNode(name, typeName, initializer);
    }

    private Stmt htmlStatement() {
        return new HtmlNode(primary());
    }

    private Stmt includeStatement(){
        if (!check(TokenType.STRING)) {
            throw error(previous(), "Invalid filename to include.");
        }
        advance();
        Token filename = previous();
        // Consume the ';'s
        do {
            consume(TokenType.SEMICOLON, "Expect ';' after include statement.");
        } while (check(TokenType.SEMICOLON));
        return new IncludeNode(filename);
    }

    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.LEFT_BRACE)) return new BlockNode(block());
        return expressionStatement();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        boolean flag = false;
        // Consume the ';'s, if present
        while (check(TokenType.SEMICOLON)) {
            consume(TokenType.SEMICOLON, "Expect ';' after expression.");
            flag = true;
        }

        // if it is an expression statement with ';' at the end
        if (flag) return new ExpressionNode(expr);
        else return new HtmlNode(expr);
    }

    private Stmt ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new IfNode(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new WhileNode(condition, body);
    }

    private Stmt forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.INT_TYPE, TokenType.DOUBLE_TYPE, TokenType.CHAR_TYPE, TokenType.BOOLEAN_TYPE, TokenType.STRING_TYPE, TokenType.IDENTIFIER)) { // TODO: check `TokenType.IDENTIFIER`
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");
        Stmt body = statement();

        if (increment != null) {
            body = new BlockNode(
                    Arrays.asList(body, new ExpressionNode(increment))
            );
        }

        if (condition == null) condition = new LiteralNode(true);
        body = new WhileNode(condition, body);

        if (initializer != null) {
            body = new BlockNode(Arrays.asList(initializer, body));
        }

        return body;
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expr assignmentExpr() {
        Expr expr = conditionalExpr();

        // If the next token is '=', treat this as an assignment
        if (match(TokenType.EQUAL, TokenType.PLUS_EQUAL, TokenType.MINUS_EQUAL,
                TokenType.STAR_EQUAL, TokenType.SLASH_EQUAL, TokenType.PERCENT_EQUAL)) {
            Token operator = previous();
            Expr value = assignmentExpr();  // NOTE: recursive!

            // Validate the left-hand side
            if (expr instanceof VariableNode || expr instanceof GetNode) {
                Expr target = (expr);
                return new AssignmentNode(target, operator, value);
            }

            throw error(operator, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr conditionalExpr() {
        Expr expr = logicalOr();

        if (match(TokenType.QUESTION_MARK)) {
            throw error(previous(), "Conditional expression is NOT supported yet!");
            // TODO
//            Token equals = previous();
//            Expr value = expression();
//
//            if (expr instanceof VariableNode) {
//                Token name = ((VariableNode)expr).name;
//                return new AssignmentNode(name, value);
//            }
//
//            reporter.error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr logicalOr() {
        Expr expr = logicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = logicalAnd();
            expr = new LogicalNode(expr, operator, right);
        }

        return expr;
    }

    private Expr logicalAnd() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new LogicalNode(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new BinaryNode(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new BinaryNode(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new BinaryNode(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new BinaryNode(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new UnaryNode(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TokenType.TRUE)) return new LiteralNode(true);
        if (match(TokenType.FALSE)) return new LiteralNode(false);
        if (match(TokenType.NULL)) return new LiteralNode(null);

        if (match(TokenType.NUMBER, TokenType.CHAR, TokenType.STRING)) {
            return new LiteralNode(previous().literal);
        }

        if (match(TokenType.HTML))
            return new LiteralNode(previous().literal);

        if (match(TokenType.IDENTIFIER)) {
            Token token = previous();
            return finishCall(new VariableNode(token, "Identifier"));
        }

        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return finishCall(new GroupingNode(expr));
        }
        throw error(peek(), "Expect expression.");
    }
    
    // Add field access and array access support
    private Expr finishCall(Expr expr) {
        while (true) {
            if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
                expr = new GetNode(expr, name);
            } else if (match(TokenType.LEFT_BRACKET)) {
                Token bracket = previous();
                Expr index = expression();
                consume(TokenType.RIGHT_BRACKET, "Expect ']' after array index.");
                expr = new ArrayAccessNode(expr, index, bracket);
            } else {
                break;
            }
        }
        
        return expr;
    }


    // Helper functions
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // Error handling
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        reporter.error(token, message);
        return new ParseError();
    }

    // Use to find the next statement, so that if the error
    // happens, it can still parse the tokens after the error
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}