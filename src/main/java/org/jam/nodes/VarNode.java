package org.jam.nodes;

import org.jam.Token;
import org.jam.TokenType;


public class VarNode<T> extends Stmt<T> {
    public Token name;
    public TokenType tokenType;

    public Expr<T> initializer;

    public VarNode(Token name, TokenType tokenType, Expr<T> initializer) {
        this.name = name;
        this.tokenType = tokenType;
        this.initializer = initializer;
    }

    @Override
    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitVarStmt(this);
    }
}
