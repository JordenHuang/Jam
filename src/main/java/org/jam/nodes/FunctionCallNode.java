package org.jam.nodes;

import org.jam.nodes.ExprVisitor;
import org.jam.Token;
import java.util.List;

public class FunctionCallNode<T> extends Expr<T> {
    public final Token name;
    public final List<Expr<T>> arguments;

    public FunctionCallNode(Token name, List<Expr<T>> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitFunctionCallExpr(this);
    }
}
