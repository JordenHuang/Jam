package org.jam.nodes;

import org.jam.Token;

public class IncludeNode<T> extends Stmt<T> {
    public final Token filename;

    public IncludeNode(Token filename) {
        this.filename = filename;
    }

    @Override
    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitIncludeStmt(this);
    }
}
