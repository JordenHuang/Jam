package org.jam.nodes;

public abstract class Expr<T> {
    public abstract T accept(ExprVisitor<T> visitor);
}
