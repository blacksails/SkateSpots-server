import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
	private Connection con = null;
	private String host = "bufo.avalonia.dk";
	private String database = "skatespots";
	private String username = "skatespots";
	private String password = "sKATEsPOTS";
	
	public DatabaseConnection() throws Exception {
		String URL = "jdbc:mysql://" + host + "/" + database;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(URL, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Connection getDatabaseConnection() {
		return con;
	}
}
