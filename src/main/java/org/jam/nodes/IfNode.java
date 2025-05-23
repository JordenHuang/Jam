package org.jam.nodes;

public class IfNode<T> extends Stmt<T> {
    public final Expr<T> condition;
    public final Stmt<T> thenBranch;
    public final Stmt<T> elseBranch;

    public IfNode(Expr<T> condition, Stmt<T> thenBranch, Stmt<T> elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    @Override
    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitIfStmt(this);
    }
}
