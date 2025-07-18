package org.jam.nodes;

public interface ExprVisitor<T> {
    T visitUnaryNode(UnaryNode<T> expr);
    T visitBinaryNode(BinaryNode<T> expr);
    T visitLiteralNode(LiteralNode<T> expr);
    T visitGroupingNode(GroupingNode<T> expr);
    T visitVariableExpr(VariableNode<T> expr);
    T visitAssignmentExpr(AssignmentNode<T> expr);
    T visitLogicalExpr(LogicalNode<T> expr);
    T visitGetExpr(GetNode<T> expr);
    T visitArrayAccessExpr(ArrayAccessNode<T> expr);
    T visitFunctionCallExpr(FunctionCallNode<T> expr);
}