import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class WebServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(80), 0);

        HttpContext context = server.createContext("/", new EchoHandler());
        context.setAuthenticator(new Auth());

        server.setExecutor(null);
        server.start();



    }

    static class EchoHandler implements HttpHandler {
        @Override
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

    static class Auth extends Authenticator {
        @Override
        public Result authenticate(HttpExchange httpExchange) {
            if ("/forbidden".equals(httpExchange.getRequestURI().toString()))
                return new Failure(403);
            else
                return new Success(new HttpPrincipal("c0nst", "realm"));
        }
    }
}
