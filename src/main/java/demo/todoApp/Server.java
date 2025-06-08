package demo.todoApp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

import org.jam.outputType.IOutput;
import org.jam.Jam;

/**
 * SimpleHttpServer: an IOutput implementation that directly serves HTML content via HTTP.
 */
public class Server {
    private int PORT = 8000;
    private byte[] htmlBytes;
    private String templatePath = "src/main/templates/todoApp/todo.jam";

    class htmlOutput extends IOutput {
        @Override
        public void write(byte[] content) {
            if (content == null || content.length == 0) {
                System.err.println("content is empty.");
                htmlBytes = "<h1>[ERROR] Fail to render template</h1>".getBytes();
                return;
            }
            htmlBytes = content;
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServer() throws IOException {
        // Template renderer
        Jam jam = new Jam();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // root
        server.createContext("/", exchange -> {
            System.out.printf("[%s] from %s, at %s\n", exchange.getRequestMethod(), exchange.getRemoteAddress(), LocalTime.now());
            if (exchange.getRequestMethod().equals("GET")) {
                ArrayList<Todo> file = SaveFile.readFile();
//                System.out.println(file);

                Map<String, Object> context = new HashMap<>();
                context.put("todo", file);
                jam.renderTemplate(templatePath, new htmlOutput(), context);
                sendResponse(exchange, htmlBytes);
            } else {
                // 405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        });

        // API call
        server.createContext("/msg", exchange -> {
            System.out.printf("[%s] from %s, at %s\n", exchange.getRequestMethod(), exchange.getRemoteAddress(), LocalTime.now());
            if (exchange.getRequestMethod().equals("POST")) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);

//                記得關閉
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    body.append(line);
                }
//                System.out.println("body : " + body);

                // decode request body to Todo
                SaveFile.writeFile(decoder(body.toString()));

                ArrayList<Todo> file = SaveFile.readFile();
//                System.out.println(file);
                Map<String, Object> context = new HashMap<>();
                context.put("todo", file);
                jam.renderTemplate(templatePath, new htmlOutput(), context);
//                System.out.println(Arrays.toString(htmlBytes));
                sendResponse(exchange, htmlBytes);
            } else {
                // 405 Method Not Allowed
                exchange.sendResponseHeaders(405, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server at http://localhost:" + PORT + "/");
    }

    private void sendResponse(HttpExchange exchange, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
//            System.out.println("Sended");
            os.write(data);
        }
    }


    private Todo decoder(String file) {
//        System.out.println("Try to decode " + file);
        // split the string by '&'
        String[] pairs = file.split("&");
        String type = new String();
        ArrayList<String> data = new ArrayList<>();

        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            // get value
            data.add(parts[1]);
        }
        type = data.get(0);
        // 分辨 資料更新 / 新增
        if (type.equals("toggle")) {
//            System.out.println(data);
            Todo todo = new Todo("", "", "");
            todo.setFinish();
            todo.setPrimary_key(Integer.parseInt(data.get(1)));
            // System.out.println(todo.primary_key);
            return todo;
        } else {
//            System.out.println(data);
            Todo todo = new Todo(data.get(0), data.get(1), data.get(2));

            // System.out.println("finish decoder");
            return todo;
        }
    }
}
