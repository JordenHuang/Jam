package org.jam;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Jam jam = new Jam();
        String basePath = "src/main/templates/";
        String templateFileName = "9x9.jam";
        Map<String, Object> context = new HashMap<>();
        context.put("title", "9x9 table");
        jam.renderTemplate(basePath.concat(templateFileName), context);

//        String templateFileName = "test.jam";
//        jam.renderTemplate(basePath.concat(templateFileName), context);
    }
}