package org.jam;

public class Main {
    public static void main(String[] args) {
        Jam jam = new Jam();
        String basePath = "src/main/templates/";
        String templateFileName = "9x9.template";
        jam.renderTemplate(basePath.concat(templateFileName));
    }
}