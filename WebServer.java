import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer extends Thread{
    public static void main(String[] args) throws Exception {
            HttpServer server = HttpServer.create();
            server.bind(new InetSocketAddress(80), 0);
            HttpContext context = server.createContext("/", new MyHandler());
            server.setExecutor(null);
            server.start();
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            builder.append("<h1>Hell eah!!! ").append("</h1>");

            byte[] bytes = builder.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

}
