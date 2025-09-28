package dao;

import models.User;
import helpers.DBUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static app.Main.LOGGER;


public class UserDAO {

	//INITIALIZE DATABASE
    public static boolean initializeUser() {
        String tableStmt = "CREATE TABLE IF NOT EXISTS Users (" +
                "userID SERIAL PRIMARY KEY, " +
                "email VARCHAR(80) NOT NULL UNIQUE, " +
                "passwordHash VARCHAR(255) NOT NULL, " +
                "firstName VARCHAR(30), " +
                "lastName VARCHAR(30), " +
                "gender VARCHAR(15), " +
                "contactNum VARCHAR(15), " +
                "profilePictureURL VARCHAR(100), " +
                "isActivated BOOLEAN DEFAULT FALSE, " +
                "activationCode VARCHAR(100), " +
                "activationCodeExpiry TIMESTAMP" +
                ");";

        try (
                Connection dbConn = DBUtil.getConnection(); // Connects directly to vapfumiconnect DB
                Statement dbStmt = dbConn.createStatement()
        ) {
            dbStmt.executeUpdate(tableStmt);
            LOGGER.info("Table 'Users' created or already exists.");
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize table", e);
            return false;
        }
    }


    //<---------------------------------   SAVE METHOD -------------------------------------------------->

	//SAVES USER TO DATABASE
    public static boolean saveUser(User user) {

        initializeUser();

        String sql = "INSERT INTO Users (email, passwordHash, firstName, lastName, gender, contactNum, profilePictureURL, isActivated, activationCode, activationCodeExpiry) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFirstName());
            stmt.setString(4, user.getLastName());
            stmt.setString(5, user.getGender().toString());
            stmt.setString(6, user.getContactNum());
            stmt.setString(7, user.getProfilePictureURL());
            stmt.setBoolean(8, user.isActivated());
            stmt.setString(9, user.getActivationCode());

            LocalDateTime expiry = user.getActivationCodeExpiry() != null
                    ? user.getActivationCodeExpiry()
                    : LocalDateTime.now().plusMinutes(10);

            stmt.setTimestamp(10, Timestamp.valueOf(expiry));

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting user into database", e);
            return false;
        }
    }

    //<---------------------------------   READ METHODS -------------------------------------------------->

    //READ USERS (ALL)
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM Users";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserID(rs.getInt("userID"));
                user.setEmail(rs.getString("email"));
                user.setPasswordHash(rs.getString("passwordHash"));
                user.setFirstName(rs.getString("firstName"));
                user.setLastName(rs.getString("lastName"));
                user.setGender(User.Gender.valueOf(rs.getString("gender")));
                user.setContactNum(rs.getString("contactNum"));
                user.setProfilePictureURL(rs.getString("profilePictureURL"));
                user.setActivated(rs.getBoolean("isActivated"));
                user.setActivationCode(rs.getString("activationCode"));
                
                Timestamp ts = rs.getTimestamp("activationCodeExpiry");
                if (ts != null) {
                    user.setActivationCodeExpiry(ts.toLocalDateTime());
                }

                users.add(user);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving users from database", e);
        }

        return users;
    }
    
    
    //READ USER (SINGLE) - Search By ID
    public static User getUserByID(int userID) {

        String sql = "SELECT * FROM Users WHERE userID = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    User user = new User();
                    user.setUserID(rs.getInt("userID"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("passwordHash"));
                    user.setFirstName(rs.getString("firstName"));
                    user.setFirstName(rs.getString("lastName"));
                    user.setGender(User.Gender.valueOf(rs.getString("gender")));
                    user.setContactNum(rs.getString("contactNum"));
                    user.setProfilePictureURL(rs.getString("profilePictureURL"));
                    user.setActivated(rs.getBoolean("isActivated"));
                    user.setActivationCode(rs.getString("activationCode"));

                    Timestamp activationCodeExpiry = rs.getTimestamp("activationCodeExpiry");
                    if(activationCodeExpiry != null){
                        user.setActivationCodeExpiry(activationCodeExpiry.toLocalDateTime());
                    }

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
    public static User getUserByEmail(String email) {

        String sql = "SELECT * FROM Users WHERE email = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    User user = new User();
                    user.setUserID(rs.getInt("userID"));
                    user.setEmail(rs.getString("email"));
                    user.setPasswordHash(rs.getString("passwordHash"));
                    user.setFirstName(rs.getString("firstName"));
                    user.setLastName(rs.getString("lastName"));
                    user.setGender(User.Gender.valueOf(rs.getString("gender")));
                    user.setContactNum(rs.getString("contactNum"));
                    user.setProfilePictureURL(rs.getString("profilePictureURL"));
                    user.setActivated(rs.getBoolean("isActivated"));
                    user.setActivationCode(rs.getString("activationCode"));

                    Timestamp activationCodeExpiry = rs.getTimestamp("activationCodeExpiry");
                    if(activationCodeExpiry != null){
                        user.setActivationCodeExpiry(activationCodeExpiry.toLocalDateTime());
                    }

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

    //<---------------------------------   UPDATE METHOD -------------------------------------------------->

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

            if (user.getActivationCodeExpiry() != null){
                stmt.setTimestamp(11, Timestamp.valueOf(user.getActivationCodeExpiry()));
            }else{
                stmt.setNull(11, Types.TIMESTAMP);
            }




            stmt.setInt(12, user.getUserID());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user in database", e);
            return false;
        }

    }

    //<---------------------------------   DELETE METHODS -------------------------------------------------->

    //DELETE USER USING USER ID
    public static boolean deleteUserByID(int userID){

        String sql ="DELETE FROM Users WHERE userID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }

    }



    //DELETE USER USING EMAIL
    public static boolean deleteUserByEmail(String email){

        String sql ="DELETE FROM Users WHERE email = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }

    }


    //DELETE USER
    public static boolean deleteAllUser(){

        String sql ="DELETE * FROM Users";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }

    }

}
