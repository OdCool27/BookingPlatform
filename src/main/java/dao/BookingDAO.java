package dao;

import helpers.DBUtil;
import models.Booking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static app.Main.LOGGER;

public class BookingDAO {

    //CREATES/INITIALIZES BOOKING TABLE
    public static boolean initializeBookingTable(){

        String tableStmt = "CREATE TABLE IF NOT EXISTS Bookings(" +
                "bookingID VARCHAR(10) PRIMARY KEY," +
                "guestID VARCHAR(12) NOT NULL REFERENCES Guests(guestID)," +
                "propertyID VARCHAR(12) NOT NULL REFERENCES Properties(propertyID)," +
                "arrivalDate DATE NOT NULL, " +
                "departureDate DATE NOT NULL, " +
                "baseCharge DECIMAL(10,2)," +
                "gct DECIMAL(10,2)," +
                "tef DECIMAL(10,2)," +
                "gart DECIMAL(10,2)," +
                "totalCharge DECIMAL(10,2)," +
                "status VARCHAR(12)," +
                "payStatus VARCHAR(12)," +
                "createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try(
                Connection dbConn = DBUtil.getConnection();//CREATE CONNECTION OBJECT AND CONNECTS TO DATABASE
                Statement dbStmt = dbConn.createStatement() //CREATE STATEMENT OBJECT
                ){

            dbStmt.executeUpdate(tableStmt);//EXECUTES STATEMENT
            LOGGER.info("Table 'Bookings' created or already exists.");
            return true;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize table", e);
            return false;
        }

    }


    //<---------------------------------   SAVE METHOD -------------------------------------------------->

    //SAVES BOOKING OBJECT TO DB
    public static boolean saveBooking(Booking b) {

        if (initializeBookingTable()) {

            String sql = "INSERT INTO Bookings(bookingID, guestID, propertyID, arrivalDate, departureDate," +
                    " baseCharge, gct, tef, gart, totalCharge, status, payStatus, createdAt, updatedAt) " +
                    "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (
                    Connection conn = DBUtil.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql);
            ){
                stmt.setString(1,b.getBookingID());
                stmt.setString(2, b.getGuestID());
                stmt.setString(3, b.getPropertyID());
                stmt.setDate(4, Date.valueOf(b.getArrivalDate()));
                stmt.setDate(5, Date.valueOf(b.getDepartureDate()));
                stmt.setBigDecimal(6,b.getBaseCharge());
                stmt.setBigDecimal(7,b.getGct());
                stmt.setBigDecimal(8,b.getTef());
                stmt.setBigDecimal(9,b.getGart());
                stmt.setBigDecimal(10, b.getTotalCharge());
                stmt.setString(11, b.getStatus().toString());
                stmt.setString(12, b.getPayStatus().toString());
                stmt.setTimestamp(13, Timestamp.valueOf(b.getCreatedAt()));
                stmt.setTimestamp(14, Timestamp.valueOf(b.getUpdatedAt()));

                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;

            }catch(SQLException e){
                LOGGER.log(Level.SEVERE, "Error inserting user into database", e);
                return false;
            }

        }else{
            LOGGER.log(Level.SEVERE, "Database Initialization Failed");
            return false;
        }
    }

    //<---------------------------------   READ METHODS -------------------------------------------------->

    //RETRIEVES A BOOKING OBJECT FROM DB USING BOOKING ID
    public static Booking retrieveBookingByBookingID(String bookingID){

        String sql = "SELECT * FROM Bookings WHERE bookingID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)
        ){
            stmt.setString(1, bookingID);

            try(ResultSet rs = stmt.executeQuery()){
                if(rs.next()){
                    Booking b = new Booking();
                    b.setBookingID(rs.getString("bookingID"));
                    b.setGuestID(rs.getString("guestID"));
                    b.setPropertyID(rs.getString("propertyID"));
                    b.setArrivalDate(rs.getDate("arrivalDate").toLocalDate());
                    b.setDepartureDate(rs.getDate("departureDate").toLocalDate());
                    b.setBaseCharge(rs.getBigDecimal("baseCharge"));
                    b.setGct(rs.getBigDecimal("gct"));
                    b.setTef(rs.getBigDecimal("tef"));
                    b.setGart(rs.getBigDecimal("gart"));
                    b.setTotalCharge(rs.getBigDecimal("totalCharge"));
                    b.setStatus(Booking.Status.valueOf(rs.getString("status")));
                    b.setPayStatus(Booking.PaymentStatus.valueOf(rs.getString("payStatus")));
                    b.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                    b.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

                    return b;
                }else{
                    return null;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database Connection Failed" + e);
            return null;
        }
    }


    //RETRIEVES BOOKING OBJECTS FROM DB USING GUEST ID
    public static List<Booking> retrieveBookingByGuestID(String guestID){
        String sql = "SELECT * FROM Bookings WHERE guestID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)
        ){
            stmt.setString(1, guestID);

            try(ResultSet rs = stmt.executeQuery()){

                List<Booking> bookingList = new ArrayList<>();

                while(rs.next()){
                    Booking b = new Booking();
                    b.setBookingID(rs.getString("bookingID"));
                    b.setGuestID(rs.getString("guestID"));
                    b.setPropertyID(rs.getString("propertyID"));
                    b.setArrivalDate(rs.getDate("arrivalDate").toLocalDate());
                    b.setDepartureDate(rs.getDate("departureDate").toLocalDate());
                    b.setBaseCharge(rs.getBigDecimal("baseCharge"));
                    b.setGct(rs.getBigDecimal("gct"));
                    b.setTef(rs.getBigDecimal("tef"));
                    b.setGart(rs.getBigDecimal("gart"));
                    b.setTotalCharge(rs.getBigDecimal("totalCharge"));
                    b.setStatus(Booking.Status.valueOf(rs.getString("status")));
                    b.setPayStatus(Booking.PaymentStatus.valueOf(rs.getString("payStatus")));
                    b.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                    b.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

                    bookingList.add(b);

                }
                return bookingList;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database Connection Failed" + e);
            return null;
        }
    }


    //RETRIEVES BOOKING OBJECTS FROM DB USING PROPERTY ID
    public static List<Booking> retrieveBookingByPropertyID(String propertyID){
        String sql = "SELECT * FROM Bookings WHERE propertyID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)
        ){
            stmt.setString(1, propertyID);

            try(ResultSet rs = stmt.executeQuery()){

                List<Booking> bookingList = new ArrayList<>();

                while(rs.next()){
                    Booking b = new Booking();
                    b.setBookingID(rs.getString("bookingID"));
                    b.setGuestID(rs.getString("guestID"));
                    b.setPropertyID(rs.getString("propertyID"));
                    b.setArrivalDate(rs.getDate("arrivalDate").toLocalDate());
                    b.setDepartureDate(rs.getDate("departureDate").toLocalDate());
                    b.setBaseCharge(rs.getBigDecimal("baseCharge"));
                    b.setGct(rs.getBigDecimal("gct"));
                    b.setTef(rs.getBigDecimal("tef"));
                    b.setGart(rs.getBigDecimal("gart"));
                    b.setTotalCharge(rs.getBigDecimal("totalCharge"));
                    b.setStatus(Booking.Status.valueOf(rs.getString("status")));
                    b.setPayStatus(Booking.PaymentStatus.valueOf(rs.getString("payStatus")));
                    b.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                    b.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

                    bookingList.add(b);
                }
                return bookingList;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database Connection Failed" + e);
            return null;
        }
    }


    //RETRIEVES ALL BOOKING OBJECTS FROM DB
    public static List<Booking> retrieveAllBookings(){

        String sql = "SELECT * FROM Bookings;";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement dbStmt = dbConn.prepareStatement(sql)){

            ResultSet rs = dbStmt.executeQuery();

            List<Booking> bookingList = new ArrayList<>();

            while (rs.next()){
                Booking b = new Booking();
                b.setBookingID(rs.getString("bookingID"));
                b.setGuestID(rs.getString("guestID"));
                b.setPropertyID(rs.getString("propertyID"));
                b.setArrivalDate(rs.getDate("arrivalDate").toLocalDate());
                b.setDepartureDate(rs.getDate("departureDate").toLocalDate());
                b.setBaseCharge(rs.getBigDecimal("baseCharge"));
                b.setGct(rs.getBigDecimal("gct"));
                b.setTef(rs.getBigDecimal("tef"));
                b.setGart(rs.getBigDecimal("gart"));
                b.setTotalCharge(rs.getBigDecimal("totalCharge"));
                b.setStatus(Booking.Status.valueOf(rs.getString("status")));
                b.setPayStatus(Booking.PaymentStatus.valueOf(rs.getString("payStatus")));
                b.setCreatedAt(rs.getTimestamp("createdAt").toLocalDateTime());
                b.setUpdatedAt(rs.getTimestamp("updatedAt").toLocalDateTime());

                bookingList.add(b);
            }

            return bookingList;


        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database Connection Failed" + e);
            return null;
        }
    }

    //<---------------------------------   UPDATE METHOD -------------------------------------------------->

    //UPDATES BOOKING OBJECT IN DB
    public static boolean updateBooking(Booking b){
        String sql = "UPDATE Bookings "+
                "SET bookingID = ?, " +
                "guestID = ?, " +
                "propertyID = ?, " +
                "arrivalDate = ?, " +
                "departureDate = ?, " +
                "baseCharge = ?, " +
                "gct = ?, " +
                "tef = ?, " +
                "gart = ?, " +
                "totalCharge = ?, " +
                "status = ?, " +
                "payStatus = ?, " +
                "createdAt = ?, " +
                "updatedAt = ?, " +
                "WHERE bookingID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1,b.getBookingID());
            stmt.setString(2, b.getGuestID());
            stmt.setString(3, b.getPropertyID());
            stmt.setDate(4, Date.valueOf(b.getArrivalDate()));
            stmt.setDate(5, Date.valueOf(b.getDepartureDate()));
            stmt.setBigDecimal(6,b.getBaseCharge());
            stmt.setBigDecimal(7,b.getGct());
            stmt.setBigDecimal(8,b.getTef());
            stmt.setBigDecimal(9,b.getGart());
            stmt.setBigDecimal(10, b.getTotalCharge());
            stmt.setString(11, b.getStatus().toString());
            stmt.setString(12, b.getPayStatus().toString());
            stmt.setTimestamp(13, Timestamp.valueOf(b.getCreatedAt()));
            stmt.setTimestamp(14, Timestamp.valueOf(b.getUpdatedAt()));

            stmt.setString(15, b.getBookingID());

            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;

        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Database Connection Failed " + e);
            return false;
        }

    }


    //<---------------------------------   DELETE METHODS -------------------------------------------------->

    //DELETES BOOKING OBJECT FROM DB USING BOOKING ID
    public static boolean deleteBookingByBookingID(String bookingID){

        String sql = "DELETE FROM Bookings WHERE bookingID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bookingID);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }
    }


    //DELETES BOOKING OBJECT FROM DB USING PROPERTY ID
    public static boolean deleteBookingByPropertyID(String propertyID){

        String sql = "DELETE FROM Bookings WHERE propertyID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, propertyID);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }
    }


    //DELETES BOOKING OBJECT FROM DB USING PROPERTY ID
    public static boolean deleteBookingByGuestID(String guestID){

        String sql = "DELETE FROM Bookings WHERE guestID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, guestID);

            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user from database", e);
            return false;
        }
    }

}
