import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UrlShortener {
    @SuppressWarnings("HttpUrlsUsage")
    private static final String SCHEME = "http://";
    private static final String HOST = "localhost";
    private static final int PORT = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : 8080;
    private static final String PREFIX = SCHEME + HOST + ":" + PORT + "/";

    private static final ConcurrentHashMap<String, Integer> urlToCode = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, String> codeToUrl = new ConcurrentHashMap<>();
    private static final AtomicInteger id = new AtomicInteger(0);

    private static int shorten(String url) {
        if (urlToCode.containsKey(url)) return urlToCode.get(url);
        int code = id.incrementAndGet();
        codeToUrl.put(code, url);
        urlToCode.put(url, code);
        return code;
    }

    private static boolean onlyDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    private static final String NOT_FOUND = "Not found";

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/", httpExchange ->
            {
                String url = httpExchange.getRequestURI().toString().substring(1);
                if (url.isEmpty() || onlyDigits(url)) {
                    int code = url.isEmpty() ? id.get() : Integer.parseInt(url);
                    String redirectUrl = codeToUrl.get(code);
                    if (redirectUrl != null) {
                        httpExchange.getResponseHeaders().add("Location", redirectUrl);
                        httpExchange.sendResponseHeaders(307, 0);
                    } else {
                        httpExchange.sendResponseHeaders(404, NOT_FOUND.length());
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(NOT_FOUND.getBytes(StandardCharsets.UTF_8));
                        }
                    }
                } else {
                    // Return code by url
                    int code = shorten(url);
                    String response = "Now go to " + PREFIX + code + " or just " + PREFIX;
                    httpExchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(response.getBytes(StandardCharsets.UTF_8));
                    }
                }
            });

            System.out.println("BEFORE START");
            server.start();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}