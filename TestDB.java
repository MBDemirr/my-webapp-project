import java.io.File;
import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String dbPath = tempDir + File.separator + "my-webapp-todo.db";
        
        System.out.println("Temp directory: " + tempDir);
        System.out.println("Database path: " + dbPath);
        System.out.println("File exists before: " + new File(dbPath).exists());
        
        String dbUrl = "jdbc:sqlite:" + dbPath;
        
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver loaded");
            
            Connection conn = DriverManager.getConnection(dbUrl);
            System.out.println("Connection established");
            
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, completed INTEGER)");
            System.out.println("Table created/verified");
            
            System.out.println("File exists after: " + new File(dbPath).exists());
            System.out.println("File size: " + new File(dbPath).length() + " bytes");
            
            stmt.close();
            conn.close();
            
            System.out.println("SUCCESS: Database is working");
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
