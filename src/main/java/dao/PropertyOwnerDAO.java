package dao;

import helpers.DBUtil;
import models.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.logging.Level;

import static app.Main.LOGGER;

public class PropertyOwnerDAO {

    //INITIALIZE DATABASE
    public static boolean initializePropertyOwner() {
        String tableStmt = "CREATE TABLE IF NOT EXISTS PropertyOwners (" +
                "ownerID VARCHAR(13) PRIMARY KEY, " +
                "email VARCHAR(80) NOT NULL UNIQUE, " +
                "balance DECIMAL, " +
                "totalEarnings DECIMAL " +
                ");";

        try (
                Connection dbConn = DBUtil.getConnection(); // Connects directly to vapfumiconnect DB
                Statement dbStmt = dbConn.createStatement()
        ) {
            dbStmt.executeUpdate(tableStmt);
            LOGGER.info("Table 'PropertyOwners' created or already exists.");
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize table", e);
            return false;
        }
    }


    //-----------------------------------CRUD OPERATIONS---------------------------------------------------

    //SAVES USER TO DATABASE
    public static boolean savePropertyOwner(PropertyOwner pO) {

        initializePropertyOwner();
        UserDAO.saveUser(pO);

        String sql = "INSERT INTO PropertyOwners (ownerID, email, balance, totalEarnings) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pO.getOwnerID());
            stmt.setString(2, pO.getEmail());
            stmt.setBigDecimal(3, pO.getBalance());
            stmt.setBigDecimal(4, pO.getTotalEarnings());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting user into database", e);
            return false;
        }
    }


    //READ USERS (ALL)
    public static List<PropertyOwner> getAllPropertyOwners() {
        List<PropertyOwner> pOList = new ArrayList<>();
        String sql = "SELECT * FROM PropertyOwners";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PropertyOwner pO = new PropertyOwner();
                pO.setOwnerID(rs.getString("ownerID"));
                pO.setEmail(rs.getString("email"));
                pO.setBalance(rs.getBigDecimal("balance"));
                pO.setTotalEarnings(rs.getBigDecimal("totalEarnings"));

                pOList.add(pO);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving users from database", e);
        }

        return pOList;
    }


    //READ USER (SINGLE) - Search By ID
    public static User getOwnerByID(String ownerID) {

        String sql = "SELECT * FROM Users WHERE ownerID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ownerID);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    var user = new PropertyOwner();
                    user.setOwnerID(rs.getString("ownerID"));
                    user.setEmail(rs.getString("email"));
                    user.setBalance(rs.getBigDecimal("balance"));
                    user.setTotalEarnings(rs.getBigDecimal("totalEarnings"));

                    return user;

                }else{
                    return null;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user from database", e);
            return null;
        }
    }


    //READ USER (SINGLE) - Search By Email
    public static PropertyOwner getOwnerByEmail(String email) {
        User u = UserDAO.getUserByEmail(email);
        String sql = "SELECT * FROM PropertyOwners WHERE email = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    PropertyOwner pO = new PropertyOwner();
                    pO.setOwnerID(rs.getString("ownerID"));
                    pO.setBalance(rs.getBigDecimal("balance"));
                    pO.setTotalEarnings(rs.getBigDecimal("totalEarnings"));
                    pO.setUserID(u.getUserID());
                    pO.setEmail(u.getEmail());
                    pO.setPasswordHash(u.getPasswordHash());
                    pO.setFirstName(u.getFirstName());
                    pO.setLastName(u.getLastName());
                    pO.setGender(u.getGender());
                    pO.setContactNum(u.getContactNum());
                    pO.setProfilePictureURL(u.getProfilePictureURL());
                    pO.setActivated(u.isActivated());
                    pO.setActivationCode(u.getActivationCode());
                    pO.setActivationCodeExpiry(u.getActivationCodeExpiry());

                    return pO;

                }else{
                    return null;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving user from database", e);
            return null;
        }
    }


    //UPDATE USER (SINGLE)
    public static boolean updateUser(User user){
        String sql = "UPDATE Users "
                + "SET email = ?, "
                + "passwordHash = ?, "
                + "firstName = ?, "
                + "lastName = ?, "
                + "gender = ?, "
                + "contactNum = ?, "
                + "profilePictureUrl = ?, "
                + "isActivated = ?, "
                + "activationCode = ?, "
                + "activationCodeExpiry = ? "
                + "WHERE userID = ? " ;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getUserID());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getGender().toString());
            stmt.setString(7, user.getContactNum());
            stmt.setString(8, user.getProfilePictureURL());
            stmt.setBoolean(9, user.isActivated());
            stmt.setString(10, user.getActivationCode());
            stmt.setTimestamp(11, Timestamp.valueOf(user.getActivationCodeExpiry()));

            stmt.setInt(12, user.getUserID());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user in database", e);
            return false;
        }

    }


    //DELETE USER
    public static boolean deleteUser(User user){

        String sql ="DELETE FROM Users WHERE userID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, user.getUserID());

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }

    }

}
