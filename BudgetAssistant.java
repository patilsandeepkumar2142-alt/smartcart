import java.sql.*;
import java.util.*;

public class BudgetAssistant {
    public static List<Product> findProductsList(double budget, String category, String requirement) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.id, p.name, c.name AS category, p.category_id, p.description, p.price, p.stock, p.rating, p.website_url FROM products p " +
                     "JOIN categories c ON p.category_id=c.id " +
                     "WHERE p.price <= ? AND LOWER(c.name) LIKE LOWER(?) " +
                     "AND LOWER(p.name) LIKE LOWER(?) ORDER BY p.rating DESC";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, budget);
                    ps.setString(2, "%" + (category != null ? category : "") + "%");
                    ps.setString(3, "%" + (requirement != null ? requirement : "") + "%");
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String siteUrl = null;
                        try {
                            siteUrl = rs.getString("website_url");
                        } catch (Exception ignored) {}

                        list.add(new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("category"),
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
            e.printStackTrace();
        }

        // Fallback filter over default products if database returns empty
        if (list.isEmpty()) {
            String cleanCat = (category != null) ? category.trim().toLowerCase().replace("\\u0026", "&") : "";
            String cleanReq = (requirement != null) ? requirement.trim().toLowerCase() : "";
            for (Product p : Product.getAllProducts()) {
                boolean matchBudget = budget <= 0 || p.price <= budget;
                String pCat = (p.category != null) ? p.category.toLowerCase() : "";
                boolean matchCategory = cleanCat.isEmpty() || pCat.equals(cleanCat) || pCat.contains(cleanCat) || cleanCat.contains(pCat);
                boolean matchReq = cleanReq.isEmpty() || p.name.toLowerCase().contains(cleanReq) || (p.description != null && p.description.toLowerCase().contains(cleanReq));
                if (matchBudget && matchCategory && matchReq) {
                    list.add(p);
                }
            }
        }
        return list;
    }

    public static void findProducts(double budget, String category, String requirement) {
        List<Product> list = findProductsList(budget, category, requirement);
        for (Product p : list) {
            System.out.println(p.name + " | ₹" + p.price + " | Rating: " + p.rating);
        }
    }
}
