package org.jam.nodes;

import org.jam.*;

public class VariableNode<T> extends Expr<T> {
    public final Token name;
    public TokenType tokenType;

    public VariableNode(Token name, TokenType tokenType) {
        this.name = name;
        this.tokenType = tokenType;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitVariableExpr(this);
    }
}
