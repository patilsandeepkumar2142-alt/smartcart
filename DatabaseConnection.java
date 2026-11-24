import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/smartcart",
                "root",
                "Sandeep@2887"
            );

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}