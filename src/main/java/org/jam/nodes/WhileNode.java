package org.jam.nodes;

public class WhileNode<T> extends Stmt<T> {
    public final Expr<T> condition;
    public final Stmt<T> body;

    public WhileNode(Expr<T> condition, Stmt<T> body) {
        this.condition = condition;
        this.body = body;
    }

    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitWhileStmt(this);
    }
}
