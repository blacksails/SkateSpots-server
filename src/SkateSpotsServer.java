import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.ResponseWrapper;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SkateSpotsServer implements Container {

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
		private java.sql.Connection con; 
		private Statement st;
		private ResultSet res;

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
				String content = request.getContent();
				JsonObject obj = new JsonParser().parse(content).getAsJsonObject();
				if (obj.get("key").getAsString().equals("ourKey")) { //TODO Define our key
					Integer type = obj.get("type").getAsInt();
					switch (type) {
					case 0: login(obj);
					break;
					case 1: createUser(obj);
					break;
					default: response.setStatus(Status.BAD_REQUEST);
					}
				}
				body.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void login(JsonObject obj) {
			try {
				String email = '"'+obj.get("email").getAsString()+'"';
				String password = '"'+obj.get("password").getAsString()+'"';
				System.out.println(email+" is trying to login with the password:"+password);
				
				if (email != null && password != null) {
					con = new DatabaseConnection().getDatabaseConnection();
					if (con == null) System.out.println("Error establishing the database connection");
					st = con.createStatement();
					String checkUser = "SELECT * FROM users WHERE email="+email+" AND pass="+password+";";
					res = st.executeQuery(checkUser);
					if (res.next()) {
						System.out.println(email+" has been accepted");
						// email and password matches
						response.setStatus(Status.OK);
					} else {
						System.out.println(email+" has been rejected");
						// wrong email or password
						response.setStatus(Status.BAD_REQUEST);
					}
				} else {
					// invalid request
					response.setStatus(Status.BAD_REQUEST);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}

		private void createUser(JsonObject obj) {
			try {
				String email = '"'+obj.get("email").getAsString()+'"';
				String password = '"'+obj.get("password").getAsString()+'"';
				String displayname = '"'+obj.get("displayname").getAsString()+'"';
				if (email != null && password != null && displayname != null) {
					con = new DatabaseConnection().getDatabaseConnection();
					st = con.createStatement();
					String checkIfExists = "SELECT * FROM users WHERE email="+email+";";
					res = st.executeQuery(checkIfExists);
					if (!res.next()) {
						// User does not exist and is therefore created
						String createUser = "INSERT INTO users VALUES ("+email+", "+password+", "+displayname+");";
						st.execute(createUser);
						response.setStatus(Status.OK);
					} else {
						// User with the given email already exists
						response.setStatus(Status.BAD_REQUEST);
					}
				} else {
					// invalid request
					response.setStatus(Status.BAD_REQUEST);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}
		
		// Closes the remains of the database connection
		private void close() {
			try {
				res.close();
				st.close();
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
