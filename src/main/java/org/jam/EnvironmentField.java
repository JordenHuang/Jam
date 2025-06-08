package org.jam;

public class EnvironmentField {
    private Object value;
    private String typeName;

    public EnvironmentField(Object value, String typeName) {
        this.value = value;
        this.typeName = typeName;
    }

    public Object getValue() {
        return value;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
