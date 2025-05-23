package org.jam;

import java.util.Map;

public class Renderer {
    public String render(String templateText, Map<String, Object> context) {
        // 1. Tokenize
        // 2. 解析 if/for/while 的邏輯區塊
        // 3. 替換 {{ variable }} 為 context 中對應值
        // 4. 執行區塊邏輯，產出結果
        Lexer lexer = new Lexer(templateText);
//        String token = lexer.getNextToken();
//        while (token != null) {
//            System.out.println("'" + token + "'");
//            token = lexer.getNextToken();
//        }
        return null;
    }
}
