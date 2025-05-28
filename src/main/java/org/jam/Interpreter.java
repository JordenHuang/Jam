package org.jam;

import org.jam.nodes.*;

import java.util.List;

public class Interpreter implements ExprVisitor<Object>, StmtVisitor<Void> {
    private Environment environment;
    private final Reporter reporter;
    private StringBuilder result;

    public Interpreter(Environment env, Reporter reporter) {
        this.environment = env;
        this.reporter = reporter;
        this.result = new StringBuilder();
    }

    public byte[] interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            reporter.runtimeError(error);
        }
        return result.toString().getBytes();
    }

    @Override
    public Object visitUnaryNode(UnaryNode<Object> expr) {
        Object rightObj = evaluate(expr.right);
        TypedValue right = convertToTypedValue(rightObj);

        switch (expr.operator.type) {
            case BANG:
                return new TypedValue(!isBoolean(right.getValue()), "Boolean");
            case MINUS:
                if (right.isInteger()) {
                    return new TypedValue(-right.asInt(), "Integer");
                } else if (right.isNumeric()) {
                    return new TypedValue(-right.asDouble(), "Double");
                } else {
                    throw new RuntimeError(expr.operator, "Operand must be a number.");
                }
        }

        // Unreachable.
        System.err.println("[ERROR] UNREACHABLE, in visitUnaryNode");
        return null;
    }

    @Override
    public Object visitBinaryNode(BinaryNode<Object> expr) {
        Object leftObj = evaluate(expr.left);
        Object rightObj = evaluate(expr.right);
        
        TypedValue left = convertToTypedValue(leftObj);
        TypedValue right = convertToTypedValue(rightObj);

        switch (expr.operator.type) {
            case PLUS:
                if (left.isNumeric() && right.isNumeric()) {
                    if (left.isInteger() && right.isInteger()) {
                        return new TypedValue(left.asInt() + right.asInt(), "Integer");
                    } else {
                        return new TypedValue(left.asDouble() + right.asDouble(), "Double");
                    }
                }

                if (left.getValue() instanceof String && right.getValue() instanceof String) {
                    return new TypedValue((String)left.getValue() + (String)right.getValue(), "String");
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                if (left.isInteger() && right.isInteger()) {
                    return new TypedValue(left.asInt() - right.asInt(), "Integer");
                } else {
                    return new TypedValue(left.asDouble() - right.asDouble(), "Double");
                }
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                if (left.isInteger() && right.isInteger()) {
                    return new TypedValue(left.asInt() * right.asInt(), "Integer");
                } else {
                    return new TypedValue(left.asDouble() * right.asDouble(), "Double");
                }
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                // Division typically returns a floating-point result
                return new TypedValue(left.asDouble() / right.asDouble(), "Double");
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return new TypedValue(left.asDouble() > right.asDouble(), "Boolean");
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return new TypedValue(left.asDouble() >= right.asDouble(), "Boolean");
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return new TypedValue(left.asDouble() < right.asDouble(), "Boolean");
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return new TypedValue(left.asDouble() <= right.asDouble(), "Boolean");
            case BANG_EQUAL: return new TypedValue(!isEqual(left.getValue(), right.getValue()), "Boolean");
            case EQUAL_EQUAL: return new TypedValue(isEqual(left.getValue(), right.getValue()), "Boolean");
        }

        // Unreachable.
        System.err.println("[ERROR] UNREACHABLE, in visitBinaryNode");
        return null;
    }

    @Override
    public Object visitLiteralNode(LiteralNode<Object> expr) {
        reporter.log("[INFO in Interpreter] Literal, value: " + expr.value);
        if (expr.value instanceof Number) {
            if (expr.value instanceof Integer) {
                return new TypedValue(expr.value, "Integer");
            } else {
                return new TypedValue(expr.value, "Double");
            }
        } else if (expr.value instanceof String) {
            return new TypedValue(expr.value, "String");
        } else if (expr.value instanceof Boolean) {
            return new TypedValue(expr.value, "Boolean");
        }
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(LogicalNode<Object> expr) {
        Object leftObj = evaluate(expr.left);
        TypedValue left = convertToTypedValue(leftObj);

        if (expr.operator.type == TokenType.OR) {
            if (isBoolean(left.getValue())) return left;
        } else {
            if (!isBoolean(left.getValue())) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitGroupingNode(GroupingNode<Object> expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVariableExpr(VariableNode<Object> expr) {
        EnvironmentField field = environment.get(expr.name);
        reporter.log("[INFO in Interpreter] Variable, value: " + field.getValue() + ", " + expr.name);
        return new TypedValue(field.getValue(), field.getTypeName());
    }

    @Override
    public Object visitAssignmentExpr(AssignmentNode<Object> expr) {
        EnvironmentField env = environment.get(expr.name);
        Object valueObj = evaluate(expr.value);
        TypedValue value = convertToTypedValue(valueObj);
        
        // Variable name, value, and type name
        reporter.log("[INFO in Interpreter]typename: " + env.getTypeName() + " " + value.getValue() + " " + expr.name);
        environment.assign(expr.name, value.getValue(), env.getTypeName());
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
        Object condObj = evaluate(stmt.condition);
        TypedValue cond = convertToTypedValue(condObj);
        
        if (isBoolean(cond.getValue())) {
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
            Object valueObj = evaluate(stmt.initializer);
            TypedValue typedValue = convertToTypedValue(valueObj);
            value = typedValue.getValue();
            reporter.log("[INFO in Interpreter] in visitVarStmt, value is: " + value + " " + stmt.typeName);
        }
        environment.define(stmt.name, value, stmt.typeName);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileNode<Void> stmt) {
        while (true) {
            Object condObj = evaluate(stmt.condition);
            TypedValue cond = convertToTypedValue(condObj);
            if (!isBoolean(cond.getValue())) break;
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Void visitHtmlStmt(HtmlNode<Void> stmt) {
        Object valueObj = evaluate(stmt.expr);
        TypedValue value = convertToTypedValue(valueObj);
        
        String typeName = value.getTypeName();
        if (typeName.equals("Identifier")) {
            // TODO: check this
            System.out.print("Identifier: ");
            result.append(value.getValue());
        } else if (typeName.equals("Integer")) {
            result.append(value.asInt());
        } else if (typeName.equals("Double")) {
            result.append(value.asDouble());
        } else if (typeName.equals("String")) {
            result.append(value.getValue());
        } else {
            result.append(value.getValue());
        }
        return null;
    }

    // Helper function
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

    private void checkNumberOperands(Token operator, TypedValue left, TypedValue right) {
        if (left.isNumeric() && right.isNumeric()) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }
    
    private TypedValue convertToTypedValue(Object obj) {
        if (obj instanceof TypedValue) {
            return (TypedValue)obj;
        } else if (obj instanceof EnvironmentField) {
            EnvironmentField field = (EnvironmentField)obj;
            return new TypedValue(field.getValue(), field.getTypeName());
        } else if (obj instanceof Number) {
            if (obj instanceof Integer) {
                return new TypedValue(obj, "Integer");
            } else {
                return new TypedValue(obj, "Double");
            }
        } else if (obj instanceof String) {
            return new TypedValue(obj, "String");
        } else if (obj instanceof Boolean) {
            return new TypedValue(obj, "Boolean");
        } else {
            return new TypedValue(obj, "Object");
        }
    }
}