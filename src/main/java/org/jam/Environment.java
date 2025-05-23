package org.jam;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing;
    private final Map<String, EnvironmentField> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public EnvironmentField get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public void assign(Token name, Object value, String typeName) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, new EnvironmentField(value, typeName));
            return;
        }

        if (enclosing != null) {
            EnvironmentField env = enclosing.get(name);
            enclosing.assign(name, value, env.getTypeName());
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value, String typeName) {
        values.put(name, new EnvironmentField(value, typeName));
    }
}
