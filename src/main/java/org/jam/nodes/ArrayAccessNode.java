package org.jam.nodes;

import org.jam.Token;

public class ArrayAccessNode<T> extends Expr<T> {
    public final Expr<T> array;
    public final Expr<T> index;
    public final Token bracket;

    public ArrayAccessNode(Expr<T> array, Expr<T> index, Token bracket) {
        this.array = array;
        this.index = index;
        this.bracket = bracket;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitArrayAccessExpr(this);
    }
}