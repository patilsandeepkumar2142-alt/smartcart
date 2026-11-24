import java.sql.*;
import java.util.*;

public class Product {
    public int id;
    public String name;
    public String category;
    public int categoryId;
    public String description;
    public double price;
    public int stock;
    public double rating;
    public String websiteUrl;
    public String imageUrl;

    public Product() {}

    public Product(int id, String name, String category, int categoryId, String description, double price, int stock, double rating) {
        this(id, name, category, categoryId, description, price, stock, rating, "", null);
    }

    public Product(int id, String name, String category, int categoryId, String description, double price, int stock, double rating, String websiteUrl) {
        this(id, name, category, categoryId, description, price, stock, rating, websiteUrl, null);
    }

    public Product(int id, String name, String category, int categoryId, String description, double price, int stock, double rating, String websiteUrl, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.categoryId = categoryId;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
        this.websiteUrl = (websiteUrl != null && !websiteUrl.trim().isEmpty()) ? websiteUrl : "";
        this.imageUrl = (imageUrl != null && !imageUrl.trim().isEmpty()) ? imageUrl : getDefaultImageForProduct(id, name);
    }

    private static List<Product> inMemoryProducts = null;

    public static String getDefaultImageForProduct(int id, String name) {
        String lower = (name != null) ? name.toLowerCase() : "";
        if (id == 1 || lower.contains("headphone") || lower.contains("earbud")) return "images/headphones.svg";
        if (id == 2 || lower.contains("watch")) return "images/smartwatch.svg";
        if (id == 3 || lower.contains("speaker")) return "images/speaker.svg";
        if (id == 4 || lower.contains("backpack") || lower.contains("bag") || lower.contains("wallet")) return "images/backpack.svg";
        if (id == 5 || lower.contains("shoe") || lower.contains("sneaker")) return "images/shoes.svg";
        if (id == 6 || id == 13 || id == 22 || id == 23 || id == 24 || id == 25 || lower.contains("book") || lower.contains("java") || lower.contains("code") || lower.contains("habits") || lower.contains("programmer") || lower.contains("planner") || lower.contains("journal") || lower.contains("productivity") || lower.contains("deep work") || lower.contains("getting things done")) return "images/book.svg";
        if (id == 11 || lower.contains("jacket") || lower.contains("denim")) return "images/jacket.svg";
        if (id == 7 || lower.contains("shirt") || lower.contains("t-shirt") || lower.contains("chino") || lower.contains("pant")) return "images/tshirt.svg";
        if (id == 8 || lower.contains("keyboard") || lower.contains("stand") || lower.contains("hub") || lower.contains("webcam")) return "images/keyboard.svg";
        if (id == 9 || lower.contains("mouse")) return "images/mouse.svg";
        if (id == 10 || id == 26 || lower.contains("bottle") || lower.contains("kettle") || lower.contains("lunch") || lower.contains("box")) return "images/bottle.svg";
        if (id == 12 || lower.contains("coffee") || lower.contains("cookware") || lower.contains("dispenser") || lower.contains("toothbrush") || lower.contains("vacuum") || lower.contains("scale")) return "images/coffeemaker.svg";
        if (id == 14 || id == 33 || lower.contains("yoga") || lower.contains("mat") || lower.contains("band") || lower.contains("dumbbell") || lower.contains("posture") || lower.contains("grip") || lower.contains("rope")) return "images/yogamat.svg";
        return "images/default.svg";
    }

    private static String getDefaultWebsiteForProduct(int id, String name) {
        if (id == 1 || (name != null && name.toLowerCase().contains("headphone"))) return "https://www.sony.com/electronics/headband-headphones";
        if (id == 2 || (name != null && name.toLowerCase().contains("watch"))) return "https://www.fitbit.com";
        if (id == 3 || (name != null && name.toLowerCase().contains("speaker"))) return "https://www.jbl.com/bluetooth-speakers";
        if (id == 4 || (name != null && name.toLowerCase().contains("backpack"))) return "https://www.dell.com/en-us/shop/carrying-cases/ar/6675";
        if (id == 5 || (name != null && name.toLowerCase().contains("shoe"))) return "https://www.nike.com/running";
        if (id == 6 || (name != null && name.toLowerCase().contains("java"))) return "https://docs.oracle.com/en/java/";
        if (id == 7 || (name != null && name.toLowerCase().contains("t-shirt"))) return "https://www.uniqlo.com";
        if (id == 8 || (name != null && name.toLowerCase().contains("keyboard"))) return "https://www.keychron.com";
        if (id == 9 || (name != null && name.toLowerCase().contains("mouse"))) return "https://www.logitechg.com";
        if (id == 10 || (name != null && name.toLowerCase().contains("bottle"))) return "https://www.hydroflask.com";
        if (id == 11 || (name != null && name.toLowerCase().contains("jacket"))) return "https://www.levi.in";
        if (id == 12 || (name != null && name.toLowerCase().contains("coffee"))) return "https://www.philips.com";
        if (id == 13 || (name != null && name.toLowerCase().contains("clean code"))) return "https://www.oreilly.com";
        if (id == 14 || (name != null && name.toLowerCase().contains("yoga"))) return "https://www.decathlon.in";
        return "https://www.google.com/search?q=" + (name != null ? name.replace(" ", "+") : "product");
    }

    public static List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.id, p.name, c.name AS category, p.category_id, p.description, p.price, p.stock, p.rating, p.website_url " +
                     "FROM products p LEFT JOIN categories c ON p.category_id = c.id ORDER BY p.id ASC";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String siteUrl = null;
                        try {
                            siteUrl = rs.getString("website_url");
                        } catch (Exception ignored) {}

                        list.add(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category") != null ? rs.getString("category") : "General",
                            rs.getInt("category_id"),
                            rs.getString("description"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getDouble("rating"),
                            siteUrl
                        ));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("SQL error in getAllProducts: " + e.getMessage());
        }

        // Fallback default products if database is offline or empty
        if (list.isEmpty()) {
            if (inMemoryProducts == null) {
                inMemoryProducts = new ArrayList<>(getDefaultProducts());
            }
            return new ArrayList<>(inMemoryProducts);
        }
        return list;
    }

    public static List<Product> getProductsByIds(int[] ids) {
        List<Product> all = getAllProducts();
        List<Product> selected = new ArrayList<>();
        if (ids == null || ids.length == 0) return selected;

        Set<Integer> idSet = new HashSet<>();
        for (int id : ids) idSet.add(id);

        for (Product p : all) {
            if (idSet.contains(p.id)) {
                selected.add(p);
            }
        }
        return selected;
    }

    public static Map<String, Object> compareProducts(int[] ids) {
        List<Product> products = getProductsByIds(ids);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("products", products);
        result.put("count", products.size());

        if (!products.isEmpty()) {
            Product lowestPrice = products.get(0);
            Product highestRating = products.get(0);
            Product bestValue = products.get(0);
            double bestValueScore = -1;

            for (Product p : products) {
                if (p.price < lowestPrice.price) lowestPrice = p;
                if (p.rating > highestRating.rating) highestRating = p;

                // Value score = (rating * 1000) / price
                double valScore = (p.rating * 1000.0) / Math.max(p.price, 1.0);
                if (valScore > bestValueScore) {
                    bestValueScore = valScore;
                    bestValue = p;
                }
            }

            Map<String, Object> highlights = new LinkedHashMap<>();
            highlights.put("lowestPriceId", lowestPrice.id);
            highlights.put("highestRatingId", highestRating.id);
            highlights.put("bestValueId", bestValue.id);
            result.put("highlights", highlights);
        }
        return result;
    }

    public static void showProducts() {
        List<Product> products = getAllProducts();
        for (Product p : products) {
            System.out.println(p.id + " | " + p.name + " (" + p.category + ") | ₹" + p.price +
                " | Stock: " + p.stock + " | Rating: " + p.rating);
        }
    }

    public static boolean addProduct(String name, int categoryId, String description, double price, int stock, double rating) {
        return addProduct(name, categoryId, description, price, stock, rating, null);
    }

    public static boolean addProduct(String name, int categoryId, String description, double price, int stock, double rating, String websiteUrl) {
        String sqlWithUrl = "INSERT INTO products(name, category_id, description, price, stock, rating, website_url) VALUES(?,?,?,?,?,?,?)";
        String sqlFallback = "INSERT INTO products(name, category_id, description, price, stock, rating) VALUES(?,?,?,?,?,?)";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sqlWithUrl)) {
                    ps.setString(1, name);
                    ps.setInt(2, categoryId);
                    ps.setString(3, description);
                    ps.setDouble(4, price);
                    ps.setInt(5, stock);
                    ps.setDouble(6, rating);
                    ps.setString(7, (websiteUrl != null && !websiteUrl.trim().isEmpty()) ? websiteUrl : getDefaultWebsiteForProduct(0, name));
                    return ps.executeUpdate() > 0;
                } catch (SQLException sqle) {
                    try (PreparedStatement ps = con.prepareStatement(sqlFallback)) {
                        ps.setString(1, name);
                        ps.setInt(2, categoryId);
                        ps.setString(3, description);
                        ps.setDouble(4, price);
                        ps.setInt(5, stock);
                        ps.setDouble(6, rating);
                        return ps.executeUpdate() > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // In-memory fallback
        if (inMemoryProducts == null) {
            inMemoryProducts = new ArrayList<>(getDefaultProducts());
        }
        String[] catNames = {"", "Electronics", "Fashion", "Books", "Home & Kitchen", "Sports"};
        String cat = (categoryId >= 1 && categoryId <= 5) ? catNames[categoryId] : "General";
        inMemoryProducts.add(new Product(inMemoryProducts.size() + 1, name, cat, categoryId, description, price, stock, rating, websiteUrl));
        return true;
    }

    public static boolean updateStock(int id, int stock) {
        String sql = "UPDATE products SET stock=? WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, stock);
                    ps.setInt(2, id);
                    if (ps.executeUpdate() > 0) return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // In-memory fallback
        if (inMemoryProducts == null) {
            inMemoryProducts = new ArrayList<>(getDefaultProducts());
        }
        for (Product p : inMemoryProducts) {
            if (p.id == id) {
                p.stock = stock;
                return true;
            }
        }
        return false;
    }

    public static boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id=?";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() > 0) return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // In-memory fallback
        if (inMemoryProducts == null) {
            inMemoryProducts = new ArrayList<>(getDefaultProducts());
        }
        return inMemoryProducts.removeIf(p -> p.id == id);
    }

    public static List<Product> getDefaultProducts() {
        List<Product> defaults = new ArrayList<>();
        defaults.add(new Product(1, "Wireless Headphones", "Electronics", 1, "Bluetooth wireless headphones with noise cancellation & 24h battery", 2499, 20, 4.3, "https://www.sony.com/electronics/headband-headphones", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(2, "Smart Watch", "Electronics", 1, "Fitness tracking smart watch with heart rate and SpO2 monitor", 3499, 15, 4.2, "https://www.fitbit.com", "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(3, "Bluetooth Speaker", "Electronics", 1, "Portable waterproof Bluetooth speaker with deep bass", 1799, 18, 4.2, "https://www.jbl.com/bluetooth-speakers", "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(4, "Laptop Backpack", "Home & Kitchen", 4, "Water resistant ergonomic laptop backpack with USB charging port", 1299, 30, 4.4, "https://www.dell.com/en-us/shop/carrying-cases/ar/6675", "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(5, "Running Shoes", "Sports", 5, "Comfortable breathable running shoes with cushioning", 2999, 25, 4.5, "https://www.nike.com/running", "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(6, "Java Programming Book", "Books", 3, "Complete modern Java programming guide from basics to advanced", 899, 50, 4.6, "https://docs.oracle.com/en/java/", "https://images.unsplash.com/photo-1532012164546-f432f2e3edd3?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(7, "T-Shirt", "Fashion", 2, "100% Pure casual breathable slim-fit cotton T-shirt", 699, 40, 4.1, "https://www.uniqlo.com", "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(8, "Mechanical Keyboard", "Electronics", 1, "RGB mechanical gaming keyboard with tactile blue switches", 2999, 12, 4.4, "https://www.keychron.com", "https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(9, "Gaming Mouse", "Electronics", 1, "Ergonomic wireless gaming mouse with 16000 DPI sensor and RGB lighting", 1499, 28, 4.5, "https://www.logitechg.com", "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(10, "Stainless Steel Water Bottle", "Sports", 5, "Vacuum insulated 1-litre stainless steel water bottle keeping drinks cold 24h", 799, 35, 4.6, "https://www.hydroflask.com", "https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(11, "Classic Denim Jacket", "Fashion", 2, "Classic vintage blue denim jacket with button closure and relaxed fit", 2199, 22, 4.4, "https://www.levi.in", "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(12, "Drip Coffee Maker", "Home & Kitchen", 4, "Automatic drip coffee maker with programmable timer and thermal carafe", 3899, 16, 4.7, "https://www.philips.com", "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(13, "Clean Code Book", "Books", 3, "A handbook of agile software craftsmanship and modern architectural patterns", 1199, 30, 4.8, "https://www.oreilly.com", "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(14, "Non-Slip Yoga Mat", "Sports", 5, "High-density eco-friendly non-slip exercise and yoga mat with carrying strap", 899, 45, 4.3, "https://www.decathlon.in", "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?auto=format&fit=crop&w=600&q=80"));

        // Additional Category 1: Electronics
        defaults.add(new Product(15, "Ultra HD 4K Action Camera", "Electronics", 1, "Waterproof 4K action camera with wide-angle lens, WiFi, and stabilization", 6499, 14, 4.6, "https://gopro.com", "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(16, "Noise Cancelling Wireless Earbuds", "Electronics", 1, "True wireless earbuds with active noise cancellation and wireless charging case", 2199, 0, 4.7, "https://www.boat-lifestyle.com", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(17, "Fast Charging Power Bank 20000mAh", "Electronics", 1, "Compact high-capacity power bank with 22.5W fast charge & dual USB-C ports", 1599, 35, 4.5, "https://www.anker.com", "https://images.unsplash.com/photo-1609592424368-b7d159a67448?auto=format&fit=crop&w=600&q=80"));

        // Additional Category 2: Fashion
        defaults.add(new Product(18, "Slim Fit Casual Chinos", "Fashion", 2, "Stretchable cotton casual chinos with premium tailored fit and wrinkle resistance", 1499, 25, 4.3, "https://www.zara.com", "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(19, "Polarized Aviator Sunglasses", "Fashion", 2, "Classic UV400 polarized aviator metal-frame sunglasses with anti-glare lenses", 1299, 0, 4.8, "https://www.ray-ban.com", "https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(20, "Genuine Leather Bifold Wallet", "Fashion", 2, "Handcrafted RFID-blocking genuine leather wallet with multiple card slots", 899, 42, 4.6, "https://www.woodlandworldwide.com", "https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(21, "Casual Canvas Low-Top Sneakers", "Fashion", 2, "Lightweight classic low-top canvas sneakers with cushioned insole and non-slip sole", 1899, 18, 4.4, "https://www.converse.com", "https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=600&q=80"));

        // Additional Category 3: Books
        defaults.add(new Product(22, "Designing Data-Intensive Applications", "Books", 3, "The definitive guide to the architecture of modern data systems and distributed scalability", 1699, 24, 4.9, "https://www.oreilly.com", "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(23, "Atomic Habits Hardcover", "Books", 3, "An easy & proven way to build good habits, break bad ones, and achieve remarkable results", 749, 55, 4.8, "https://jamesclear.com/atomic-habits", "https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(24, "The Pragmatic Programmer", "Books", 3, "20th Anniversary Edition handbook for journeys to software engineering mastery", 1499, 0, 4.9, "https://pragprog.com", "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(25, "Deep Learning with Python", "Books", 3, "Comprehensive deep learning practical guide using Python, Keras, and modern AI neural networks", 1899, 16, 4.7, "https://www.manning.com", "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?auto=format&fit=crop&w=600&q=80"));

        // Additional Category 4: Home & Kitchen
        defaults.add(new Product(26, "Stainless Steel Electric Kettle 1.8L", "Home & Kitchen", 4, "Fast boiling cordless electric kettle with auto shut-off and boil-dry protection", 1199, 26, 4.5, "https://www.philips.com", "https://images.unsplash.com/photo-1594213114663-ddf3f2a1758b?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(27, "Non-Stick Granite Cookware Set", "Home & Kitchen", 4, "3-piece scratch-resistant non-stick induction cookware set with tempered glass lids", 3499, 0, 4.7, "https://www.prestige.com", "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(28, "Minimalist LED Desk Lamp", "Home & Kitchen", 4, "Adjustable eye-caring LED desk lamp with 5 brightness levels and USB charging port", 1249, 30, 4.5, "https://www.ikea.com", "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(29, "Microfiber Queen Bed Sheet Set", "Home & Kitchen", 4, "Super soft breathable 4-piece brushed microfiber bed sheet set with deep pockets", 999, 35, 4.3, "https://www.amazon.in", "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=600&q=80"));

        // Additional Category 5: Sports
        defaults.add(new Product(30, "Adjustable Dumbbell Set 20kg", "Sports", 5, "Quick-adjust cast iron dumbbell set with comfortable grip for home workouts", 4999, 15, 4.7, "https://www.decathlon.in", "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(31, "Aerodynamic Cycling Helmet with LED", "Sports", 5, "Ultralight breathable road cycling helmet with rechargeable safety rear LED light", 1799, 0, 4.8, "https://www.decathlon.in", "https://images.unsplash.com/photo-1559348349-86f1f65817fe?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(32, "Professional Badminton Racket Set", "Sports", 5, "High-modulus carbon fiber lightweight badminton racket set with shuttlecocks and bag", 1599, 22, 4.5, "https://www.yonex.com", "https://images.unsplash.com/photo-1626224583764-f87db24ac4ea?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(33, "Loop Resistance Bands Set of 5", "Sports", 5, "Premium natural latex workout exercise bands with different resistance levels", 549, 60, 4.6, "https://www.decathlon.in", "https://images.unsplash.com/photo-1598289431512-b97b0917affc?auto=format&fit=crop&w=600&q=80"));

        // Daily Work & Desk Essentials: Category 1 (Electronics)
        defaults.add(new Product(34, "Ergonomic Aluminum Laptop Stand", "Electronics", 1, "Adjustable 6-level folding aluminum cooling riser for laptops up to 17 inches", 1199, 25, 4.6, "https://www.amazon.in", "https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(35, "USB-C 7-in-1 Multiport Hub Adapter", "Electronics", 1, "Multiport adapter with 4K HDMI, 100W Power Delivery, SD reader, and 3 USB 3.0 ports", 1899, 30, 4.5, "https://www.anker.com", "https://images.unsplash.com/photo-1616440347437-b1c73416efc2?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(36, "Full HD 1080p Webcam with Microphone", "Electronics", 1, "Auto-focus HD webcam with dual noise-cancelling mics and privacy shutter for daily meetings", 2299, 0, 4.7, "https://www.logitech.com", "https://images.unsplash.com/photo-1588508065123-287b28e013da?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(37, "Multi-Device Silent Wireless Mouse", "Electronics", 1, "Ultra-quiet optical wireless mouse with 2.4G & Bluetooth dual-mode for multi-screen work", 999, 40, 4.4, "https://www.logitech.com", "https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=600&q=80"));

        // Daily Office & Commute Essentials: Category 2 (Fashion)
        defaults.add(new Product(38, "Waterproof Commuter Laptop Messenger Bag", "Fashion", 2, "Heavy-duty waterproof oxford fabric messenger bag with padded 15.6 inch laptop sleeve", 1699, 20, 4.5, "https://www.wildcraft.com", "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(39, "All-Weather Automatic Compact Umbrella", "Fashion", 2, "Windproof 10-rib auto open/close compact folding umbrella for daily commute & sun/rain", 599, 35, 4.3, "https://www.amazon.in", "https://images.unsplash.com/photo-1534353436294-0dbd4bdac845?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(40, "Cordless Beard & Hair Trimmer for Daily Grooming", "Fashion", 2, "Self-sharpening stainless steel blades with 20 length settings and 90-min fast charge", 1499, 28, 4.6, "https://www.philips.com", "https://images.unsplash.com/photo-1621607512214-68297480165e?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(41, "Pack of 3 Breathable Cotton Formal Shirts", "Fashion", 2, "Wrinkle-resistant tailored slim fit formal shirts in classic white, sky blue, and grey", 2199, 18, 4.4, "https://www.arrowlife.com", "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?auto=format&fit=crop&w=600&q=80"));

        // Daily Work Productivity & Focus Books: Category 3 (Books)
        defaults.add(new Product(42, "Deep Work: Rules for Focused Success", "Books", 3, "Cal Newport's guide on cultivating intense focus in a distracted world to produce elite output", 599, 45, 4.8, "https://calnewport.com/books/deep-work", "https://images.unsplash.com/photo-1499750310107-5fef28a66643?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(43, "Daily Productivity Planner & Goal Journal", "Books", 3, "Undated 6-month hardcover daily planner with habit trackers, time-blocking, and priorities", 499, 50, 4.7, "https://www.amazon.in", "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(44, "Getting Things Done: The Art of Stress-Free Productivity", "Books", 3, "David Allen's landmark productivity methodology for managing tasks, projects, and daily overload", 699, 0, 4.9, "https://gettingthingsdone.com", "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(45, "The 7 Habits of Highly Effective People", "Books", 3, "Stephen R. Covey's timeless principles for personal integrity, time management, and leadership", 549, 40, 4.8, "https://www.franklincovey.com", "https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=600&q=80"));

        // Daily Chores, Kitchen & Work Routine: Category 4 (Home & Kitchen)
        defaults.add(new Product(46, "Insulated Stainless Steel Lunch Box (3-Tier)", "Home & Kitchen", 4, "Leak-proof thermal bento tiffin box with double-wall insulation for daily office lunches", 1099, 0, 4.6, "https://www.milton.in", "https://images.unsplash.com/photo-1563245372-f21724e3856d?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(47, "Sonic Electric Toothbrush with 4 Brush Heads", "Home & Kitchen", 4, "40,000 VPM ultrasonic motor, 5 cleaning modes, and 30-day battery for daily dental care", 1399, 32, 4.7, "https://www.oralb.com", "https://images.unsplash.com/photo-1559591937-e1032b4b4e10?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(48, "Automatic Touchless Foam Soap Dispenser", "Home & Kitchen", 4, "Rechargeable infrared sensor automatic foaming dispenser for daily hand hygiene", 849, 25, 4.4, "https://www.amazon.in", "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(49, "Cordless Handheld Desk & Keyboard Vacuum", "Home & Kitchen", 4, "Compact 8000Pa suction rechargeable vacuum for daily desk, keyboard, and car cleaning", 1299, 22, 4.5, "https://www.xiaomi.com", "https://images.unsplash.com/photo-1558317374-067fb5f30001?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(50, "High Precision Digital Kitchen & Coffee Scale", "Home & Kitchen", 4, "0.1g accuracy digital scale with LED display and tare function for daily meal and coffee prep", 799, 38, 4.6, "https://www.amazon.in", "https://images.unsplash.com/photo-1588681664899-f142ff2dc9b1?auto=format&fit=crop&w=600&q=80"));

        // Daily Ergonomics & Health Break: Category 5 (Sports)
        defaults.add(new Product(51, "Ergonomic Posture Corrector for Upper Back", "Sports", 5, "Adjustable breathable clavicle support brace to relieve neck and back strain from desk work", 699, 0, 4.5, "https://www.decathlon.in", "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(52, "Adjustable Hand Grip Strengthener (10-60kg)", "Sports", 5, "Ergonomic non-slip forearm and finger strength trainer for daily desk work break exercises", 399, 50, 4.6, "https://www.decathlon.in", "https://images.unsplash.com/photo-1584735935682-2f2b69dff9d2?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(53, "High-Density Foam Roller for Muscle Relief", "Sports", 5, "Deep tissue massage foam roller for daily back, spine, and leg muscle relaxation", 899, 24, 4.7, "https://www.decathlon.in", "https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=600&q=80"));
        defaults.add(new Product(54, "Digital Jump Rope with Calorie Counter", "Sports", 5, "Cordless and roped 2-in-1 speed jump rope with HD LCD counter for quick daily cardio", 649, 35, 4.4, "https://www.decathlon.in", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?auto=format&fit=crop&w=600&q=80"));

        return defaults;
    }
}
