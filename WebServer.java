import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

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
                    String response = "HTTP/1.1 400 Bad Request\n";

                    // дата в GMT
                    DateFormat df = DateFormat.getTimeInstance();
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    response = response + "Date: " + df.format(new Date()) + "\n";

                    // остальные заголовки
                    response = response
                            + "Connection: close\n"
                            + "Server: SimpleWEBServer\n"
                            + "Pragma: no-cache\n\n";

                    // выводим данные:
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                    //os.write(response.getBytes());

                    // завершаем соединение
                    os.close();

                    // выход
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
                    // первая строка ответа
                    String response = "HTTP/1.1 404 Not Found\n";

                    // дата в GMT
                    DateFormat df = DateFormat.getTimeInstance();
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    response = response + "Date: " + df.format(new Date()) + "\n";

                    // остальные заголовки
                    response = response
                            + "Content-Type: text/plain\n"
                            + "Connection: close\n"
                            + "Server: SimpleWEBServer\n"
                            + "Pragma: no-cache\n\n";

                    // и гневное сообщение
                    response = response + "File " + path + " not found!";

                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                   // выводим данные:
                    os.write(response.getBytes());

                    // завершаем соединение
                    os.close();

                    // выход
                    return;
                }

                // определяем MIME файла по расширению
                // MIME по умолчанию - "text/plain"
                String mime = "text/plain";

                // выделяем у файла расширение (по точке)
                int r = path.lastIndexOf(".");
                if (r > 0) {
                    String ext = path.substring(r);
                    if (ext.equalsIgnoreCase("html"))
                        mime = "text/html";
                    else if (ext.equalsIgnoreCase("htm"))
                        mime = "text/html";
                    else if (ext.equalsIgnoreCase("gif"))
                        mime = "image/gif";
                    else if (ext.equalsIgnoreCase("jpg"))
                        mime = "image/jpeg";
                    else if (ext.equalsIgnoreCase("jpeg"))
                        mime = "image/jpeg";
                    else if (ext.equalsIgnoreCase("bmp"))
                        mime = "image/x-xbitmap";
                }

                // создаём ответ

                // первая строка ответа
                String response = "HTTP/1.1 200 OK\n";

                // дата создания в GMT
                DateFormat df = DateFormat.getTimeInstance();
                df.setTimeZone(TimeZone.getTimeZone("GMT"));

                // время последней модификации файла в GMT
                response = response + "Last-Modified: " + df.format(new Date(f.lastModified())) + "\n";

                // длина файла
                response = response + "Content-Length: " + f.length() + "\n";

                // строка с MIME кодировкой
                response = response + "Content-Type: " + mime + "\n";

                // остальные заголовки
                response = response
                        + "Connection: close\n"
                        + "Server: SimpleWEBServer\n\n";

                // выводим заголовок:
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());

                /*os.close();
                os.write(response.getBytes());*/

                // и сам файл:
                byte buf[] = new byte[64 * 1024];
                FileInputStream fis = new FileInputStream(path);
                r = 1;
                while (r > 0) {
                    r = fis.read(buf);
                    if (r > 0) os.write(buf, 0, r);
                }
                fis.close();

                // завершаем соединение
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            } // вывод исключений
        }


        // "вырезает" из HTTP заголовка URI ресурса и конвертирует его в filepath
        // URI берётся только для GET и POST запросов, иначе возвращается null
        protected String getPath(String request) {
            // ищем URI, указанный в HTTP запросе
            // URI ищется только для методов POST и GET, иначе возвращается null
            String  path;
            if (request == null) return null;

            // если URI записан вместе с именем протокола
            // то удаляем протокол и имя хоста
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