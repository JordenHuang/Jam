package org.jam.nodes;

public class HtmlNode<T> extends Stmt<T> {
    public final Expr<T> expr;

    public HtmlNode(Expr<T> expr) {
        this.expr = expr;
    }

    public T accept(StmtVisitor<T> visitor) {
        return visitor.visitHtmlStmt(this);
    }
}
