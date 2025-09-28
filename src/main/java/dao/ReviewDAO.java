package dao;

import helpers.DBUtil;
import models.Review;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static app.Main.LOGGER;

public class ReviewDAO {

    public static boolean initializeReviewsTable(){

        String tableStmt = "CREATE TABLE IF NOT EXISTS Reviews ( " +
                "reviewID INTEGER SERIAL PRIMARY KEY, " +
                "guestID VARCHAR(12) NOT NULL REFERENCES Guests(guestID), " +
                "propertyID VARCHAR(12) NOT NULL REFERENCES Properties(propertyID), " +
                "bookingID VARCHAR(12) NOT NULL UNIQUE REFERENCES Bookings(bookingID), " +
                "rating INTEGER CHECK (rating BETWEEN 1 AND 5), " +
                "comment VARCHAR(200), " +
                "timeStamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try(Connection dbConn = DBUtil.getConnection();
        Statement dbStmt = dbConn.createStatement()){

            dbStmt.executeUpdate(tableStmt);
            LOGGER.info("Table 'Reviews' created or already exists.");
            return true;

        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Create/Initialize 'Reviews' Table.");
            return false;
        }

    }

    //<---------------------------------- SAVE METHOD ------------------------------------------------->

    public static boolean saveReview(Review r){

        String sql = "INSERT INTO Reviews (reviewID, guestID, propertyID, bookingID, rating, comment, timestamp) " +
                "VALUES(?,?,?,?,?,?,?);";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement prepStmt = dbConn.prepareStatement(sql)){
            prepStmt.setInt(1, r.getReviewID());
            prepStmt.setString(2, r.getGuestID());
            prepStmt.setString(3, r.getPropertyID());
            prepStmt.setString(4, r.getBookingID());
            prepStmt.setInt(5, r.getRating());
            prepStmt.setString(6, r.getComment());
            prepStmt.setTimestamp(7, Timestamp.valueOf(r.getTimeStamp()));

            int rowsInserted = prepStmt.executeUpdate();
            return rowsInserted > 0;

        }catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Save Review.");
            return false;

        }

    }

    //<---------------------------------- READ METHODS ------------------------------------------------->

    public static Review retrieveReviewByReviewID(int reviewID){

        String sql =  "SELECT * FROM Reviews WHERE reviewID = ?";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setInt(1, reviewID);

            try(ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    Review r = new Review();
                    r.setReviewID(rs.getInt("reviewID"));
                    r.setBookingID(rs.getString("bookingID"));
                    r.setPropertyID(rs.getString("propertyID"));
                    r.setGuestID(rs.getString("guestID"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setTimeStamp(rs.getTimestamp("timestamp").toLocalDateTime());

                    return r;

                }
            } catch(SQLException e){
                LOGGER.log(Level.SEVERE, "Fail to Retrieve Review", e);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Fail to Connect to Review Table", e);
        }

        return null;
    }


    public static List<Review> retrieveReviewByGuestID(String guestID){

        String sql =  "SELECT * FROM Reviews WHERE guestID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, guestID);

            try(ResultSet rs = stmt.executeQuery()) {

                List<Review> reviewList = new ArrayList<>();

                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewID(rs.getInt("reviewID"));
                    r.setBookingID(rs.getString("bookingID"));
                    r.setPropertyID(rs.getString("propertyID"));
                    r.setGuestID(rs.getString("guestID"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setTimeStamp(rs.getTimestamp("timestamp").toLocalDateTime());

                    reviewList.add(r);
                }

                return reviewList;

            } catch(SQLException e){
                LOGGER.log(Level.SEVERE, "Fail to Retrieve Review", e);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Fail to Connect to Review Table", e);
        }

        return null;

    }


    public static List<Review> retrieveReviewByPropertyID(String propertyID){
        String sql =  "SELECT * FROM Reviews WHERE propertyID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, propertyID);

            try(ResultSet rs = stmt.executeQuery()) {

                List<Review> reviewList = new ArrayList<>();

                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewID(rs.getInt("reviewID"));
                    r.setBookingID(rs.getString("bookingID"));
                    r.setPropertyID(rs.getString("propertyID"));
                    r.setGuestID(rs.getString("guestID"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setTimeStamp(rs.getTimestamp("timestamp").toLocalDateTime());

                    reviewList.add(r);
                }

                return reviewList;

            } catch(SQLException e){
                LOGGER.log(Level.SEVERE, "Fail to Retrieve Review", e);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Fail to Connect to Review Table", e);
        }

        return null;

    }


    public static List<Review> retrieveAllReviews(){

        String sql =  "SELECT * FROM Reviews";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            try(ResultSet rs = stmt.executeQuery()) {

                List<Review> reviewList = new ArrayList<>();

                while (rs.next()) {
                    Review r = new Review();
                    r.setReviewID(rs.getInt("reviewID"));
                    r.setBookingID(rs.getString("bookingID"));
                    r.setPropertyID(rs.getString("propertyID"));
                    r.setGuestID(rs.getString("guestID"));
                    r.setRating(rs.getInt("rating"));
                    r.setComment(rs.getString("comment"));
                    r.setTimeStamp(rs.getTimestamp("timestamp").toLocalDateTime());

                    reviewList.add(r);
                }

                return reviewList;

            } catch(SQLException e){
                LOGGER.log(Level.SEVERE, "Fail to Retrieve Review", e);
            }

        } catch (SQLException e){
            LOGGER.log(Level.SEVERE, "Fail to Connect to Review Table", e);
        }

        return null;

    }

    //<---------------------------------- UPDATE METHOD ------------------------------------------>

    public static boolean updateReview(Review r){

        String sql = "UPDATE Reviews " +
                "SET reviewID = ?, " +
                "bookingID = ?, " +
                "propertyID = ?, " +
                "guestID = ?, " +
                "rating = ?, " +
                "comment = ?, " +
                "timestamp = ?, " +
                "WHERE reviewID = ?;";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setInt(1, r.getReviewID());
            stmt.setString(2,r.getBookingID());
            stmt.setString(3, r.getPropertyID());
            stmt.setString(4, r.getGuestID());
            stmt.setInt(5, r.getRating());
            stmt.setString(6, r.getComment());
            stmt.setTimestamp(7, Timestamp.valueOf(r.getTimeStamp()));

            stmt.setInt(8, r.getReviewID());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Connect to Review Table", e);
            return false;
        }

    }

    //<--------------------------------- DELETE METHODS ------------------------------------------>

    public static boolean deleteReviewByReviewID(int reviewID){
        String sql = "DELETE FROM Reviews WHERE reviewID = ?";

        try(Connection dbConn = DBUtil.getConnection();
        PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setInt(1, reviewID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;


        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Connect to Review Table", e);
            return false;
        }
    }


    public static boolean deleteReviewByPropertyID(String propertyID){
        String sql = "DELETE FROM Reviews WHERE propertyID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, propertyID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;


        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Connect to Review Table", e);
            return false;
        }

    }


    public static boolean deleteReviewByGuestID(String guestID){
        String sql = "DELETE FROM Reviews WHERE guestID = ?";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            stmt.setString(1, guestID);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;


        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Connect to Review Table", e);
            return false;
        }
    }


    public static boolean deleteAllReviews(){
        String sql = "DELETE FROM Reviews;";

        try(Connection dbConn = DBUtil.getConnection();
            PreparedStatement stmt = dbConn.prepareStatement(sql)){

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;


        }catch(SQLException e){
            LOGGER.log(Level.SEVERE, "Failed to Connect to Review Table", e);
            return false;
        }
    }

}
