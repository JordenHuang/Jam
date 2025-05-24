package org.jam.nodes;

import org.jam.Token;

public class VariableNode<T> extends Expr<T> {
    public final Token name;
    public String typeName;

    public VariableNode(Token name, String typeName) {
        this.name = name;
        this.typeName = typeName;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitVariableExpr(this);
    }
}