package org.jam.nodes;

public class ExpressionNode<T> extends Stmt<T> {
    public final Expr<T> expression;

    public ExpressionNode(Expr<T> expression) {
        this.expression = expression;
    }

    @Override
    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitExpressionStmt(this);
    }
}
