package krusty;

import spark.Request;
import spark.Response;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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
	private static final String jdbcPassword = "<CHANGE ME>"
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
		
		String query = "SELECT * FROM Customers";

		try (PreparedStatement statement = conn.prepareStatement(query)) {
			return Jsonizer.toJson(statement.executeQuery(), "customers");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "{}";
	}

	public String getRawMaterials(Request req, Response res) {
		return "{}";
	}

	public String getCookies(Request req, Response res) {
		return "{\"cookies\":[]}";
	}

	public String getRecipes(Request req, Response res) {
		return "{}";
	}

	public String getPallets(Request req, Response res) {
		return "{\"pallets\":[]}";
	}

	public String reset(Request req, Response res) {
		return "{}";
	}

	public String createPallet(Request req, Response res) {
		return "{}";
	}
}
