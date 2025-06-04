package org.jam;

import org.jam.outputType.FileOutput;
import org.jam.outputType.IOutput;
import org.jam.outputType.StandardOutput;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UnknownFormatConversionException;

public class Main {
    public static void main(String[] args) {
        String basePath = "src/main/templates/";
//        String templateFileName = "9x9.jam";
        String templateFileName = "features.jam";

        Jam jam = new Jam();
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
        // 注意：故意不添加 backgroundColor, theme, logoUrl, customMessage, showClasses
        // 來測試 ifDefine 的預設值功能
        String[] ss = new String[10];
        Student[] sts = new Student[10];
        for (int i = 0; i < ss.length; ++i) {
            ss[i] = Integer.toString(i);
            sts[i] = new Student(i, new String(new char[]{(char)(i+65), (char)(i+65), (char)(i+65), (char)(i+65)}));
        }
        context.put("student", new Student(1111, "John"));
        context.put("ss", ss);
        context.put("sts", sts);

        jam.renderTemplate(basePath.concat(templateFileName), outputType, context);

    }
}

// For testing
class Student {
    public int id;
    public String name;
    public String[] classes;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
        this.classes = new String[] {
                "Chinese",
                "English",
                "Math",
                "Science",
        };
    }
}