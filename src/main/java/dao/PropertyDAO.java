package dao;

import helpers.DBUtil;
import models.Address;
import models.PricingEntry;
import models.Property;
import models.Review;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static app.Main.LOGGER;

public class PropertyDAO {
    //EACH REQUIRES OWN DAO
//        private List<String> photosURL;
//        private List<String> facilities;
//        private List<Review> reviews;
//        private List<PricingEntry> priceList;

    //CREATES/INITIALIZES PROPERTIES TABLE IN DB
    public static boolean initializePropertiesTable(){

        String tableStmt = "CREATE TABLE IF NOT EXISTS Properties( "
                + "propertyID VARCHAR(13) PRIMARY KEY, "
                + "ownerID VARCHAR(13) REFERENCES PropertyOwners(ownerID), "
                + "propertyName VARCHAR(40), "
                + "description VARCHAR(255), "
                + "street VARCHAR(100), "
                + "suite VARCHAR(100), "
                + "city VARCHAR(100), "
                + "state VARCHAR(100), "
                + "postalCode VARCHAR(10), "
                + "country VARCHAR (100), "
                + "latitude DOUBLE PRECISION, "
                + "longitude DOUBLE PRECISION, "
                + "houseSize DOUBLE PRECISION, "
                + "landSize DOUBLE PRECISION, "
                + "baseRate DECIMAL"
                + ");";

        try(Connection dbConn = DBUtil.getConnection();
            Statement stmt = dbConn.createStatement()){

            stmt.executeUpdate(tableStmt);
            LOGGER.info("Table 'Properties' created or already exists.");
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "DATABASE CONNECTION FAILED" + e);
            return false;
        }
    }

    //--------------------CRUD OPERATIONS-------------------------//

    //SAVES PROPERTY TO DB
    public static boolean saveProperty(Property p){

        if (initializePropertiesTable()){
            String sql = "INSERT INTO Properties (propertyID, ownerID, propertyName, description, street, suite, city, state, postalCode, " +
                    "country, latitude, longitude, houseSize, landSize, baseRate)"
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

            try(Connection dbConn = DBUtil.getConnection();
                PreparedStatement stmt = dbConn.prepareStatement(sql)){

                stmt.setString(1, p.getPropertyID());
                stmt.setString(2, p.getOwnerID());
                stmt.setString(3, p.getPropertyName());
                stmt.setString(4, p.getDescription());
                stmt.setString(5, p.getAddress().getStreet());
                stmt.setString(6, p.getAddress().getSuite());
                stmt.setString(7, p.getAddress().getCity());
                stmt.setString(8, p.getAddress().getState());
                stmt.setString(9, p.getAddress().getPostalCode());
                stmt.setString(10, p.getAddress().getCountry());
                stmt.setDouble(11, p.getLatitude());
                stmt.setDouble(12, p.getLongitude());
                stmt.setDouble(13, p.getHouseSize());
                stmt.setDouble(14, p.getLandSize());
                stmt.setBigDecimal(15, p.getBaseRate());

                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;


            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "SAVING PROPERTY TO DB FAILED" + e);
                return false;
            }

        }else{
            LOGGER.log(Level.SEVERE, "INITIALIZATION OF PROPERTIES TABLE HAS FAILED");
            return false;
        }


    }


    //RETRIEVES PROPERTY FROM DB USING PROPERTY ID
    public static Property retrievePropertyByPropertyID(String propertyID){

        String sql = "SELECT * FROM Properties WHERE propertyID = ?";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, propertyID);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()){
                Property p = new Property();
                p.setPropertyID(rs.getString("propertyID"));
                p.setOwnerID(rs.getString("ownerID"));
                p.setPropertyName(rs.getString("propertyName"));
                p.setDescription(rs.getString("description"));
                    String street = rs.getString("street");
                    String suite = rs.getString("suite");
                    String city = rs.getString("city");
                    String state = rs.getString("state");
                    String postalCode = rs.getString("postalCode");
                    String country = rs.getString("country");
                p.setAddress(new Address(street, suite, city, state, postalCode, country));
                p.setLatitude(rs.getDouble("latitude"));
                p.setLongitude(rs.getDouble("longitude"));
                p.setHouseSize(rs.getDouble("houseSize"));
                p.setLandSize(rs.getDouble("landSize"));
                p.setBaseRate(rs.getBigDecimal("baseRate"));

                return p;

            }else{
                return null;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "FAILED TO RETRIEVE PROPERTY" + e);
            return null;
        }

    }


    //RETRIEVES PROPERTIES FROM DB USING OWNER ID
    public static List<Property> retrievePropertiesByOwnerID(String ownerID){

        String sql = "SELECT * FROM Properties WHERE ownerID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, ownerID);

            ResultSet rs = stmt.executeQuery();

            List<Property> propertyList = new ArrayList<>();

            while (rs.next()){
                Property p = new Property();
                p.setPropertyID(rs.getString("propertyID"));
                p.setOwnerID(rs.getString("ownerID"));
                p.setPropertyName(rs.getString("propertyName"));
                p.setDescription(rs.getString("description"));
                String street = rs.getString("street");
                String suite = rs.getString("suite");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String postalCode = rs.getString("postalCode");
                String country = rs.getString("country");
                p.setAddress(new Address(street, suite, city, state, postalCode, country));
                p.setLatitude(rs.getDouble("latitude"));
                p.setLongitude(rs.getDouble("longitude"));
                p.setHouseSize(rs.getDouble("houseSize"));
                p.setLandSize(rs.getDouble("landSize"));
                p.setBaseRate(rs.getBigDecimal("baseRate"));

                propertyList.add(p);
            }

            return propertyList;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "FAILED TO SAVE PROPERTY" + e);
            return null;
        }

    }


    //RETRIEVES PROPERTIES FROM DB USING OWNER ID
    public static List<Property> retrieveAllProperties(){

        String sql = "SELECT * FROM Properties;";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            ResultSet rs = stmt.executeQuery();

            List<Property> propertyList = new ArrayList<>();

            while (rs.next()){
                Property p = new Property();
                p.setPropertyID(rs.getString("propertyID"));
                p.setOwnerID(rs.getString("ownerID"));
                p.setPropertyName(rs.getString("propertyName"));
                p.setDescription(rs.getString("description"));
                String street = rs.getString("street");
                String suite = rs.getString("suite");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String postalCode = rs.getString("postalCode");
                String country = rs.getString("country");
                p.setAddress(new Address(street, suite, city, state, postalCode, country));
                p.setLatitude(rs.getDouble("latitude"));
                p.setLongitude(rs.getDouble("longitude"));
                p.setHouseSize(rs.getDouble("houseSize"));
                p.setLandSize(rs.getDouble("landSize"));
                p.setBaseRate(rs.getBigDecimal("baseRate"));

                propertyList.add(p);
            }

            return propertyList;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "FAILED TO RETRIEVE PROPERTY LIST" + e);
            return null;
        }

    }


    //UPDATE PROPERTY IN DB
    public static boolean updateProperty(Property p){

        String sql = "UPDATE Properties "
                + "SET ownerID = ?, "
                + "propertyName = ?, "
                + "description = ?, "
                + "street = ?, "
                + "suite = ?, "
                + "city = ?, "
                + "state = ?, "
                + "postalCode = ?, "
                + "country = ?, "
                + "latitude = ?, "
                + "longitude = ?, "
                + "houseSize = ?, "
                + "landSize = ?, "
                + "baseRate = ? "
                + "WHERE propertyID = ?";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){


            stmt.setString(1, p.getOwnerID());
            stmt.setString(2, p.getPropertyName());
            stmt.setString(3, p.getDescription());
            stmt.setString(4, p.getAddress().getStreet());
            stmt.setString(5, p.getAddress().getSuite());
            stmt.setString(6, p.getAddress().getCity());
            stmt.setString(7, p.getAddress().getState());
            stmt.setString(8, p.getAddress().getPostalCode());
            stmt.setString(9, p.getAddress().getCountry());
            stmt.setDouble(10, p.getLatitude());
            stmt.setDouble(11, p.getLongitude());
            stmt.setDouble(12, p.getHouseSize());
            stmt.setDouble(13, p.getLandSize());
            stmt.setBigDecimal(14, p.getBaseRate());

            stmt.setString(15, p.getPropertyID());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "FAILED PROPERTY UPDATE");
            return false;
        }
    }


    //DELETE PROPERTY FROM DB
    public static boolean deleteProperty(String propertyID) {

        String sql = "DELETE FROM Properties WHERE propertyID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyID);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "FAILED PROPERTY DELETION" + e);
            return false;
        }

    }

}
