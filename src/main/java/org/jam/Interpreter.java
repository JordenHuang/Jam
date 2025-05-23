package org.jam;

import org.jam.nodes.*;

import java.util.List;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
    private Environment environment = new Environment();
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Jam.runtimeError(error);
        }
    }

    @Override
    public Object visitUnaryNode(UnaryNode<Object> expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isBoolean(right);
            case MINUS:
//                if (expr.right instanceof LiteralNode<Object>) {
//                    if (((LiteralNode)expr).value)
//                }
//                NumberType nt = checkNumberOperand(expr.operator, right);
//                if (nt == NumberType.INT) return -(int)right;
//                else return -(double)right;
//                if (right instanceof Integer) {
//                    if (((VariableNode) expr.right).tokenType == TokenType.INT) {
//                        return -(int)right;
//                    } else {
//                        return -(double)right;
//                    }
//                }
                if (right instanceof EnvironmentField) {
                    return -(double)(((EnvironmentField) right).getValue());
                }
                else
                    return -(double)right;
        }

        // Unreachable.
        System.out.println("[ERROR] UNREACHABLE, in visitUnaryNode");
        return null;
    }

    @Override
    public Object visitBinaryNode(BinaryNode<Object> expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        if (left instanceof EnvironmentField) {
            left = (double)(((EnvironmentField) left).getValue());
        }
        if (right instanceof EnvironmentField) {
            right = (double)(((EnvironmentField) right).getValue());
        }

        switch (expr.operator.type) {
            case PLUS:
//                if (left instanceof Integer && right instanceof Integer) {
//                    return (int)left + (int)((Integer) right).doubleValue();
//                }
//                else if (left instanceof Integer && right instanceof Double) {
//                    return (Integer) left + (Double) right;
//                }
//                else if (left instanceof Double && right instanceof Integer) {
//                    return (Double) left + (Integer) right;
//                }
//                else if (left instanceof Double && right instanceof Double) {
//                    return (Double) left + (Double) right;
//                }

                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case MINUS:
                // TODO: Maybe create a function that checks type and operate them
                // Like: numberOperate(left, right, operator)
                // And also remove the NumberType
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }

        // Unreachable.
        System.out.println("[ERROR] UNREACHABLE, in visitBinaryNode");
        return null;
    }

    @Override
    public Object visitLiteralNode(LiteralNode<Object> expr) {
        Jam.log("[INFO in Interpreter] Literal, value: " + expr.value);
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(LogicalNode<Object> expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isBoolean(left)) return left;
        } else {
            if (!isBoolean(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitGroupingNode(GroupingNode<Object> expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVariableExpr(VariableNode<Object> expr) {
        Jam.log("[INFO in Interpreter] Variable, value: " + environment.get(expr.name).getValue() + ", " + expr.name);
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignmentExpr(AssignmentNode<Object> expr) {
        EnvironmentField env = environment.get(expr.name);
        Object value = evaluate(expr.value);
        // Variable name, value, and type name
        Jam.log("[INFO in Interpreter]typename: " + env.getTypeName() + " " + value + " " + expr.name);
        environment.assign(expr.name, value, env.getTypeName());
        return value;
    }

    @Override
    public Void visitBlockStmt(BlockNode<Void> stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionNode<Void> stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(IfNode<Void> stmt) {
        if (isBoolean(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(VarNode<Void> stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
            Jam.log("[INFO in Interpreter] in visitVarStmt, value is: " + value + " " + Token.convertTypeToString(stmt.tokenType));
        }
        environment.define(stmt.name.lexeme, value, Token.convertTypeToString(stmt.tokenType));
//        if (value != null && stmt.tokenType == TokenType.INT) {
//            environment.define(stmt.name.lexeme, ((int)value));
//        } else if (value != null && stmt.tokenType == TokenType.DOUBLE) {
//            environment.define(stmt.name.lexeme, (double)value);
//        } else {
//            environment.define(stmt.name.lexeme, value);
//        }
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileNode<Void> stmt) {
        while (isBoolean(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitHtmlStmt(HtmlNode<Void> stmt) {
        Object value = evaluate(stmt.expr);
        if (value instanceof EnvironmentField) {
            String typeName = ((EnvironmentField) value).getTypeName();
//            System.out.println("typeName: " + typeName);
            if (typeName.equals("Identifier")) {
                System.out.print("Identifier: ");
                System.out.print(((EnvironmentField) value).getValue());
                System.out.println("[info] ident");
            } else if (typeName.equals("Int_type")) {
                Number numberValue = ((Number)((EnvironmentField) value).getValue());
                System.out.println("[info] int");
                System.out.print(numberValue.intValue());
            } else if (typeName.equals("Double_type")) {
                Number numberValue = ((Number)((EnvironmentField) value).getValue());
                System.out.println("[info] double");
                System.out.print(numberValue.doubleValue());
            } else {
                System.out.println("[info] else");
                System.out.print((String) ((EnvironmentField) value).getValue());
            }
        } else {
            Jam.log("[INFO in Interpreter] in visitHtmlStmt, value is not instance of EnvironmentField");
            System.out.println("[info] value");
            System.out.print(value);
        }
        return null;
    }


    // Helper function
    /* TODO: Check this "Raw use of parameterized class 'Expr'" */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    void executeBlock(List<Stmt<Void>> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt<Void> statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean isBoolean(Object object) {
        if (object instanceof Boolean) return (boolean)object;
        else return false;
    }

    private boolean isEqual(Object a, Object b) {
//        if (a == null && b == null) return true;
//        if (a == null) return false;

        // This if equals the above two ifs
        if (a == null) {
            return b == null;
        }

        if (a instanceof String) return false;

        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Integer) return;
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
//        if (left instanceof Double || left instanceof EnvironmentField) {
//            if (right instanceof Double || right instanceof EnvironmentField) return;
//            return;
//        }
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Number) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

//    private Object ...(){}
}
