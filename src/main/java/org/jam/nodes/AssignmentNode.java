package org.jam.nodes;

import org.jam.*;

public class AssignmentNode<T> extends Expr<T> {
    public final Token name;
    public final Expr<T> value;

    public AssignmentNode(Token name, Expr<T> value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {

        return visitor.visitAssignmentExpr(this);
    }
}
