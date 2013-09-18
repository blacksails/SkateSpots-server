import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
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
		
		private final Response response;
		private final Request request;
		
		public Task(Request request, Response response) {
			this.response = response;
			this.request = request;
		}

		@Override
		public void run() {
			try {
				Query query = request.getQuery();
				String accessKey = query.get("accessKey");
				if (accessKey.equals("ourKey")) {
					String type = query.get("type");
					switch (type) {
						case "changeLocation": changeLocation(query);
							break;
						case "getLocations": getLocations();
							break;
						case "createUser": createUser(query);
							break;
						case "login": login(query);
					}
				} else {
					// TODO send error response
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		private void login(Query query) throws Exception {
			String email = '"' + query.get("email") + '"';
			String password = query.get("password");
			String checkEmail = "SELECT * FROM users WHERE email=" + email + ";";
			DatabaseConnection dbConnection = new DatabaseConnection();
			java.sql.Connection con = dbConnection.getDatabaseConnection();
			Statement st = con.createStatement();
			ResultSet checkedEmail = st.executeQuery(checkEmail);
			if (checkedEmail.next()) {
				String checkPassword = "SELECT * FROM users WHERE email=" + email + " AND pass=" + password + ";";
				ResultSet checkedPassword = st.executeQuery(checkPassword);
				if (checkedPassword.next()){
					// TODO respond "logged in"
				} else {
					// TODO respond "wrong password"
				}
			} else {
				// TODO respond "email does not exist"
			}
		}

		private void createUser(Query query) {
			// TODO Auto-generated method stub
			
		}

		private void getLocations() {
			// TODO Auto-generated method stub
			
		}

		private void changeLocation(Query query) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
