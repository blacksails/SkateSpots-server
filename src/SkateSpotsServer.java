import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.ResponseWrapper;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;


public class SkateSpotsServer implements Container{
	
	private final Executor executor;
	
	public static void main(String[] list) throws Exception {
		Container container = new SkateSpotsServer();
		Server server = new ContainerServer(container);
		@SuppressWarnings("resource")
		Connection connection = new SocketConnection(server);
		SocketAddress address = new InetSocketAddress(11337);
		connection.connect(address);
	}
	
	public SkateSpotsServer() {
		this.executor = Executors.newFixedThreadPool(10);
	}

	@Override
	public void handle(Request request, Response response) {
		Task task = new Task(request, response);
		executor.execute(task);
	}
	
	public static class Task implements Runnable {
		
		private final ResponseWrapper response;
		private final Request request;
		private PrintStream body;
		
		public Task(Request request, Response response) {
			this.response = new ResponseWrapper(response);
			this.request = request;
			this.response.setContentType("application/json");
			this.response.setValue("Server", "Skate Spots - Server 1.0");
		}

		@Override
		public void run() {
			try {
				body = this.response.getPrintStream();
				
				body.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		private void login(Query query) throws Exception {
			String email = '"'+query.get("email")+'"';
			String password = '"'+query.get("password")+'"';
			DatabaseConnection dbConnection = new DatabaseConnection();
			java.sql.Connection con = dbConnection.getDatabaseConnection();
			Statement st = con.createStatement();
			
			String checkAccount = "SELECT * FROM users WHERE email="+email+"AND pass="+password+";";
			ResultSet checkedAccount = st.executeQuery(checkAccount);
			
			if (checkedAccount.next()) {
				response.setStatus(Status.OK);
				body.println("OK: 200");
			} else {
				response.setCode(420); // The password or the email address is incorrect
				body.println("Wrong password or email address: 420");
			}
			// Close database connection
			con.close();
		}

		private void createUser(Query query) throws Exception {
			String email = '"'+query.get("email")+'"';
			String password = '"'+query.get("password")+'"';
			String displayname = '"'+query.get("displayname")+'"';
			DatabaseConnection dbConnection = new DatabaseConnection();
			java.sql.Connection con = dbConnection.getDatabaseConnection();
			Statement st = con.createStatement();
			
			String checkAccount = "SELECT * FROM users WHERE email="+email+";";
			ResultSet checkedAccount = st.executeQuery(checkAccount);
			
			if (checkedAccount.next()) {
				response.setCode(421); // There is already an account with that email address
				body.println("Email is already registered: 421");
			} else {
				String insertUser = "INSERT INTO users VALUES ('"+email+"', '"+password+"', '"+displayname+"');";
				st.execute(insertUser); 
				response.setStatus(Status.OK);
				body.println("OK: 200");
			}
			// Close database connection
			con.close();
		}

		private void getLocations() {
			// TODO Auto-generated method stub
			
		}

		private void changeLocation(Query query) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
