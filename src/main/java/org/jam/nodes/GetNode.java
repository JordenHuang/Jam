package org.jam.nodes;

import org.jam.Token;

public class GetNode<T> extends Expr<T> {
    public final Expr<T> object;
    public final Token name;

    public GetNode(Expr<T> object, Token name) {
        this.object = object;
        this.name = name;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitGetExpr(this);
    }
}