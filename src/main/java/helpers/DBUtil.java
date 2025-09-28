package helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import static app.Main.LOGGER;

public class DBUtil {

    public static Connection getConnection() throws SQLException {

        Properties loginProps = new Properties();

        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        try (FileInputStream fis = new FileInputStream("<DIRECTORY OF PROPERTIES FILE HERE>")){
            loginProps.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = loginProps.getProperty("db.url");
        String user = loginProps.getProperty("db.user");
        String password = loginProps.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }

    //ADJUST LOCATION AND PROPERTIES FILE
    public static Connection getRootConnection() throws SQLException {
        Properties loginProps = new Properties();
        try (FileInputStream fis = new FileInputStream("<DIRECTORY OF PROPERTIES FILE HERE>")) {
            loginProps.load(fis);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "PROPERTIES NOT FOUND", e);
        }

        String url = loginProps.getProperty("db.rootUrl");
        String user = loginProps.getProperty("db.user");
        String password = loginProps.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }



    public static void main(String[] args) {
        try {
            @SuppressWarnings("unused")
			Connection conn = getConnection();
            System.out.println("Connection successful!");
        } catch (SQLException e) {

        }
    }
}
