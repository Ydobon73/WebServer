import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;

import static javax.imageio.ImageIO.read;


public class WebServer extends Thread {
    public static void main(String[] args) throws Exception {

        HttpServer server;
        InetSocketAddress inetSocketAddress = new InetSocketAddress(80);
        server = HttpServer.create();
        server.bind(inetSocketAddress,0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
    }

    static class MyHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            try {
                InputStream is = exchange.getRequestBody();
                read(is);

                String request = exchange.getRequestURI().toString();
                String path = getPath(request);

                // если из запроса не удалось выделить путь, то
                // возвращаем "400 Bad Request"
                if (path == null) {
                    // первая строка ответа
                    String response = "400 Bad Request\n";
                    exchange.sendResponseHeaders(200, 0);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }

                System.out.println(path);
                // если файл существует и является директорией,
                // то ищем индексный файл index.html
                File f = new File(path);
                boolean flag = !f.exists();
                if (!flag) if (f.isDirectory()) {
                    if (path.lastIndexOf("" + File.separator) == path.length() - 1)
                        path = path + "index.html";
                    else
                        path = path + File.separator + "index.html";
                    f = new File(path);
                    flag = !f.exists();
                }

                // если по указанному пути файл не найден
                // то выводим ошибку "404 Not Found"
                if (flag) {
                    String response = "404 Not Found\n";

                    response = response + "File " + path + " not found!";

                    exchange.sendResponseHeaders(200, 0);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    os.write(response.getBytes());
                    os.close();
                    return;
                }

                // создаём ответ

                exchange.sendResponseHeaders(200, 0);
                OutputStream os = exchange.getResponseBody();
                byte buf[] = new byte[64 * 1024];
                FileInputStream fis = new FileInputStream(path);
                int r = 1;
                while (r > 0) {
                    r = fis.read(buf);
                    if (r > 0) os.write(buf, 0, r);
                }
                fis.close();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            } // вывод исключений
        }


        protected String getPath(String request) {
            String  path;
            if (request == null) return null;
            path = request.toLowerCase();
            if (path.indexOf("http://", 0) == 0) {
                request = request.substring(7);
                request = request.substring(request.indexOf("/", 0));
            } else if (path.indexOf("/", 0) == 0)
                request = request.substring(1); // если URI начинается с символа /, удаляем его

            // конвертируем URI в путь до документов
            // предполагается, что документы лежат в папке C://www/
            path = "C:\\www" + File.separator;
            char a;
            for (int i = 0; i < request.length(); i++) {
                a = request.charAt(i);
                if (a == '/')
                    path = path + File.separator;
                else
                    path = path + a;
            }
            return path;
        }
    }
}