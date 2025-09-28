package dao;

import helpers.DBUtil;
import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static app.Main.LOGGER;

public class GuestDAO {

    //INITIALIZE DATABASE
    public static boolean initializeGuest() {
        String tableStmt = "CREATE TABLE IF NOT EXISTS Guests (" +
                "guestID VARCHAR(12) PRIMARY KEY, " +
                "email VARCHAR(80) NOT NULL REFERENCES Users(email), " +
                "nationality VARCHAR(24), " +
                "points INTEGER, " +
                "dob DATE, " +
                "paymentMethodID VARCHAR(50), " +
                "paymentUserID VARCHAR(50), " +
                "paymentType VARCHAR(20), " +
                "isDefault BOOLEAN, " +
                "nickname VARCHAR(30), " +
                "preferences TEXT[] " +
                ");";

        try (
                Connection dbConn = DBUtil.getConnection(); // Connects directly to vapfumiconnect DB
                Statement dbStmt = dbConn.createStatement()
        ) {
            dbStmt.executeUpdate(tableStmt);
            LOGGER.info("Table 'Guests' created or already exists.");
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize table", e);
            return false;
        }
    }


    //-----------------------------------CRUD OPERATIONS---------------------------------------------------

    //SAVES USER TO DATABASE
    public static boolean saveGuest(Guest guest) {

        UserDAO.saveUser(guest);//Saves user related information

        initializeGuest();

        String sql = "INSERT INTO Guests (guestID, email, nationality, points, dob, paymentMethodID, paymentUserID, paymentType, " +
                "isDefault, nickname, preferences) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getGuestID());
            stmt.setString(2, guest.getEmail());
            stmt.setString(3, guest.getNationality());
            stmt.setInt(4, guest.getPoints());
            stmt.setDate(5, java.sql.Date.valueOf(guest.getDob()));
            stmt.setString(6, guest.getPaymentDetails().getPaymentMethodID());
            stmt.setString(7, guest.getPaymentDetails().getPaymentUserID());
            stmt.setString(8, String.valueOf(guest.getPaymentDetails().getPaymentType()));
            stmt.setBoolean(9, guest.getPaymentDetails().isDefault());
            stmt.setString(10, guest.getPaymentDetails().getNickname());

            // Converts Array List to an Array
            String[] prefsArray = guest.getPreferences().toArray(new String[0]);
            java.sql.Array sqlArray = conn.createArrayOf("text", prefsArray);
            stmt.setArray(11, sqlArray);


            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting guest into database", e);
            return false;
        }
    }


    //READ USERS (ALL)
    public static List<Guest> getAllGuests() {
        List<Guest> guestList = new ArrayList<>();
        String sql = "SELECT * FROM Guests";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Guest user = new Guest();
                user.setGuestID(rs.getString("guestID"));
                user.setEmail(rs.getString("email"));
                user.setNationality(rs.getString("nationality"));
                user.setPoints(rs.getInt("points"));
                user.setDob(rs.getDate("dob").toLocalDate());
                user.getPaymentDetails().setPaymentMethodID(rs.getString("paymentMethodID"));
                user.getPaymentDetails().setPaymentUserID(rs.getString("paymentUserID"));
                user.getPaymentDetails().setPaymentType(PaymentDetails.PaymentType.valueOf(rs.getString("paymentType")));
                user.getPaymentDetails().setDefault(rs.getBoolean("isDefault"));
                user.getPaymentDetails().setNickname(rs.getString("nickname"));
                String[] prefArray = (String[]) (rs.getArray("preferences")).getArray();
                user.setPreferences(new ArrayList<>(Arrays.asList(prefArray)));

                guestList.add(user);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving users from database", e);
        }

        return guestList;
    }


    //READ USER (SINGLE) - Search By ID
    public static User getGuestByID(int guestID) {

        String sql = "SELECT *\n" +
                "FROM Guests\n" +
                "INNER JOIN Users\n" +
                "  ON Guests.email = Users.email\n" +
                "  WHERE Guests.guestID = '?';";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, guestID);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    Guest guest = new Guest();
                    guest.setUserID(rs.getInt("userID"));
                    guest.setEmail(rs.getString("email"));
                    guest.setPasswordHash(rs.getString("passwordHash"));
                    guest.setFirstName(rs.getString("firstName"));
                    guest.setLastName(rs.getString("lastName"));
                    guest.setGender(User.Gender.valueOf(rs.getString("gender")));
                    guest.setContactNum(rs.getString("contactNum"));
                    guest.setProfilePictureURL(rs.getString("profilePictureURL"));
                    guest.setActivated(rs.getBoolean("isActivated"));
                    guest.setActivationCode(rs.getString("activationCode"));

                    Timestamp activationCodeExpiry = rs.getTimestamp("activationCodeExpiry");
                    if(activationCodeExpiry != null){
                        guest.setActivationCodeExpiry(activationCodeExpiry.toLocalDateTime());
                    }

                    guest.setGuestID(rs.getString("guestID"));
                    guest.setNationality(rs.getString("nationality"));
                    guest.setPoints(rs.getInt("points"));
                    guest.setDob(rs.getDate("dob").toLocalDate());
                    guest.setBookingHistory(BookingDAO.retrieveBookingByGuestID(guest.getGuestID()));
                    guest.getPaymentDetails().setPaymentMethodID((rs.getString("paymentMethodID")));
                    guest.getPaymentDetails().setPaymentUserID((rs.getString("paymentUserID")));
                    guest.getPaymentDetails().setPaymentType(PaymentDetails.PaymentType.valueOf(rs.getString("paymentUserID")));
                    guest.getPaymentDetails().setDefault((rs.getBoolean("isDefault")));
                    guest.getPaymentDetails().setNickname((rs.getString("nickname")));
                    String[] prefArray = (String[]) (rs.getArray("preferences")).getArray();
                    List<Booking> bookingHistory = BookingDAO.retrieveBookingByGuestID(guest.getGuestID());
                    //List<Transaction> transactionList = TransactionDAO.retrieveTransactionsByGuestID(guest.getGuestID());
                    guest.setPreferences(new ArrayList<>(Arrays.asList(prefArray)));
                    guest.setBookingHistory(bookingHistory);
                    //guest.setTransactionList(transactionList);

                    return guest;

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
    public static Guest getGuestByEmail(String email) {

        User user = UserDAO.getUserByEmail(email);

        String sql = "SELECT *\n" +
                "FROM Guests\n" +
                "INNER JOIN Users\n" +
                "  ON Guests.email = Users.email\n" +
                "  WHERE Users.email = ?;";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    Guest guest = new Guest();

                    //User-related Data
                    guest.setUserID(rs.getInt("userID"));
                    guest.setEmail(rs.getString("email"));
                    guest.setPasswordHash(rs.getString("passwordHash"));
                    guest.setFirstName(rs.getString("firstName"));
                    guest.setLastName(rs.getString("lastName"));
                    guest.setGender(User.Gender.valueOf(rs.getString("gender")));
                    guest.setContactNum(rs.getString("contactNum"));
                    guest.setProfilePictureURL(rs.getString("profilePictureURL"));
                    guest.setActivated(rs.getBoolean("isActivated"));
                    guest.setActivationCode(rs.getString("activationCode"));

                    Timestamp activationCodeExpiry = rs.getTimestamp("activationCodeExpiry");
                    if(activationCodeExpiry != null){//Checks if activationCodeExpiry is null
                        guest.setActivationCodeExpiry(activationCodeExpiry.toLocalDateTime());
                    }

                    //Guest-related Data
                    guest.setGuestID(rs.getString("guestID"));
                    guest.setNationality(rs.getString("nationality"));
                    guest.setPoints(rs.getInt("points"));
                    guest.setDob(rs.getDate("dob").toLocalDate());
                    guest.setBookingHistory(BookingDAO.retrieveBookingByGuestID(guest.getGuestID()));
                    guest.getPaymentDetails().setPaymentMethodID((rs.getString("paymentMethodID")));
                    guest.getPaymentDetails().setPaymentUserID((rs.getString("paymentUserID")));
                    String paymentTypeStr = rs.getString("paymentType"); // make sure this column exists in DB

                    PaymentDetails.PaymentType paymentType = PaymentDetails.PaymentType.NONE;
                    if (paymentTypeStr != null) {
                        try {
                            paymentType = PaymentDetails.PaymentType.valueOf(paymentTypeStr.toUpperCase()); // optional toUpperCase()
                        } catch (IllegalArgumentException e) {
                            LOGGER.warning("Invalid PaymentType in DB: " + paymentTypeStr);
                            paymentType = PaymentDetails.PaymentType.NONE; // or handle as needed
                        }
                    }
                    guest.getPaymentDetails().setPaymentType(paymentType);


                    guest.getPaymentDetails().setDefault((rs.getBoolean("isDefault")));
                    guest.getPaymentDetails().setNickname((rs.getString("nickname")));
                    String[] prefArray = (String[]) (rs.getArray("preferences")).getArray();
                    List<Booking> bookingHistory = BookingDAO.retrieveBookingByGuestID(guest.getGuestID());
                    //List<Transaction> transactionList = TransactionDAO.retrieveTransactionsByGuestID(guest.getGuestID());
                    guest.setPreferences(new ArrayList<>(Arrays.asList(prefArray)));
                    guest.setBookingHistory(bookingHistory);
                    //guest.setTransactionList(transactionList);
                    return guest;

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
    public static boolean updateUser(Guest user){

        UserDAO.updateUser(user);

        String sql = "UPDATE Guests "
                + "SET guestID = ?, "
                + "email = ?, "
                + "nationality = ?, "
                + "points = ?, "
                + "dob = ?, "
                + "paymentMethodID = ?, "
                + "paymentUserID = ?, "
                + "paymentType = ?, "
                + "isDefault = ?, "
                + "nickname = ?, "
                + "preferences = ? "
                + "WHERE guestID = ? " ;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getGuestID());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getNationality());
            stmt.setInt(4, user.getPoints());
            stmt.setDate(5, Date.valueOf(user.getDob()));
            stmt.setString(6, user.getPaymentDetails().getPaymentMethodID());
            stmt.setString(7, user.getPaymentDetails().getPaymentUserID());
            stmt.setString(8, user.getPaymentDetails().getPaymentType().toString());
            stmt.setBoolean(9, user.getPaymentDetails().isDefault());
            stmt.setString(10, user.getPaymentDetails().getNickname());

            // Converts Array List to an Array
            String[] prefsArray = user.getPreferences().toArray(new String[0]);
            java.sql.Array sqlArray = conn.createArrayOf("text", prefsArray);
            stmt.setArray(11, sqlArray);

            stmt.setString(12, user.getGuestID());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user in database", e);
            return false;
        }

    }


    //DELETE USER
    public static boolean deleteGuest(Guest guest){

        String sql ="DELETE FROM Guests WHERE guestID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guest.getGuestID());

            int rowsDeleted = stmt.executeUpdate();
            UserDAO.deleteUserByID(guest.getUserID());
            return rowsDeleted > 0;

        } catch (SQLException e) {

            LOGGER.log(Level.SEVERE, "Error deleting guest from database", e);
            return false;
        }

    }

}
