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
					case 2: setCurrentLocation(obj);
					break;
					case 3: getCurrentLocations(obj);
					break;
					default: response.setStatus(Status.BAD_REQUEST);
					}
				}
				body.close();
			} catch (IOException e) {
				response.setStatus(Status.INTERNAL_SERVER_ERROR);
				e.printStackTrace();
			}
		}

		private void login(JsonObject obj) {
			try {
				// Creating required strings
				String email = '"'+obj.get("email").getAsString()+'"';
				String password = '"'+obj.get("password").getAsString()+'"';
				String checkUser = "SELECT * FROM users WHERE email="+email+" AND pass="+password+";";
				// Establish dbconnection and a statement, and execute the prepared sql
				con = new DatabaseConnection().getDatabaseConnection();
				st = con.createStatement();
				res = st.executeQuery(checkUser);
				System.out.println(email+" is trying to login");
				if (res.next()) {
					// email and password matches
					System.out.println(email+" has been accepted");
					response.setStatus(Status.OK);
				} else {
					// wrong email or password
					System.out.println(email+" has been rejected");
					response.setStatus(Status.BAD_REQUEST);
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.INTERNAL_SERVER_ERROR);
			} finally {
				close();
			}
		}

		private void createUser(JsonObject obj) {
			try {
				// Creating required strings
				String email = '"'+obj.get("email").getAsString()+'"';
				String password = '"'+obj.get("password").getAsString()+'"';
				String displayname = '"'+obj.get("displayname").getAsString()+'"';
				String checkIfExists = "SELECT * FROM users WHERE email="+email+";";
				// Establish dbconnection and a statement, and execute the prepared sql
				con = new DatabaseConnection().getDatabaseConnection();
				st = con.createStatement();
				res = st.executeQuery(checkIfExists);
				System.out.println("Attempts to create user:"+email);
				if (!res.next()) {
					// User does not exist and is therefore created
					String createUser = "INSERT INTO users(email,pass,displayname) VALUES ("+email+", "+password+", "+displayname+");";
					st.execute(createUser);
					System.out.println("User does not exist: Created user");
					response.setStatus(Status.OK);
				} else {
					// User with the given email already exists
					System.out.println("User already exists: User not created");
					response.setStatus(Status.BAD_REQUEST);
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.INTERNAL_SERVER_ERROR);
			} finally {
				close();
			}
		}
		
		private void setCurrentLocation(JsonObject obj) {
			try {
				// Creating required strings
				String email = '"'+obj.get("email").getAsString()+'"';
				Double latitude = '"'+obj.get("latitude").getAsDouble()+'"';
				Double longitude = '"'+obj.get("longitude").getAsDouble()+'"';
				String updateLocation = "UPDATE users SET latitude="+latitude+", longitude="+longitude+" WHERE email="+email+";";
				// Establish dbconnection and a statement, and execute the prepared sql
				con = new DatabaseConnection().getDatabaseConnection();
				st = con.createStatement();
				st.execute(updateLocation);
				System.out.println("Updated the location of "+email);
				// We had success
				response.setStatus(Status.OK);
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(Status.INTERNAL_SERVER_ERROR);
			} finally {
				close();
			}
		}

		private void getCurrentLocations(JsonObject obj) {
			try {
				// TODO implement this
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Closes the remains of the database connection
		private void close() {
			try {
				if (res != null) {
					res.close();
				}
				if (st != null) {
					st.close();
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
