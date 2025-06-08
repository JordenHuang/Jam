package org.jam.nodes;

public abstract class Stmt<T> {
    public abstract T accept(StmtVisitor<T> visitor);
}