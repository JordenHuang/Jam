package org.jam.web;
//package org.;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import org.jam.DB.Todo;
import org.jam.outputType.IOutput;
import org.jam.DB.SaveFile;
import org.jam.Jam;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.rmi.UnexpectedException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Integer.parseInt;


/**
 * SimpleHttpServer: an IOutput implementation that directly serves HTML content via HTTP.
 */
public class Server {

    int a = 0;
    Jam jam;
    String basePath = "src/main/templates/";
    String templateFileName = "todo.jam";

    private int PORT = 8000;
    private byte[] htmlBytes;
    Server server;
    class htmlOutput extends IOutput {
        @Override
        public void write(byte[] content) {
            if (content == null || content.length == 0) {
                System.err.println("content is empty.");
                return;
            }
            htmlBytes = content;
        }
    }

//    @Override
//    public void write(byte[] content) {
//        if (content == null || content.length == 0) {
//            System.err.println("content is empty.");
//            return;
//        }
//        this.htmlBytes = content;
////        try {
////            startServer();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//    }


    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServer() throws IOException {
        jam = new Jam();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new HtmlHandler());
        server.createContext("/msg", new HtmlHandler());
//        server.createContext("/", exchange -> {
//            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
//                String data = "hello world";
//                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
//                exchange.sendResponseHeaders(200, data.length());
//                    OutputStream os = exchange.getResponseBody();
//                    System.out.println("Sended");
//                    os.write(data.getBytes());
//                    os.close();
//            }
//        });
        server.setExecutor(null);
        server.start();
        System.out.println("Server at http://localhost:" + PORT + "/");
    }

    private class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                System.out.println("ASD");
                ArrayList<Todo> file = SaveFile.readFile();
                System.out.println(file);
//        SaveFile saveFile = new SaveFile();
                Map<String, Object> context = new HashMap<>();
                context.put("todo", file);
                jam.renderTemplate(basePath.concat(templateFileName), new htmlOutput(), context);
                sendResponse(exchange, htmlBytes);

            } else if (method.equalsIgnoreCase("POST")) {
//                  if change or add todo
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                System.out.print(a++);

                System.out.println(br.toString());

//                System.out.println(br.readLine());
//                記得關閉
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null){
                    body.append(line);
                }
                System.out.println("body : " + body);

                SaveFile.writeFile(decoder(body.toString()));

                ArrayList<Todo> file = SaveFile.readFile();
                System.out.println(file);
                Map<String, Object> context = new HashMap<>();
                context.put("todo", file);
                jam.renderTemplate(basePath.concat(templateFileName), new htmlOutput(), context);
                System.out.println(Arrays.toString(htmlBytes));
                sendResponse(exchange, htmlBytes);

            } else {
                // 不支援
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            }
        }

        private void sendResponse(HttpExchange exchange, byte[] data) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, data.length);
            String a = "kjl";
            try (OutputStream os = exchange.getResponseBody()) {
                System.out.println("Sended");
                os.write(data);
            }
        }



        private Todo decoder(String file) {
            System.out.println("Try to decode " + file);
            String[] pairs = file.split("&");
            String type = new String();
            ArrayList<String> data = new ArrayList<>();
//            System.out.println("finish decoder");
            for (String pair : pairs) {
                String[] parts = pair.split("=", 2);

                data.add(parts[1]);
//                System.out.println("finish decoder");
            }
            type = data.get(0);
//            data.remove(0);
            if(type.equals("toggle")){
                System.out.println(data);
                Todo todo = new Todo("","","");
                todo.setFinish();
                todo.setPrimary_key(parseInt(data.get(1)));
//            Todo todo = new Todo("Fuck", "fuck");
                System.out.println(todo.primary_key);
                return todo;
            }
            else{
                System.out.println(data);
                Todo todo = new Todo(data.get(0),data.get(1),data.get(2));
//            Todo todo = new Todo("Fuck", "fuck");
                System.out.println("finish decoder");
                return todo;
            }
        }



    }
}
