package demo;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;

import demo.todoApp.SaveFile;
import demo.todoApp.Todo;
import org.jam.outputType.FileOutput;
import org.jam.outputType.IOutput;
import org.jam.Jam;

/**
 * SimpleHttpServer: an IOutput implementation that directly serves HTML content via HTTP.
 */
public class Server {
    private int PORT = 8000;
    private byte[] htmlBytes;
    private String templateBasePath = "src/main/templates/";
    private Map<String, String> templates = new HashMap<>(){{
        put("9x9TableTemplate", templateBasePath + "9x9Table/9x9.jam");
        put("todoAppTemplate", templateBasePath + "todoApp/todo.jam");
    }};

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

        // 9x9 Table
        {
            server.createContext("/9x9Table", exchange -> {
                System.out.printf("[%s] from %s, at %s\n", exchange.getRequestMethod(), exchange.getRemoteAddress(), LocalTime.now());
                if (exchange.getRequestMethod().equals("GET")) {
                    Map<String, Object> context = new HashMap<>();
                    context.put("title", "9x9 table");
                    context.put("fontSize", 20);

                    // Render and send
                    jam.renderTemplate(templates.get("9x9TableTemplate"), new htmlOutput(), context);
                    sendResponse(exchange, htmlBytes);
                    // Write to file
                    jam.renderTemplate(templates.get("9x9TableTemplate"), new FileOutput(templateBasePath + "9x9Table/9x9.html"), context);
                } else {
                    // 405 Method Not Allowed
                    exchange.sendResponseHeaders(405, -1);
                }
            });
        }

        // TodoApp
        {
            server.createContext("/todoApp", exchange -> {
                System.out.printf("[%s] from %s, at %s\n", exchange.getRequestMethod(), exchange.getRemoteAddress(), LocalTime.now());
                if (exchange.getRequestMethod().equals("GET")) {
                    ArrayList<Todo> file = SaveFile.readFile();
//                System.out.println(file);

                    Map<String, Object> context = new HashMap<>();
                    context.put("todo", file);
                    jam.renderTemplate(templates.get("todoAppTemplate"), new htmlOutput(), context);
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

                    // decode request body to Todo
                    SaveFile.writeFile(decoder(body.toString()));

                    ArrayList<Todo> file = SaveFile.readFile();
                    Map<String, Object> context = new HashMap<>();
                    context.put("todo", file);
                    jam.renderTemplate(templates.get("todoAppTemplate"), new htmlOutput(), context);
                    sendResponse(exchange, htmlBytes);
                } else {
                    // 405 Method Not Allowed
                    exchange.sendResponseHeaders(405, -1);
                }
            });
        }

        server.setExecutor(null);
        server.start();
//        System.out.println("Server at http://localhost:" + PORT + "/");
        System.out.println("Server starts");
        System.out.println("URLs:");
        System.out.println("- http://localhost:" + PORT + "/9x9Table");
        System.out.println("- http://localhost:" + PORT + "/todoApp");
        System.out.println("--------------------------------------------------");
    }

    private void sendResponse(HttpExchange exchange, byte[] data) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, data.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    private Todo decoder(String file) {
        // split the string by '&'
        String[] pairs = file.split("&");
        ArrayList<String> data = new ArrayList<>();
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            // get value
            data.add(parts[1]);
        }

        String type = data.get(0);
        // 分辨 資料更新 / 新增
        Todo todo;
        if (type.equals("toggle")) {
            todo = new Todo("", "", "");
            todo.setFinish();
            todo.setPrimary_key(Integer.parseInt(data.get(1)));
        } else {
            todo = new Todo(data.get(0), data.get(1), data.get(2));
        }
        return todo;
    }
}
