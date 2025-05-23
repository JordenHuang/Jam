package org.jam.nodes;

import org.jam.*;

public class LogicalNode<T> extends Expr<T> {
    public final Expr<T> left;
    public final Token operator;
    public final Expr<T> right;

    public LogicalNode(Expr<T> left, Token operator, Expr<T> right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitLogicalExpr(this);
    }
}
