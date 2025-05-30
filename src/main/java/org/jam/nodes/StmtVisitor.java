package org.jam.nodes;

public interface StmtVisitor<T> {
    T visitExpressionStmt(ExpressionNode<T> stmt);
    T visitVarStmt(VarNode<T> stmt);
    T visitBlockStmt(BlockNode<T> stmt);
    T visitIfStmt(IfNode<T> stmt);
    T visitWhileStmt(WhileNode<T> stmt);
    T visitHtmlStmt(HtmlNode<T> stmt);
    T visitIncludeStmt(IncludeNode<T> stmt);
}
