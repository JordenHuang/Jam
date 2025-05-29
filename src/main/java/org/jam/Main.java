package org.jam;

import org.jam.outputType.FileOutput;
import org.jam.outputType.IOutput;
import org.jam.outputType.StandardOutput;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String basePath = "src/main/templates/";
        String templateFileName = "9x9.jam";

        Jam jam = new Jam();
//        IOutput outputType = new StandardOutput();
        IOutput outputType;
        try {
             outputType = new FileOutput(basePath + "9x9.html");
        } catch (IOException e) {
            outputType = null;
            e.printStackTrace();
        }

        Map<String, Object> context = new HashMap<>();
        context.put("title", "9x9 table");
        context.put("fontSize", 20);
        context.put("student", new Student(1111, "John"));

        jam.renderTemplate(basePath.concat(templateFileName), outputType, context);

    }
}

// For testing
class Student {
    public int id;
    public String name;
    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }
}