package org.jam.nodes;

import java.util.List;

public class BlockNode<T> extends Stmt<T> {
    public final List<Stmt<T>> statements;

    public BlockNode(List<Stmt<T>> statements) {
        this.statements = statements;
    }

    @Override
    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitBlockStmt(this);
    }
}
