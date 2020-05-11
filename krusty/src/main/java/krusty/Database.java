package krusty;

import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.sqlite.SQLiteConfig;

import static krusty.Jsonizer.toJson;

public class Database {
	/**
	 * Modify it to fit your environment and then use this string when connecting to
	 * your database!
	 */
	private static final String jdbcString = "jdbc:sqlite://C:/Users/milad/krusty/kDB.db";
	private Connection conn;
	/*
	 * For use with MySQL or PostgreSQL
	 */
	SQLiteConfig config = new SQLiteConfig();
	Calendar calendar = Calendar.getInstance();
	String Q;

	public void connect(boolean foreignKeysOn) {
		// Connect to database here
		try {
			SQLiteConfig config = new SQLiteConfig();
			config.enforceForeignKeys(foreignKeysOn);
			conn = DriverManager.getConnection(jdbcString, config.toProperties());
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	// TODO: Implement and change output in all methods below!

	public String getCustomers(Request req, Response res) {

		Q = "SELECT * FROM Customers";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "customers");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "{}";
	}

	public String getRawMaterials(Request req, Response res) {
		Q = "SELECT name, amountStored AS amount, unit FROM Ingredients";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "raw-materials");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "{}";
	}

	public String getCookies(Request req, Response res) {
		Q = "SELECT cookieName AS name FROM Cookies";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "cookies");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "{\"cookies\":[]}";
	}

	public String getRecipes(Request req, Response res) {
		Q = "SELECT * FROM containsIngredient";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			return Jsonizer.toJson(stmt.executeQuery(), "recipe");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "{}";
	}

	public String getPallets(Request req, Response res) {

		Q = "SELECT Pallets.cookieName AS cookie, Pallets.isBlocked AS blocked, "
				+ "Pallets.dateProduced AS production_date, " + "Customers.name AS customer "
				+ "FROM Pallets Join Orders USING(orderID) Join Customers USING(name) ";

		ArrayList<String> values = new ArrayList<>();
		String json = "";
		String from = req.queryParams("from");
		String to = req.queryParams("to");
		String cookie = req.queryParams("cookie");
		String blocked = req.queryParams("blocked");

		if (from != null || to != null || cookie != null || blocked != null) {
			Q += "WHERE ";
		}
		if (from != null) {
			Q += "production_date >= ? ";
			values.add(from);
		}
		if (to != null) {
			if (values.size() > 0)
				Q += "AND ";
			Q += "production_date <= ? ";
			values.add(to);
		}
		if (cookie != null) {
			if (values.size() > 0)
				Q += "AND ";
			Q += "cookie = ? ";
			values.add(cookie);
		}

		if (blocked != null) {
			if (values.size() > 0)
				Q += "AND ";
			Q += "blocked = ? ";
			values.add(blocked);
		}
		// Check other query parameters

		try {
			// Check
			System.out.print(Q);

			PreparedStatement stmt = conn.prepareStatement(Q);
			for (int i = 0; i < values.size(); i++) {
				stmt.setString(i + 1, values.get(i));
			}

			json = Jsonizer.toJson(stmt.executeQuery(), "pallets");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return json;
	}

	public String reset(Request req, Response res) {
		// Turn off FK
		connect(false);
		// do stuff

		removeRows("Pallets");
		removeRows("Orders");
		removeRows("Customers");
		removeRows("Cookies");
		removeRows("Ingredients");

		insertCustomer("Bjudkakor AB", "Ystad");
		insertCustomer("Finkakor AB", "Helsingborg");
		insertCustomer("Gästkakor AB", "Hässleholm");
		insertCustomer("Kaffebröd AB", "Landskrona");
		insertCustomer("Kalaskakor AB", "Trelleborg");
		insertCustomer("Partykakor AB", "Kristianstad");
		insertCustomer("Skånekakor AB", "Perstorp");
		insertCustomer("Småbröd AB", "Malmö");

		insertCookie("Amneris");
		insertCookie("Berliner");
		insertCookie("Nut cookie");
		insertCookie("Nut ring");
		insertCookie("Tango");
		insertCookie("Almond delight");

		insertIng("Bread crumbs", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Butter", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Chocolate", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Chopped almonds", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Cinnamon", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Egg whites", 500000, LocalDate.now().toString(), 500000, "ml");
		insertIng("Eggs", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Fine-ground nuts", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Flour", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Ground, roasted nuts", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Icing sugar", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Marzipan", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Potato starch", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Roasted, chopped nuts", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Sodium bicarbonate", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Sugar", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Vanilla sugar", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Vanilla", 500000, LocalDate.now().toString(), 500000, "g");
		insertIng("Wheat flour", 500000, LocalDate.now().toString(), 500000, "g");

		connect(true);

		return "{}";
	}

	public String createPallet(Request req, Response res) {

		String cookie = req.queryParams("cookie");

		if (!cookieExists(cookie)) {
			return "{\"status\":\"unknown cookie\"}";
		}
		// Will be used to create a pallet ofc
		int orderID = createOrder();
		int palletID = -1;
		Q = "INSERT INTO Pallets(cookieName, orderID, dateProduced, isBlocked, location) VALUES (?,?,?,?,?)";
		try (PreparedStatement stmt = conn.prepareStatement(Q)) {

			stmt.setString(1, cookie);
			stmt.setInt(2, orderID);
			stmt.setString(3, LocalDate.now().toString());
			stmt.setString(4, "no");
			stmt.setString(5, sendLocation(orderID));
			stmt.executeUpdate();
			palletID = getIDofLastInsertedRow();
			if (palletID > -1) {
				// Do subtract of stored goods.
				subtractFromStorage(cookie);
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
			return Jsonizer.anythingToJson("error", "status");
		}

		return Jsonizer.anythingToJson(palletID, "id");
	}

	private boolean cookieExists(String cookieName) {
		Q = "SELECT * FROM Cookies WHERE cookieName = ?";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			stmt.setString(1, cookieName);
			ResultSet rs = stmt.executeQuery();
			// Om kaka hitta
			if (rs.next()) {
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

		Q = "INSERT INTO Orders(name, placedDate) VALUES(?,?)";
		try (PreparedStatement stmt = conn.prepareStatement(Q)) {

			stmt.setString(1, getCustomer());
			stmt.setString(2, LocalDate.now().toString());
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
			if (rs.next()) {
				// return the last inserted row by looking up ID
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

		Q = "SELECT name FROM Customers ORDER BY RANDOM() LIMIT 1";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
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
		// Hashmap to order an ingredient with respective amount
		HashMap<String, Integer> IngAmount = new HashMap<String, Integer>();

		// In order to set multiple transactions during one commit
		// We must set autocommit = false
		try {
			conn.setAutoCommit(false);
			// Get all ingredients used in the making of the cookie aswell as
			// the stored amount of each ingredient
			Q = "SELECT Ingredients.name, amountStored FROM Ingredients \n" + "INNER JOIN ContainsIngredients \n"
					+ "ON ContainsIngredients.ingredient = Ingredients.name \n" + "WHERE cookieName = ?;";
			PreparedStatement stmt = conn.prepareStatement(Q);
			stmt.setString(1, cookieName);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				
				// Map them...
				IngAmount.put(rs.getString("name"), rs.getInt("amountStored"));
			}
			stmt.close();
			// Get the amount that is used per ingredient...
			Q = "SELECT ingredient, amount FROM ContainsIngredients WHERE cookieName = ?";

			stmt = conn.prepareStatement(Q);
			// With cookiename as param
			stmt.setString(1, cookieName);
			rs = stmt.executeQuery();

			while (rs.next()) {
				
				
				String ingredient = rs.getString("ingredient");
				int amount = rs.getInt("amount");
				
				// Update storage by subtracting
				IngAmount.put(ingredient, IngAmount.get(ingredient) - rs.getInt("amount") * 54);
			}

			for (Map.Entry<String, Integer> entry : IngAmount.entrySet()) { // Update each ingredient

				Q = "UPDATE Ingredients SET amountStored = ? WHERE name = ?"; // name = ingredient
				stmt = conn.prepareStatement(Q);
				stmt.setInt(1, entry.getValue());
				stmt.setString(2, entry.getKey());
				stmt.executeUpdate();
				stmt.close();

			}

			conn.commit();

		} catch (Exception e) {

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

	private String sendLocation(int orderID) {

		Q = "SELECT Customers.address from Customers JOIN Orders ON Orders.name = Customers.name WHERE orderID = ?";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			stmt.setInt(1, orderID);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getString("address");
			}
			rs.close();

		} catch (SQLException err) {
			// TODO Auto-generated catch block
			err.printStackTrace();
		}

		return null;
	}

	// -----//RESET METHODS//-----//

	private void removeRows(String table) {
		Q = "DELETE FROM " + table;

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertCookie(String cookieName) {
		Q = "INSERT INTO Cookies(cookieName) VALUES (?)";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			stmt.setString(1, cookieName);

			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void insertIng(String ing, int amountStored, String dateNow, int deliveryAmount, String unit) {
		Q = "INSERT INTO " + "Ingredients(name, amountStored, deliveryDate, deliveryAmount, unit) VALUES(?,?,?,?,?)";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			stmt.setString(1, ing);
			stmt.setInt(2, amountStored);
			stmt.setString(3, dateNow);
			stmt.setInt(4, deliveryAmount);
			stmt.setString(5, unit);

			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void insertCustomer(String name, String address) {
		Q = "INSERT INTO Customers(name, address) VALUES (?,?)";

		try (PreparedStatement stmt = conn.prepareStatement(Q)) {
			stmt.setString(1, name);
			stmt.setString(2, address);

			stmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
