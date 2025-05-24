package org.jam.nodes;

import org.jam.Token;

public class VarNode<T> extends Stmt<T> {
    public Token name;
    public String typeName;
    public Expr<T> initializer;

    public VarNode(Token name, String typeName, Expr<T> initializer) {
        this.name = name;
        this.typeName = typeName;
        this.initializer = initializer;
    }

    @Override
    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitVarStmt(this);
    }
}