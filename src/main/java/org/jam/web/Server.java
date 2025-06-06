package org.jam.web;
//package org.;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.jam.outputType.IOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * SimpleHttpServer: an IOutput implementation that directly serves HTML content via HTTP.
 */
public class Server extends IOutput {

    private int PORT = 8000;
    private byte[] htmlBytes;

    @Override
    public void write(byte[] content) {
        if (content == null || content.length == 0) {
            System.err.println("content is empty.");
            return;
        }
        this.htmlBytes = content;
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new HtmlHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server at http://localhost:" + PORT + "/");
    }

    private class HtmlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, htmlBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(htmlBytes);
            }
        }
    }
}
