package helpers;

import java.sql.*;
import java.util.logging.Level;

import static app.Main.LOGGER;


public class MSSQLConnect {
	
	public static void main(String[] args) {


		String url = "jdbc:sqlserver://ODANE\\SQLEXPRESS;databaseName=VapfumiConnect;integratedSecurity=true;encrypt=true;trustServerCertificate=true";

		 try {
	            Connection conn = DriverManager.getConnection(url);
	            System.out.println("Connected to SQL Server successfully!");

	            Statement stmt = conn.createStatement();
	            ResultSet rs = stmt.executeQuery("SELECT * FROM [Users]");

	            while (rs.next()) {
	                System.out.println("Data: " + rs.getString(1)); // Update column index/name
	            }

	            rs.close();
	            stmt.close();
	            conn.close();
	        } catch (SQLException e) {
			 LOGGER.log(Level.SEVERE, "Connection to SQL Server has failed", e);
	        }

	}

}
