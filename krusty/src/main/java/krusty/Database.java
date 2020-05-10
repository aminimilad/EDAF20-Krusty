package krusty;

import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
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
		
		String cookie = req.queryParams("cookie");
		
		if (!cookieExists(cookie)) {
			return "{\"status\":\"unknown cookie\"}";
		}
		//Will be used to create a pallet ofc
		int orderID = createOrder();
		int palletID = -1;
		String Q = "INSERT INTO Pallets(cookieName, orderID, dateProduced, isBlocked, location) VALUES (?,?,?,?,?)";
		Calendar calendar = Calendar.getInstance();
		Timestamp ourJavaTimestampObject = new Timestamp(calendar.getTime().getTime());
		try(PreparedStatement stmt = conn.prepareStatement(Q)) {
			
			stmt.setString(1,  cookie);
			stmt.setInt(2, orderID);
			stmt.setString(3, ourJavaTimestampObject.toString());
			stmt.setString(4, "no");
			stmt.setString(5, sendLocation());
			stmt.executeUpdate();
			palletID = getIDofLastInsertedRow();
			if(palletID > -1) {
				//Do subtract of stored goods - then update. 
				subtractFromStorage(cookie);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Jsonizer.anythingToJson("error", "status");
		}
		
		return Jsonizer.anythingToJson(palletID, "id");
	}
	
	private boolean cookieExists(String cookieName) {
		String Q = "SELECT * FROM Cookies WHERE cookieName = ?";
		
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
		
		String Q = "INSERT INTO Orders(name, placedDate) VALUES(?,?)";
		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			
			
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
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void subtractFromStorage(String cookieName) {
		//Hashmap to order an ingredient with respective amount
		HashMap<String, Integer> IngAmount = new HashMap<String, Integer>(); 
		
		//In order to set multiple transactions during one commit
		//We must set autocommit = false
		try {
			conn.setAutoCommit(false);
			//Get all ingredients used in the making of the cookie aswell as 
			//the stored amount of each ingredient
			String Q = "SELECT Ingredients.name, amountStored FROM Ingredients \n" + 
					"INNER JOIN ContainsIngredients \n" + 
					"ON ContainsIngredients.ingredient = Ingredients.name \n" + 
					"WHERE cookieName = ?;";
			PreparedStatement stmt = conn.prepareStatement(Q);
			stmt.setString(1, cookieName);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				//Map them...
				IngAmount.put(rs.getString("name"), rs.getInt("amountStored"));
			}
			stmt.close();
			//Get the amount that is used per ingredient...
			Q = "SELECT ingredient, amount FROM ContainsIngredients WHERE cookieName = ?";
			
			stmt = conn.prepareStatement(Q);
			//With cookiename as param
			stmt.setString(1, cookieName);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				
				String ingredient = rs.getString("ingredient");
				int amount = rs.getInt("amount");
				//Update storage by subtracting
				IngAmount.put(ingredient, IngAmount.get(ingredient) - rs.getInt("amount") * 54);
			}
			
			for (Map.Entry<String, Integer> entry : IngAmount.entrySet()) { // Update each ingredient

				Q = "UPDATE Ingredients SET amountStored = ? WHERE name = ?"; //name = ingredient
				stmt = conn.prepareStatement(Q);
				stmt.setInt(1, entry.getValue());
				stmt.setString(2, entry.getKey());
				stmt.executeUpdate();
				stmt.close();

			}

			conn.commit();
			
		
		} catch(Exception e) {
			
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
		}

		try {
			conn.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String sendLocation() {
		
		String Q = "SELECT address FROM Orders, Customers WHERE Orders.name = Customers.name";
		
		try(PreparedStatement stmt = conn.prepareStatement(Q)){
			
			ResultSet rs = stmt.executeQuery();
			if(rs.next()) {
				return rs.getString("address");
			}
			rs.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
