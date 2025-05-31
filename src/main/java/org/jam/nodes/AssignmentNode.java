package org.jam.nodes;

import org.jam.*;

public class AssignmentNode<T> extends Expr<T> {
    public final Token name;
    public final Expr<T> value;
    public final Token operator;

    public AssignmentNode(Token name, Expr<T> value) {
        this.name = name;
        this.value = value;
        this.operator = new Token(TokenType.EQUAL, "=", null, name.line);
    }

    public AssignmentNode(Token name, Token operator, Expr<T> value) {
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    public AssignmentNode(Expr<T> target, Token operator, Expr<T> value) {
        if (target instanceof VariableNode) {
            this.name = ((VariableNode<T>)target).name;
        } else if (target instanceof GetNode<T>){
            this.name = ((GetNode<T>)target).name;
        } else {
            this.name = null;
        }
        this.value = value;
        this.operator = operator;
    }

    @Override
    public T accept(ExprVisitor<T> visitor) {
        return visitor.visitAssignmentExpr(this);
    }
}