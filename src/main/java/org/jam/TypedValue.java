package org.jam;

public class TypedValue {
    private final Object value;
    private final String typeName;

    public TypedValue(Object value, String typeName) {
        this.value = value;
        this.typeName = typeName;
    }

    public Object getValue() {
        return value;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isInteger() {
        return "Integer".equals(typeName);
    }

    public boolean isDouble() {
        return "Double".equals(typeName);
    }

    public boolean isNumeric() {
        return isInteger() || isDouble();
    }

    public int asInt() {
        if (value instanceof Number) {
            return ((Number)value).intValue();
        }
        throw new RuntimeException("Cannot convert to int: " + value);
    }

    public double asDouble() {
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        }
        throw new RuntimeException("Cannot convert to double: " + value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}