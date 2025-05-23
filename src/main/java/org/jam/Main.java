package org.jam;

import java.io.*;
import java.util.List;

/**
 * 1. [x] Read the file
 * 2. [ ] Tokenize
 * 3. [ ]
*/

public class Main {
    public static void main(String[] args) {
        Main m = new Main();
        Renderer renderer = new Renderer();
        String basePath = "src/main/templates/";
        String templateFileName = "9x9.template";
        StringBuilder sb = m.readFile(basePath.concat(templateFileName));
//        renderer.render(sb.toString(), null);
        Lexer lexer = new Lexer(sb.toString());
        List<Token> tokens = lexer.scanTokens();
        int i = 0;
        for (Token token : tokens) {
            System.out.printf("%d: %s\n", i, token);
            i += 1;
        }
    }

    public StringBuilder readFile(String filePath) {
//        try {
//            File fileObj = new File(filePath);
//            Scanner fileReader = new Scanner(fileObj);
//            while (fileReader.hasNextLine()) {
//                String data = fileReader.nextLine();
//                System.out.println(data);
//            }
//            fileReader.close();
//        } catch (FileNotFoundException e) {
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }

        //this is called try-with-resources, it handles closing the resources for you
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            //readLine() will return null when there are no more lines
            while (line != null) {
                //append the current line
                stringBuilder.append(line);
                stringBuilder.append("\n");
                //read the next line, will be null when there are no more
                line = reader.readLine();
            }
//            System.out.println(stringBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder;
    }
}