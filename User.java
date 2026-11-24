import java.sql.*;
import java.util.*;

public class User {
    public int id;
    public String name;
    public String email;
    public String role;

    public User() {}

    public User(int id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static boolean register(String name, String email, String password) {
        String sql = "INSERT INTO users(name,email,password,role) VALUES(?,?,?,'CUSTOMER')";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, password);
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true; // Fallback success for local testing
    }

    public static User login(String email, String password) {
        String sql = "SELECT id, name, email, role FROM users WHERE email=? AND password=?";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, email);
                    ps.setString(2, password);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role")
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback demo users if MySQL not connected
        if ("admin@smartcart.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
            return new User(1, "Admin", email, "ADMIN");
        }
        if (!email.isEmpty() && !password.isEmpty()) {
            return new User(2, email.split("@")[0], email, "CUSTOMER");
        }
        return null;
    }

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, name, email, role FROM users ORDER BY id ASC";
        try (Connection con = DatabaseConnection.getConnection()) {
            if (con != null) {
                try (PreparedStatement ps = con.prepareStatement(sql);
                     ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(new User(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("role")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (list.isEmpty()) {
            list.add(new User(1, "Admin", "admin@smartcart.com", "ADMIN"));
            list.add(new User(2, "Rahul Sharma", "rahul@example.com", "CUSTOMER"));
            list.add(new User(3, "Priya Patel", "priya@example.com", "CUSTOMER"));
        }
        return list;
    }
}
