package org.jam.nodes;

public class LiteralNode<T> extends Expr<T> {
    public final Object value;

    public LiteralNode(Object value) {
        this.value = value;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitLiteralNode(this);
    }
}
