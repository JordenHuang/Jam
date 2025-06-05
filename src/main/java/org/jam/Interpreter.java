package org.jam;

import org.jam.nodes.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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
        reporter.log("visit unary node");
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
        reporter.log("visit binary node");
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
            case PERCENT:
                checkNumberOperands(expr.operator, left, right);
                return new TypedValue(left.asInt() % right.asInt(), "Integer");
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
        reporter.log("visit literal node");
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
        reporter.log("visit logical node");
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
        reporter.log("visit grouping node");
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVariableExpr(VariableNode<Object> expr) {
        reporter.log("visit variable node");
        EnvironmentField field = environment.get(expr.name);
        return new TypedValue(field.getValue(), field.getTypeName());
    }

    @Override
    public Object visitAssignmentExpr(AssignmentNode<Object> expr) {
        reporter.log("visit assignment node");
        EnvironmentField env = environment.get(expr.name);
        Object targetValueObj = env.getValue();
        Object valueObj = evaluate(expr.value);
        TypedValue value = convertToTypedValue(valueObj);

        TypedValue left = convertToTypedValue(targetValueObj);
        TypedValue right = convertToTypedValue(valueObj);
        switch (expr.operator.type) {
            case PLUS_EQUAL:
                if (left.isNumeric() && right.isNumeric()) {
                    if (left.isInteger() && right.isInteger()) {
                        value = convertToTypedValue(left.asInt() + right.asInt());
                    } else {
                        value = convertToTypedValue(left.asDouble() + right.asDouble());
                    }
                }
                if (left.getValue() instanceof String && right.getValue() instanceof String) {
                    value = new TypedValue((String)left.getValue() + (String)right.getValue(), "String");
                }
                break;
            case MINUS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left.isInteger() && right.isInteger()) {
                    value = convertToTypedValue(left.asInt() - right.asInt());
                } else {
                    value = convertToTypedValue(left.asDouble() - right.asDouble());
                }
                break;
            case STAR_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left.isInteger() && right.isInteger()) {
                    value = convertToTypedValue(left.asInt() * right.asInt());
                } else {
                    value = convertToTypedValue(left.asDouble() * right.asDouble());
                }
                break;
            case SLASH_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left.isInteger() && right.isInteger()) {
                    value = convertToTypedValue(left.asInt() / right.asInt());
                } else {
                    value = convertToTypedValue(left.asDouble() / right.asDouble());
                }
                break;
            case PERCENT_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                if (left.isInteger() && right.isInteger()) {
                    value = convertToTypedValue(left.asInt() % right.asInt());
                } else {
                    value = convertToTypedValue(left.asDouble() % right.asDouble());
                }
            break;
            case EQUAL:
            default:
                value = convertToTypedValue(valueObj);
                break;
        }

        // Variable name, value, and type name
        environment.assign(expr.name, value.getValue(), env.getTypeName());
        return value;
    }

    @Override
    public Object visitGetExpr(GetNode<Object> expr) {
        reporter.log("visit get node");
        Object objectObj = evaluate(expr.object);
        String fieldName = expr.name.lexeme;

        // Special case for arrays - handle length property
        if (fieldName.equals("length")) {
            if (objectObj instanceof TypedValue) {
                TypedValue typedValue = (TypedValue)objectObj;
                Object object = typedValue.getValue();

                if (object instanceof List) {
                    return new TypedValue(((List<?>)object).size(), "Integer");
                } else if (object instanceof Object[]) {
                    return new TypedValue(((Object[])object).length, "Integer");
                } else if (object instanceof String) {
                    return new TypedValue(((String)object).length(), "Integer");
                }
            }
        }

        // Handle TypedValue objects
        if (objectObj instanceof TypedValue) {
            TypedValue typedValue = (TypedValue)objectObj;
            Object object = typedValue.getValue();

            // Case 1: Map
            if (object instanceof Map) {
                Map<String, Object> map = (Map<String, Object>)object;
                if (map.containsKey(fieldName)) {
                    Object value = map.get(fieldName);
                    return new TypedValue(value, getTypeNameForValue(value));
                }
                throw new RuntimeError(expr.name, "Undefined property '" + fieldName + "'.");
            }

            // Case 2: JavaBean - try getter
            try {
                // e.g., "name" -> "getName"
                String methodName = "get" + capitalize(fieldName);
                Method method = object.getClass().getMethod(methodName);
                Object result = method.invoke(object);
                return new TypedValue(result, getTypeNameForValue(result));
            } catch (NoSuchMethodException ignored) {
                // Fall through to try field directly
            } catch (Exception e) {
                throw new RuntimeException("Failed to access getter for field '" + expr.name.lexeme + "': " + e.getMessage());
            }
            // Case 3: Try direct public field access
            try {
                Field field = object.getClass().getField(fieldName);
                Object result = field.get(object);
                return new TypedValue(result, getTypeNameForValue(result));
            } catch (NoSuchFieldException e) {
                throw new RuntimeError(expr.name, "Field '" + fieldName + "' not found in object of type " + object.getClass().getSimpleName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to access field '" + fieldName + "': " + e.getMessage());
            }
        } else {
            // Handle regular objects (non-TypedValue)
            Object object = objectObj;

            // Case 1: Map
            if (object instanceof Map) {
                Map<String, Object> map = (Map<String, Object>)object;
                if (map.containsKey(fieldName)) {
                    Object value = map.get(fieldName);
                    return new TypedValue(value, getTypeNameForValue(value));
                }
                throw new RuntimeError(expr.name, "Undefined property '" + fieldName + "'.");
            }

            // Case 2: JavaBean - try getter
            try {
                // e.g., "name" -> "getName"
                String methodName = "get" + capitalize(fieldName);
                Method method = object.getClass().getMethod(methodName);
                Object result = method.invoke(object);
                return new TypedValue(result, getTypeNameForValue(result));
            } catch (NoSuchMethodException ignored) {
                // Fall through to try field directly
            } catch (Exception e) {
                throw new RuntimeException("Failed to access getter for field '" + expr.name.lexeme + "': " + e.getMessage());
            }

            // Case 3: Try direct public field access
            try {
                Field field = object.getClass().getField(fieldName);
                Object result = field.get(object);
                return new TypedValue(result, getTypeNameForValue(result));
            } catch (NoSuchFieldException e) {
                throw new RuntimeError(expr.name, "Field '" + fieldName + "' not found in object of type " + object.getClass().getSimpleName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to access field '" + fieldName + "': " + e.getMessage());
            }
        }
    }

    @Override
    public Object visitArrayAccessExpr(ArrayAccessNode<Object> expr) {
        Object arrayObj = evaluate(expr.array);
        Object indexObj = evaluate(expr.index);

        TypedValue array = convertToTypedValue(arrayObj);
        TypedValue index = convertToTypedValue(indexObj);

        if (array.getValue() == null) {
            throw new RuntimeError(expr.bracket, "Cannot access elements of null.");
        }

        // Handle array access for different types
        if (array.getValue() instanceof List) {
            List<?> list = (List<?>) array.getValue();
            int idx = index.asInt();

            if (idx < 0 || idx >= list.size()) {
                throw new RuntimeError(expr.bracket, "Array index out of bounds: " + idx);
            }

            Object value = list.get(idx);
            return new TypedValue(value, getTypeNameForValue(value));
        }
        else if (array.getValue() instanceof Object[]) {
            Object[] objArray = (Object[]) array.getValue();
            int idx = index.asInt();

            if (idx < 0 || idx >= objArray.length) {
                throw new RuntimeError(expr.bracket, "Array index out of bounds: " + idx);
            }

            Object value = objArray[idx];
            return new TypedValue(value, getTypeNameForValue(value));
        }
        else if (array.getValue() instanceof String) {
            String str = (String) array.getValue();
            int idx = index.asInt();

            if (idx < 0 || idx >= str.length()) {
                throw new RuntimeError(expr.bracket, "String index out of bounds: " + idx);
            }

            return new TypedValue(String.valueOf(str.charAt(idx)), "String");
        }
        else if (array.getValue() instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) array.getValue();
            Object key = index.getValue();

            if (!map.containsKey(key)) {
                throw new RuntimeError(expr.bracket, "Key not found in map: " + key);
            }

            Object value = map.get(key);
            return new TypedValue(value, getTypeNameForValue(value));
        }

        throw new RuntimeError(expr.bracket, "Cannot use array access on type: " + array.getTypeName());
    }

    @Override
    public Void visitBlockStmt(BlockNode<Void> stmt) {
        reporter.log("visit block node");
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(ExpressionNode<Void> stmt) {
        reporter.log("visit expression node");
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(IfNode<Void> stmt) {
        reporter.log("visit if node");
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
        reporter.log("visit var node");
        Object value = null;
        if (stmt.initializer != null) {
            Object valueObj = evaluate(stmt.initializer);
            TypedValue typedValue = convertToTypedValue(valueObj);
            value = typedValue.getValue();
        }
        environment.define(stmt.name, value, stmt.typeName);
        return null;
    }

    @Override
    public Void visitWhileStmt(WhileNode<Void> stmt) {
        reporter.log("visit while node");
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
        reporter.log("visit html node");
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

    @Override
    public Void visitIncludeStmt(IncludeNode<Void> stmt) {
        reporter.log("visit include node");
        try {
            Jam jam = new Jam(environment);
            String filename = stmt.filename.lexeme;
            filename = filename.substring(1, filename.length()-1);
            byte[] bytes = Files.readAllBytes(Paths.get(filename));
            byte[] subResult = jam.run(new String(bytes, Charset.defaultCharset()));
            result.append(new String(subResult, Charset.defaultCharset()));
        } catch (IOException e) {
            throw new RuntimeError(stmt.filename, "File not found");
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

    private String getTypeNameForValue(Object value) {
        if (value instanceof Integer) return "Integer";
        if (value instanceof Double) return "Double";
        if (value instanceof String) return "String";
        if (value instanceof Boolean) return "Boolean";
        if (value instanceof Map) return "Object";
        if (value instanceof List) return "Array";
        if (value instanceof Object[]) return "Array";
        return "Unknown_type";
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    // 新增 函數調用表達式
    @Override
    public Object visitFunctionCallExpr(FunctionCallNode<Object> expr) {
        reporter.log("visit function call node: " + expr.name.lexeme);

        String functionName = expr.name.lexeme;

        // 處理內建函數
        switch (functionName) {
            case "ifDefine":
                return handleIfDefine(expr.arguments);
            default:
                throw new RuntimeError(expr.name, "Undefined function '" + functionName + "'.");
        }
    }

    // 實作 ifDefine 函數的邏輯
    private Object handleIfDefine(List<Expr<Object>> arguments) {
        // 修改：允許接受 1 個或多個參數
        if (arguments.isEmpty()) {
            throw new RuntimeError(null, "ifDefine() expects at least 1 argument.");
        }

        // case 1：只有一個參數 - 返回 boolean（原有邏輯）
        if (arguments.size() == 1) {
            Expr<Object> arg = arguments.get(0);

            // 檢查參數是否為變數表達式
            if (!(arg instanceof VariableNode<Object> varNode)) {
                throw new RuntimeError(null, "ifDefine() argument must be a variable name.");
            }

            // 檢查變數是否在環境中定義
            try {
                environment.get(varNode.name);
                return true;  // 變數存在
            } catch (Exception e) {
                return false; // 變數不存在
            }
        }

        // 情況2：多個參數 - 返回第一個已定義變數的值，或最後一個參數（默認值）
        // {% ifDefine(var1, var2, "default value") %}
        // 檢查變數 var1 和 var2 是否已定義。如果其中一個變數已經定義，則返回該變數的值；如果兩者都未定義，則返回 "default value"
        else {
            // 檢查前 n-1 個參數（變數名稱）
            for (int i = 0; i < arguments.size() - 1; i++) {
                Expr<Object> arg = arguments.get(i);

                // 檢查是否為變數表達式
                if (!(arg instanceof VariableNode<Object> varNode)) {
                    throw new RuntimeError(null, "ifDefine() variable arguments must be variable names.");
                }

                // 檢查變數是否在環境中定義
                try {
                    Object value = environment.get(varNode.name);
                    return value;  // 返回第一個找到的變數值
                } catch (Exception e) {
                    // 繼續檢查下一個變數
                    continue;
                }
            }

            // 如果沒有找到任何已定義的變數，返回最後一個參數（默認值）
            Expr<Object> defaultValue = arguments.get(arguments.size() - 1);
            return evaluate(defaultValue);
        }


    }
}