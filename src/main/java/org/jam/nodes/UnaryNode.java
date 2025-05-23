package org.jam.nodes;

import org.jam.*;

public class UnaryNode<T> extends Expr<T> {
    public final Token operator;
    public final Expr<T> right;

    public UnaryNode(Token operator, Expr<T> right) {
        this.operator = operator;
        this.right = right;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitUnaryNode(this);
    }
}
