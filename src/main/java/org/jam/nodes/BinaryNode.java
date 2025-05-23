package org.jam.nodes;

import org.jam.*;

public class BinaryNode<T> extends Expr<T> {
    public final Expr<T> left;
    public final Token operator;
    public final Expr<T> right;

    public BinaryNode(Expr<T> left, Token operator, Expr<T> right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitBinaryNode(this);
    }
}
