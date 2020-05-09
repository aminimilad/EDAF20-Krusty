package krusty;

import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static krusty.Jsonizer.toJson;

public class Database {
	/**
	 * Modify it to fit your environment and then use this string when connecting to your database!
	 */
	private static final String jdbcString = "jdbc:sqlite://C:/Users/milad/krusty/kDB.db";
	private Connection conn;
	/* For use with MySQL or PostgreSQL
	private static final String jdbcUsername = "<CHANGE ME>";
	private static final String jdbcPassword = "<CHANGE ME>" I changed you.. you gone grey fr doe
	*/

	public void connect() {
		// Connect to database here
		try {
			conn = DriverManager.getConnection(jdbcString);
			System.out.println(conn);
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}

	// TODO: Implement and change output in all methods below!

	public String getCustomers(Request req, Response res) {
		
		String Q = "SELECT * FROM Customers";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "customers");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "{}";
	}

	public String getRawMaterials(Request req, Response res) {
		String Q = "SELECT name, amountStored AS amount, unit FROM Ingredients";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)){
			return Jsonizer.toJson(stmt.executeQuery(), "raw-materials");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{}";
	}

	public String getCookies(Request req, Response res) {
		String Q = "SELECT cookieName AS name FROM Cookies";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "cookies");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return "{\"cookies\":[]}";
	}

	public String getRecipes(Request req, Response res) {
		String Q = "SELECT * FROM containsIngredient";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "recipe");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "{}";
	}

	public String getPallets(Request req, Response res) {
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		
		String cookie = req.queryParams("cookies");
		
		if (!cookieExists(cookie)) {
			return "{\"status\":\"unknown cookie\"}";
		}
		//Will be used to create a pallet ofc
		int orderID = createOrder();
		
		String Q = "INSERT INTO Pallets(cookieName, orderID, dateProduced, isBlocked, location) VALUES (?,?,?,?,?)";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)) {
			
			stmt.setString(1,  cookie);
			stmt.setInt(2, orderID);
			stmt.setString(3, "CURRENT_TIMESTAMP");
			stmt.setString(4, "no");
			stmt.setString(4, "SELECT address FROM Orders, Customers WHERE Orders.name = Customers.name");
			stmt.executeUpdate();
			
			if(getIDofLastInsertedRow() > -1) {
				//Do subtract of stored goods - then update.
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "{}";
	}
	
	private boolean cookieExists(String cookieName) {
		String Q = "SELECT * FROM Cookies WHERE name = ?";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)) {
			stmt.setString(1, cookieName);
			ResultSet rs = stmt.executeQuery();
			//Om kaka hitta
			if(rs.next()) {
				rs.close();
				return true;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	private int createOrder() {
		
		String Q = "INSERT INTO Order(name, placedDate) VALUES(?,?)";
		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			
			ResultSet rs = stmt.executeQuery();
			stmt.setString(1, getCustomer());
			stmt.setString(2, "CURRENT_TIMESTAMP");
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return getIDofLastInsertedRow();
	}
	
	private int getIDofLastInsertedRow() {
		// Following code block for finding last inserted id
				try (PreparedStatement stmt = conn.prepareStatement("SELECT LAST_INSERT_ROWID() AS id")) {
					
					ResultSet rs = stmt.executeQuery();
					if(rs.next()) {
						//return the last inserted row by looking up ID
						return rs.getInt("id");
					}
					rs.close();
					
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return -99;
	}
	
	private String getCustomer() {
		
		String Q = "SELECT name FROM Customers ORDER BY RANDOM() LIMIT 1";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)){
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString("name");
			}
			rs.close();
			
		} catch (SQLException ero) {
			// TODO Auto-generated catch block
			ero.printStackTrace();
		}
		
		return null;
	}
	
	private void subtractFromstorage(String cookieName) {
		
	}
}
