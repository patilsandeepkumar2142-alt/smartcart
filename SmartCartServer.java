import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SmartCartServer {
    private static final int PORT = 8080;
    private static Path frontendDir;

    // In-memory runtime stores (synced with SQL or used as fast active cache)
    private static final List<Map<String, Object>> cartItems = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> wishlistItems = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> ordersList = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> reviewsList = new CopyOnWriteArrayList<>();
    private static final List<Map<String, Object>> reservationsList = new CopyOnWriteArrayList<>();

    static {
        // Initialize default seed reviews
        Map<String, Object> r1 = new LinkedHashMap<>();
        r1.put("id", 1);
        r1.put("productId", 1);
        r1.put("productName", "Wireless Headphones");
        r1.put("userName", "Rahul Sharma");
        r1.put("rating", 5);
        r1.put("comment", "Amazing sound quality, deep bass, and comfortable battery life!");
        r1.put("date", "2026-09-20");
        reviewsList.add(r1);

        Map<String, Object> r2 = new LinkedHashMap<>();
        r2.put("id", 2);
        r2.put("productId", 2);
        r2.put("productName", "Smart Watch");
        r2.put("userName", "Priya Patel");
        r2.put("rating", 4);
        r2.put("comment", "Great fitness tracker, very accurate step counter and nice display.");
        r2.put("date", "2026-09-21");
        reviewsList.add(r2);

        // Initialize default seed orders
        Map<String, Object> o1 = new LinkedHashMap<>();
        o1.put("id", 1001);
        o1.put("customerName", "Rahul Sharma");
        o1.put("phone", "+91 9876543210");
        o1.put("address", "Flat 402, Sunshine Apts, Indiranagar, Bengaluru - 560038");
        o1.put("paymentMethod", "Instant UPI (GPay)");
        o1.put("bookingType", "BUY_NOW");
        o1.put("totalAmount", 2499.0);
        o1.put("status", "DELIVERED");
        o1.put("orderDate", "2026-09-24 14:30");
        o1.put("items", "Wireless Headphones (x1)");
        o1.put("itemsJson", "[{\"productId\":1,\"name\":\"Wireless Headphones\",\"price\":2499.0,\"quantity\":1,\"category\":\"Electronics\",\"imageUrl\":\"https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80\"}]");
        ordersList.add(o1);

        Map<String, Object> o2 = new LinkedHashMap<>();
        o2.put("id", 1002);
        o2.put("customerName", "Priya Patel");
        o2.put("phone", "+91 9811223344");
        o2.put("address", "Plot 18, Green Valley, Whitefield, Bengaluru - 560066");
        o2.put("paymentMethod", "Cash on Delivery");
        o2.put("bookingType", "CART_ORDER");
        o2.put("totalAmount", 4398.0);
        o2.put("status", "SHIPPED");
        o2.put("orderDate", "2026-09-27 10:15");
        o2.put("items", "Smart Watch (x1), Java Programming Book (x1)");
        o2.put("itemsJson", "[{\"productId\":2,\"name\":\"Smart Watch\",\"price\":3499.0,\"quantity\":1,\"category\":\"Electronics\",\"imageUrl\":\"https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80\"},{\"productId\":6,\"name\":\"Java Programming Book\",\"price\":899.0,\"quantity\":1,\"category\":\"Books\",\"imageUrl\":\"https://images.unsplash.com/photo-1532012164546-f432f2e3edd3?auto=format&fit=crop&w=600&q=80\"}]");
        ordersList.add(o2);

        // Initialize sample seed reservation for out-of-stock product
        Map<String, Object> res1 = new LinkedHashMap<>();
        res1.put("id", 2001);
        res1.put("productId", 16);
        res1.put("productName", "Noise Cancelling Wireless Earbuds");
        res1.put("customerName", "Rahul Sharma");
        res1.put("email", "rahul@example.com");
        res1.put("phone", "+91 9876543210");
        res1.put("quantity", 1);
        res1.put("notificationChannel", "Email & SMS");
        res1.put("status", "AWAITING_RESTOCK");
        res1.put("reservationDate", "2026-10-10 11:20");
        res1.put("note", "Notify me immediately on restock");
        reservationsList.add(res1);
    }

    public static void startServer() {
        try {
            // Find frontend directory
            frontendDir = Paths.get("SmartCart/frontend");
            if (!Files.exists(frontendDir)) {
                frontendDir = Paths.get("frontend");
            }
            if (!Files.exists(frontendDir)) {
                frontendDir = Paths.get("../SmartCart/frontend");
            }

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            System.out.println("==================================================");
            System.out.println(" 🛒 SMARTCART FULL-STACK WEB SERVER RUNNING");
            System.out.println(" 🌐 URL: http://localhost:" + PORT);
            System.out.println(" 📁 Serving Frontend from: " + frontendDir.toAbsolutePath());
            System.out.println("==================================================");

            // API Endpoints
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/products", new ProductsHandler());
            server.createContext("/api/products/compare", new CompareProductsHandler());
            server.createContext("/api/budget-assistant", new BudgetAssistantHandler());
            server.createContext("/api/auth/login", new LoginHandler());
            server.createContext("/api/auth/register", new RegisterHandler());
            server.createContext("/api/cart", new CartHandler());
            server.createContext("/api/wishlist", new WishlistHandler());
            server.createContext("/api/orders", new OrdersHandler());
            server.createContext("/api/reservations", new ReservationsHandler());
            server.createContext("/api/reviews", new ReviewsHandler());
            server.createContext("/api/admin/products", new AdminProductsHandler());
            server.createContext("/api/admin/customers", new AdminCustomersHandler());
            server.createContext("/api/admin/orders", new AdminOrdersHandler());
            server.createContext("/api/admin/stock", new AdminStockHandler());
            server.createContext("/api/admin/reports", new AdminReportsHandler());

            // Static File Handler (HTML, CSS, JS, Assets)
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(null);
            server.start();

        } catch (Exception e) {
            System.err.println("Error starting SmartCart Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Helper Methods ---

    public static void sendCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public static void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        sendCors(exchange);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    public static Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String val = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            map.put(key, val);
        }
        return map;
    }

    public static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String productToJson(Product p) {
        return "{" +
            "\"id\":" + p.id + "," +
            "\"name\":\"" + escapeJson(p.name) + "\"," +
            "\"category\":\"" + escapeJson(p.category) + "\"," +
            "\"categoryId\":" + p.categoryId + "," +
            "\"description\":\"" + escapeJson(p.description) + "\"," +
            "\"price\":" + p.price + "," +
            "\"stock\":" + p.stock + "," +
            "\"rating\":" + p.rating + "," +
            "\"imageUrl\":\"" + escapeJson(p.imageUrl != null ? p.imageUrl : Product.getDefaultImageForProduct(p.id, p.name)) + "\"" +
            "}";
    }

    public static String productsListToJson(List<Product> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(productToJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    // --- Handlers ---

    static class StatusHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            boolean dbOk = false;
            try (Connection con = DatabaseConnection.getConnection()) {
                dbOk = (con != null);
            } catch (Exception ignored) {}

            String json = "{\"status\":\"online\",\"dbConnected\":" + dbOk + ",\"timestamp\":\"" + new Date() + "\"}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    static class ProductsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                List<Product> products = Product.getAllProducts();
                sendJsonResponse(exchange, 200, productsListToJson(products));
            } else {
                sendJsonResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }
        }
    }

    static class CompareProductsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
            String idsParam = params.getOrDefault("ids", "1,2");
            String[] parts = idsParam.split(",");
            List<Integer> idList = new ArrayList<>();
            for (String p : parts) {
                try {
                    idList.add(Integer.parseInt(p.trim()));
                } catch (NumberFormatException ignored) {}
            }
            if (idList.isEmpty()) {
                idList.add(1);
                idList.add(2);
            }

            int[] ids = new int[idList.size()];
            for (int i = 0; i < idList.size(); i++) ids[i] = idList.get(i);

            Map<String, Object> comparison = Product.compareProducts(ids);
            @SuppressWarnings("unchecked")
            List<Product> prods = (List<Product>) comparison.get("products");
            @SuppressWarnings("unchecked")
            Map<String, Object> highlights = (Map<String, Object>) comparison.get("highlights");

            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"count\":").append(comparison.get("count")).append(",");
            sb.append("\"products\":").append(productsListToJson(prods)).append(",");
            if (highlights != null) {
                sb.append("\"highlights\":{");
                sb.append("\"lowestPriceId\":").append(highlights.get("lowestPriceId")).append(",");
                sb.append("\"highestRatingId\":").append(highlights.get("highestRatingId")).append(",");
                sb.append("\"bestValueId\":").append(highlights.get("bestValueId"));
                sb.append("}");
            } else {
                sb.append("\"highlights\":null");
            }
            sb.append("}");

            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    static class BudgetAssistantHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            double budget = 0;
            String category = "";
            String requirement = "";

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                try { budget = Double.parseDouble(params.getOrDefault("budget", "0")); } catch (Exception ignored) {}
                category = params.getOrDefault("category", "");
                requirement = params.getOrDefault("requirement", "");
            } else {
                String body = readRequestBody(exchange);
                // Simple parser for key=value or json
                if (body.startsWith("{")) {
                    budget = extractDouble(body, "budget");
                    category = extractString(body, "category");
                    requirement = extractString(body, "requirement");
                }
            }

            List<Product> results = BudgetAssistant.findProductsList(budget, category, requirement);
            sendJsonResponse(exchange, 200, productsListToJson(results));
        }
    }

    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String body = readRequestBody(exchange);
            String email = extractString(body, "email");
            String password = extractString(body, "password");

            User u = User.login(email, password);
            if (u != null) {
                String json = "{\"success\":true,\"user\":{\"id\":" + u.id + ",\"name\":\"" + escapeJson(u.name) + "\",\"email\":\"" + escapeJson(u.email) + "\",\"role\":\"" + escapeJson(u.role) + "\"}}";
                sendJsonResponse(exchange, 200, json);
            } else {
                sendJsonResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid email or password\"}");
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String body = readRequestBody(exchange);
            String name = extractString(body, "name");
            String email = extractString(body, "email");
            String password = extractString(body, "password");

            boolean ok = User.register(name, email, password);
            if (ok) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Account registered successfully\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Registration failed or email exists\"}");
            }
        }
    }

    static class CartHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < cartItems.size(); i++) {
                    if (i > 0) sb.append(",");
                    Map<String, Object> item = cartItems.get(i);
                    sb.append("{\"id\":").append(item.get("id"))
                      .append(",\"productId\":").append(item.get("productId"))
                      .append(",\"name\":\"").append(escapeJson((String)item.get("name"))).append("\"")
                      .append(",\"price\":").append(item.get("price"))
                      .append(",\"quantity\":").append(item.get("quantity"))
                      .append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                int productId = (int) extractDouble(body, "productId");
                int qty = Math.max(1, (int) extractDouble(body, "quantity"));

                Product prod = null;
                for (Product p : Product.getAllProducts()) {
                    if (p.id == productId) { prod = p; break; }
                }
                if (prod != null) {
                    boolean found = false;
                    for (Map<String, Object> item : cartItems) {
                        if ((int)item.get("productId") == productId) {
                            item.put("quantity", (int)item.get("quantity") + qty);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        Map<String, Object> newItem = new LinkedHashMap<>();
                        newItem.put("id", cartItems.size() + 1);
                        newItem.put("productId", prod.id);
                        newItem.put("name", prod.name);
                        newItem.put("price", prod.price);
                        newItem.put("quantity", qty);
                        cartItems.add(newItem);
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Added to cart\",\"cartCount\":" + cartItems.size() + "}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Product not found\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    cartItems.removeIf(item -> (int)item.get("productId") == id || (int)item.get("id") == id);
                } else {
                    cartItems.clear();
                }
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Cart updated\"}");
            }
        }
    }

    static class WishlistHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < wishlistItems.size(); i++) {
                    if (i > 0) sb.append(",");
                    Map<String, Object> item = wishlistItems.get(i);
                    sb.append("{\"id\":").append(item.get("id"))
                      .append(",\"productId\":").append(item.get("productId"))
                      .append(",\"name\":\"").append(escapeJson((String)item.get("name"))).append("\"")
                      .append(",\"price\":").append(item.get("price"))
                      .append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                int productId = (int) extractDouble(body, "productId");
                Product prod = null;
                for (Product p : Product.getAllProducts()) {
                    if (p.id == productId) { prod = p; break; }
                }
                if (prod != null) {
                    boolean exists = wishlistItems.stream().anyMatch(w -> (int)w.get("productId") == productId);
                    if (!exists) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("id", wishlistItems.size() + 1);
                        item.put("productId", prod.id);
                        item.put("name", prod.name);
                        item.put("price", prod.price);
                        wishlistItems.add(item);
                    }
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Added to wishlist\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Product not found\"}");
                }
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    wishlistItems.removeIf(item -> (int)item.get("productId") == id || (int)item.get("id") == id);
                } else {
                    wishlistItems.clear();
                }
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Wishlist updated\"}");
            }
        }
    }

    static class OrdersHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < ordersList.size(); i++) {
                    if (i > 0) sb.append(",");
                    Map<String, Object> o = ordersList.get(i);
                    String itemsJson = (String) o.get("itemsJson");
                    if (itemsJson == null || itemsJson.trim().isEmpty()) {
                        itemsJson = "[]";
                    }
                    sb.append("{\"id\":").append(o.get("id"))
                      .append(",\"customerName\":\"").append(escapeJson(String.valueOf(o.getOrDefault("customerName", "Customer")))).append("\"")
                      .append(",\"phone\":\"").append(escapeJson(String.valueOf(o.getOrDefault("phone", "+91 9876543210")))).append("\"")
                      .append(",\"address\":\"").append(escapeJson(String.valueOf(o.getOrDefault("address", "Bengaluru, Karnataka")))).append("\"")
                      .append(",\"paymentMethod\":\"").append(escapeJson(String.valueOf(o.getOrDefault("paymentMethod", "Cash on Delivery")))).append("\"")
                      .append(",\"bookingType\":\"").append(escapeJson(String.valueOf(o.getOrDefault("bookingType", "CART_ORDER")))).append("\"")
                      .append(",\"totalAmount\":").append(o.get("totalAmount"))
                      .append(",\"status\":\"").append(escapeJson(String.valueOf(o.getOrDefault("status", "PLACED")))).append("\"")
                      .append(",\"orderDate\":\"").append(escapeJson(String.valueOf(o.getOrDefault("orderDate", "2026-09-28 21:00")))).append("\"")
                      .append(",\"items\":\"").append(escapeJson(String.valueOf(o.getOrDefault("items", "SmartCart Items")))).append("\"")
                      .append(",\"itemsList\":").append(itemsJson)
                      .append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                String customerName = extractString(body, "customerName");
                if (customerName.isEmpty()) customerName = "Customer";
                String phone = extractString(body, "phone");
                if (phone.isEmpty()) phone = "+91 9876543210";
                String address = extractString(body, "address");
                if (address.isEmpty()) address = "Delivery Address, Bengaluru";
                String paymentMethod = extractString(body, "paymentMethod");
                if (paymentMethod.isEmpty()) paymentMethod = "Cash on Delivery (COD)";
                String bookingType = extractString(body, "bookingType");
                if (bookingType.isEmpty()) bookingType = "CART_ORDER";
                
                int directProdId = (int) extractDouble(body, "productId");
                int directQty = Math.max(1, (int) extractDouble(body, "quantity"));
                double totalAmount = extractDouble(body, "totalAmount");

                String itemsSummary = "";
                StringBuilder itemsJson = new StringBuilder("[");

                if (directProdId > 0 || "DIRECT_BOOKING".equalsIgnoreCase(bookingType) || "BUY_NOW".equalsIgnoreCase(bookingType)) {
                    // Buy Now / Direct Order for single product
                    Product prod = null;
                    for (Product p : Product.getAllProducts()) {
                        if (p.id == directProdId) { prod = p; break; }
                    }
                    if (prod != null) {
                        if (totalAmount <= 0) totalAmount = prod.price * directQty;
                        itemsSummary = prod.name + " (x" + directQty + ")";
                        itemsJson.append("{\"productId\":").append(prod.id)
                                 .append(",\"name\":\"").append(escapeJson(prod.name)).append("\"")
                                 .append(",\"price\":").append(prod.price)
                                 .append(",\"quantity\":").append(directQty)
                                 .append(",\"category\":\"").append(escapeJson(prod.category)).append("\"")
                                 .append(",\"imageUrl\":\"").append(escapeJson(prod.imageUrl != null ? prod.imageUrl : Product.getDefaultImageForProduct(prod.id, prod.name))).append("\"")
                                 .append("}");
                    }
                } else {
                    // Cart Order
                    if (!cartItems.isEmpty()) {
                        StringBuilder it = new StringBuilder();
                        for (int i = 0; i < cartItems.size(); i++) {
                            Map<String, Object> ci = cartItems.get(i);
                            if (it.length() > 0) it.append(", ");
                            it.append(ci.get("name")).append(" (x").append(ci.get("quantity")).append(")");

                            if (i > 0) itemsJson.append(",");
                            int pId = (int) ci.get("productId");
                            Product pObj = null;
                            for (Product p : Product.getAllProducts()) { if (p.id == pId) { pObj = p; break; } }
                            String imgUrl = pObj != null ? (pObj.imageUrl != null ? pObj.imageUrl : Product.getDefaultImageForProduct(pId, pObj.name)) : "images/default.svg";
                            String cat = pObj != null ? pObj.category : "General";

                            itemsJson.append("{\"productId\":").append(pId)
                                     .append(",\"name\":\"").append(escapeJson(String.valueOf(ci.get("name")))).append("\"")
                                     .append(",\"price\":").append(ci.get("price"))
                                     .append(",\"quantity\":").append(ci.get("quantity"))
                                     .append(",\"category\":\"").append(escapeJson(cat)).append("\"")
                                     .append(",\"imageUrl\":\"").append(escapeJson(imgUrl)).append("\"")
                                     .append("}");
                        }
                        itemsSummary = it.toString();
                    }
                }
                itemsJson.append("]");

                if (itemsSummary.isEmpty()) {
                    itemsSummary = extractString(body, "items");
                    if (itemsSummary.isEmpty()) itemsSummary = "SmartCart Product";
                }
                if (totalAmount <= 0) totalAmount = 1499.0;

                String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

                Map<String, Object> order = new LinkedHashMap<>();
                order.put("id", 1000 + ordersList.size() + 1);
                order.put("customerName", customerName);
                order.put("phone", phone);
                order.put("address", address);
                order.put("paymentMethod", paymentMethod);
                order.put("bookingType", bookingType);
                order.put("totalAmount", totalAmount);
                order.put("status", "CONFIRMED");
                order.put("orderDate", dateStr);
                order.put("items", itemsSummary);
                order.put("itemsJson", itemsJson.toString());
                ordersList.add(0, order);

                if (!"DIRECT_BOOKING".equalsIgnoreCase(bookingType) && !"BUY_NOW".equalsIgnoreCase(bookingType)) {
                    cartItems.clear(); // Clear cart for standard cart checkouts
                }

                sendJsonResponse(exchange, 200, "{\"success\":true,\"orderId\":" + order.get("id") + ",\"bookingType\":\"" + escapeJson(bookingType) + "\",\"message\":\"Order placed successfully!\"}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                // Cancel / remove order
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    int oId = Integer.parseInt(idStr);
                    for (Map<String, Object> o : ordersList) {
                        if ((int) o.get("id") == oId) {
                            o.put("status", "CANCELLED");
                            break;
                        }
                    }
                }
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Order cancelled successfully\"}");
            }
        }
    }

    static class ReservationsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < reservationsList.size(); i++) {
                    if (i > 0) sb.append(",");
                    Map<String, Object> r = reservationsList.get(i);
                    sb.append("{")
                      .append("\"id\":").append(r.get("id"))
                      .append(",\"productId\":").append(r.get("productId"))
                      .append(",\"productName\":\"").append(escapeJson(String.valueOf(r.get("productName")))).append("\"")
                      .append(",\"customerName\":\"").append(escapeJson(String.valueOf(r.get("customerName")))).append("\"")
                      .append(",\"email\":\"").append(escapeJson(String.valueOf(r.get("email")))).append("\"")
                      .append(",\"phone\":\"").append(escapeJson(String.valueOf(r.get("phone")))).append("\"")
                      .append(",\"quantity\":").append(r.get("quantity"))
                      .append(",\"notificationChannel\":\"").append(escapeJson(String.valueOf(r.get("notificationChannel")))).append("\"")
                      .append(",\"status\":\"").append(escapeJson(String.valueOf(r.get("status")))).append("\"")
                      .append(",\"reservationDate\":\"").append(escapeJson(String.valueOf(r.get("reservationDate")))).append("\"")
                      .append(",\"note\":\"").append(escapeJson(String.valueOf(r.getOrDefault("note", "")))).append("\"")
                      .append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                int productId = (int) extractDouble(body, "productId");
                String prodName = extractString(body, "productName");
                if (prodName.isEmpty() && productId > 0) {
                    for (Product p : Product.getAllProducts()) {
                        if (p.id == productId) { prodName = p.name; break; }
                    }
                }
                if (prodName.isEmpty()) prodName = "Reserved Product";

                String customerName = extractString(body, "customerName");
                if (customerName.isEmpty()) customerName = "Valued Customer";
                String email = extractString(body, "email");
                String phone = extractString(body, "phone");
                int quantity = Math.max(1, (int) extractDouble(body, "quantity"));
                String notificationChannel = extractString(body, "notificationChannel");
                if (notificationChannel.isEmpty()) notificationChannel = "Email & SMS";
                String note = extractString(body, "note");

                String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());

                Map<String, Object> resObj = new LinkedHashMap<>();
                int resId = 2000 + reservationsList.size() + 1;
                resObj.put("id", resId);
                resObj.put("productId", productId);
                resObj.put("productName", prodName);
                resObj.put("customerName", customerName);
                resObj.put("email", email);
                resObj.put("phone", phone);
                resObj.put("quantity", quantity);
                resObj.put("notificationChannel", notificationChannel);
                resObj.put("status", "AWAITING_RESTOCK");
                resObj.put("reservationDate", dateStr);
                resObj.put("note", note);
                reservationsList.add(0, resObj);

                sendJsonResponse(exchange, 200, "{\"success\":true,\"reservationId\":" + resId + ",\"message\":\"Product reserved successfully! We will alert you upon restocking.\"}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                String idStr = params.get("id");
                if (idStr != null) {
                    int rId = Integer.parseInt(idStr);
                    for (Map<String, Object> r : reservationsList) {
                        if ((int) r.get("id") == rId) {
                            r.put("status", "CANCELLED");
                            break;
                        }
                    }
                }
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Reservation cancelled\"}");
            }
        }
    }

    static class ReviewsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < reviewsList.size(); i++) {
                    if (i > 0) sb.append(",");
                    Map<String, Object> r = reviewsList.get(i);
                    sb.append("{\"id\":").append(r.get("id"))
                      .append(",\"productName\":\"").append(escapeJson((String)r.get("productName"))).append("\"")
                      .append(",\"userName\":\"").append(escapeJson((String)r.get("userName"))).append("\"")
                      .append(",\"rating\":").append(r.get("rating"))
                      .append(",\"comment\":\"").append(escapeJson((String)r.get("comment"))).append("\"")
                      .append(",\"date\":\"").append(escapeJson((String)r.get("date"))).append("\"")
                      .append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                String prodName = extractString(body, "productName");
                String userName = extractString(body, "userName");
                String comment = extractString(body, "comment");
                int rating = Math.max(1, Math.min(5, (int) extractDouble(body, "rating")));

                Map<String, Object> rev = new LinkedHashMap<>();
                rev.put("id", reviewsList.size() + 1);
                rev.put("productName", prodName.isEmpty() ? "SmartCart Product" : prodName);
                rev.put("userName", userName.isEmpty() ? "Verified Buyer" : userName);
                rev.put("rating", rating);
                rev.put("comment", comment);
                rev.put("date", "2026-09-23");
                reviewsList.add(0, rev);

                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Review posted successfully!\"}");
            }
        }
    }

    static class AdminProductsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                sendJsonResponse(exchange, 200, productsListToJson(Product.getAllProducts()));
            } else if ("POST".equalsIgnoreCase(method)) {
                String body = readRequestBody(exchange);
                String name = extractString(body, "name");
                int catId = (int) extractDouble(body, "categoryId");
                String desc = extractString(body, "description");
                double price = extractDouble(body, "price");
                int stock = (int) extractDouble(body, "stock");
                double rating = extractDouble(body, "rating");
                String imageUrl = extractString(body, "imageUrl");
                if (imageUrl.isEmpty()) {
                    imageUrl = extractString(body, "image");
                }
                if (rating <= 0) rating = 4.0;

                boolean added = Product.addProduct(name, catId, desc, price, stock, rating);
                sendJsonResponse(exchange, 200, "{\"success\":" + added + ",\"message\":\"Product saved successfully\"}");
            } else if ("DELETE".equalsIgnoreCase(method)) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI().getQuery());
                int id = Integer.parseInt(params.getOrDefault("id", "0"));
                boolean deleted = Product.deleteProduct(id);
                sendJsonResponse(exchange, 200, "{\"success\":" + deleted + ",\"message\":\"Product deleted\"}");
            }
        }
    }

    static class AdminCustomersHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            List<User> users = User.getAllUsers();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                if (i > 0) sb.append(",");
                User u = users.get(i);
                sb.append("{\"id\":").append(u.id)
                  .append(",\"name\":\"").append(escapeJson(u.name)).append("\"")
                  .append(",\"email\":\"").append(escapeJson(u.email)).append("\"")
                  .append(",\"role\":\"").append(escapeJson(u.role)).append("\"")
                  .append("}");
            }
            sb.append("]");
            sendJsonResponse(exchange, 200, sb.toString());
        }
    }

    static class AdminOrdersHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < ordersList.size(); i++) {
                    if (i > 0) sb.append(",");
                    Map<String, Object> o = ordersList.get(i);
                    sb.append("{\"id\":").append(o.get("id"))
                      .append(",\"customerName\":\"").append(escapeJson((String)o.get("customerName"))).append("\"")
                      .append(",\"totalAmount\":").append(o.get("totalAmount"))
                      .append(",\"status\":\"").append(escapeJson((String)o.get("status"))).append("\"")
                      .append(",\"orderDate\":\"").append(escapeJson((String)o.get("orderDate"))).append("\"")
                      .append(",\"items\":\"").append(escapeJson((String)o.get("items"))).append("\"")
                      .append("}");
                }
                sb.append("]");
                sendJsonResponse(exchange, 200, sb.toString());
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                int orderId = (int) extractDouble(body, "orderId");
                String status = extractString(body, "status");
                for (Map<String, Object> o : ordersList) {
                    if ((int)o.get("id") == orderId) {
                        o.put("status", status);
                        break;
                    }
                }
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Order status updated to " + escapeJson(status) + "\"}");
            }
        }
    }

    static class AdminStockHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String body = readRequestBody(exchange);
            int productId = (int) extractDouble(body, "productId");
            int stock = (int) extractDouble(body, "stock");

            boolean updated = Product.updateStock(productId, stock);
            sendJsonResponse(exchange, 200, "{\"success\":" + updated + ",\"message\":\"Stock updated to " + stock + "\"}");
        }
    }

    static class AdminReportsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendCors(exchange);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            double totalRevenue = 0;
            for (Map<String, Object> o : ordersList) {
                totalRevenue += (double) o.get("totalAmount");
            }
            List<Product> products = Product.getAllProducts();
            List<User> users = User.getAllUsers();

            String json = "{" +
                "\"totalSales\":" + totalRevenue + "," +
                "\"totalOrders\":" + ordersList.size() + "," +
                "\"totalProducts\":" + products.size() + "," +
                "\"totalCustomers\":" + users.size() + "," +
                "\"topCategory\":\"Electronics\"," +
                "\"averageOrderValue\":" + (ordersList.isEmpty() ? 0 : Math.round(totalRevenue / ordersList.size())) +
                "}";
            sendJsonResponse(exchange, 200, json);
        }
    }

    // Static Files Handler
    static class StaticFileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Clean path
            if (path.startsWith("/")) path = path.substring(1);

            Path filePath = frontendDir.resolve(path).normalize();

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                // Try customer.html or 404
                String notFound = "<h1>404 Not Found</h1><p>The requested URL was not found on this server.</p><a href='/'>Go Home</a>";
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                exchange.sendResponseHeaders(404, notFound.getBytes(StandardCharsets.UTF_8).length);
                OutputStream os = exchange.getResponseBody();
                os.write(notFound.getBytes(StandardCharsets.UTF_8));
                os.close();
                return;
            }

            String mimeType = getMimeType(filePath.toString());
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            sendCors(exchange);

            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, fileBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(fileBytes);
            os.close();
        }

        private String getMimeType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }

    // JSON String & Number Parser Utilities
    private static String extractString(String body, String key) {
        if (body == null) return "";
        int keyIndex = body.indexOf("\"" + key + "\"");
        if (keyIndex == -1) keyIndex = body.indexOf(key + "=");
        if (keyIndex == -1) return "";

        int colon = body.indexOf(":", keyIndex);
        if (colon != -1) {
            int startQuote = body.indexOf("\"", colon);
            if (startQuote != -1) {
                int endQuote = body.indexOf("\"", startQuote + 1);
                if (endQuote != -1) {
                    return body.substring(startQuote + 1, endQuote).replace("\\u0026", "&");
                }
            }
        }
        return "";
    }

    private static double extractDouble(String body, String key) {
        if (body == null) return 0;
        int keyIndex = body.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return 0;
        int colon = body.indexOf(":", keyIndex);
        if (colon == -1) return 0;

        int i = colon + 1;
        while (i < body.length() && (body.charAt(i) == ' ' || body.charAt(i) == '\"')) i++;
        int start = i;
        while (i < body.length() && (Character.isDigit(body.charAt(i)) || body.charAt(i) == '.' || body.charAt(i) == '-')) i++;
        try {
            return Double.parseDouble(body.substring(start, i));
        } catch (Exception e) {
            return 0;
        }
    }
}
