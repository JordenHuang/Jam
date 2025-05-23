package org.jam.nodes;

public class GroupingNode<T> extends Expr<T> {
    public final Expr<T> expression;

    public GroupingNode(Expr<T> expression) {
        this.expression = expression;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitGroupingNode(this);
    }
}
