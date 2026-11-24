public class Main {
    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("🚀 STARTING SMARTCART FULL-STACK APPLICATION");
        System.out.println("=================================================");

        // Test Database Connectivity
        try {
            if (DatabaseConnection.getConnection() != null) {
                System.out.println("✅ MySQL Database Connected Successfully (smartcart)!");
            } else {
                System.out.println("⚠️ MySQL Database not reachable at localhost:3306. Starting in standalone hybrid fallback mode.");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Database check: " + e.getMessage());
        }

        // Start Built-in HTTP Full-stack Web Server
        SmartCartServer.startServer();
    }
}
