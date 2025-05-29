package org.jam.nodes;

import org.jam.*;

public class AstPrinter implements ExprVisitor<String> {
    @Override
    public String visitUnaryNode(UnaryNode<String> node) {
        return parenthesize(node.operator.lexeme, node.right);
    }

    @Override
    public String visitBinaryNode(BinaryNode<String> node) {
        return parenthesize(node.operator.lexeme, node.left, node.right);
    }

    @Override
    public String visitLiteralNode(LiteralNode<String> node) {
        if (node.value == null) return "null";
        return node.value.toString();
    }

    @Override
    public String visitGroupingNode(GroupingNode<String> node) {
        return parenthesize("group", node.expression);
    }

    // TODO: Maybe
    @Override
    public String visitVariableExpr(VariableNode<String> expr) {
        return null;
    }

    @Override
    public String visitAssignmentExpr(AssignmentNode<String> expr) {
        return null;
    }

    @Override
    public String visitLogicalExpr(LogicalNode<String> visitor) {
        return null;
    }

    @Override
    public String visitGetExpr(GetNode<String> expr) {
        return null;
    }

    public String print(Expr<String> expr) {
        return expr.accept(this);
    }

    @SafeVarargs
    private String parenthesize(String name, Expr<String>... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr<String> expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        AstPrinter astPrinter = new AstPrinter();
        Expr<String> expression = new BinaryNode<>(
                new UnaryNode<>(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new LiteralNode<>(123)
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new GroupingNode<>(
                        new LiteralNode<>(45.67)
                )
        );

        System.out.println(astPrinter.print(expression));
    }
}
