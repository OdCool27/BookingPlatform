package dao;

import config.DBUtil;
import helpers.Json;
import models.*;
import java.sql.*;
import java.util.*;

public class GuestDAO {
    public static boolean saveGuest(Guest g) {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                UserDAO.saveUser(c, g);
                try (PreparedStatement s = c.prepareStatement("INSERT INTO Guests(guestID,email,nationality,points,dob,paymentMethodID,paymentUserID,paymentType,isDefault,nickname,preferences) VALUES(?,?,?,?,?,?,?,?,?,?,?)")) {
                    bind(s, g); s.executeUpdate();
                }
                c.commit(); return true;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    private static void bind(PreparedStatement s, Guest g) throws SQLException {
        s.setString(1,g.getGuestID()); s.setString(2,g.getEmail()); s.setString(3,g.getNationality());
        s.setInt(4,g.getPoints()); s.setString(5,g.getDob() == null ? null : g.getDob().toString());
        PaymentDetails p = g.getPaymentDetails();
        s.setString(6,p.getPaymentMethodID()); s.setString(7,p.getPaymentUserID());
        s.setString(8,p.getPaymentType().name()); s.setBoolean(9,p.isDefault()); s.setString(10,p.getNickname());
        try { s.setString(11,Json.MAPPER.writeValueAsString(g.getPreferences())); }
        catch (java.io.IOException e) { throw new IllegalStateException("Cannot encode preferences", e); }
    }
    private static Guest map(ResultSet r) throws SQLException {
        Guest g = new Guest(UserDAO.map(r), r.getString("guestID"), r.getString("nationality"), r.getInt("points"),
                SQLiteValues.date(r.getString("dob")), new PaymentDetails(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        PaymentDetails p = g.getPaymentDetails();
        p.setPaymentMethodID(r.getString("paymentMethodID")); p.setPaymentUserID(r.getString("paymentUserID"));
        String type = r.getString("paymentType");
        p.setPaymentType(type == null ? PaymentDetails.PaymentType.NONE : PaymentDetails.PaymentType.valueOf(type));
        p.setDefault(r.getBoolean("isDefault")); p.setNickname(r.getString("nickname"));
        String preferences = r.getString("preferences");
        if (preferences != null && !preferences.isBlank()) {
            try { g.setPreferences(new ArrayList<>(Arrays.asList(Json.MAPPER.readValue(preferences, String[].class)))); }
            catch (java.io.IOException e) { throw new IllegalStateException("Invalid stored guest preferences", e); }
        }
        return g;
    }
    public static Guest getGuestByEmail(String email) { return find("g.email",email); }
    public static Guest getGuestByID(String id) { return find("g.guestID",id); }
    public static User getGuestByID(int id) { return getGuestByID(Integer.toString(id)); }
    private static Guest find(String column, String value) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT u.*,g.* FROM Guests g JOIN Users u ON u.email=g.email WHERE " + column + "=?")) {
            s.setString(1,value);
            try (ResultSet r = s.executeQuery()) { return r.next() ? map(r) : null; }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static List<Guest> getAllGuests() {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT u.*,g.* FROM Guests g JOIN Users u ON u.email=g.email"); ResultSet r = s.executeQuery()) {
            List<Guest> guests = new ArrayList<>();
            while(r.next()) guests.add(map(r));
            return guests;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean updateUser(Guest g) {
        UserDAO.updateUser(g);
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Guests SET guestID=?,email=?,nationality=?,points=?,dob=?,paymentMethodID=?,paymentUserID=?,paymentType=?,isDefault=?,nickname=?,preferences=? WHERE guestID=?")) {
            bind(s,g); s.setString(12,g.getGuestID()); return s.executeUpdate() == 1;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean deleteGuest(Guest g) {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement s = c.prepareStatement("DELETE FROM Guests WHERE guestID=?");
                 PreparedStatement u = c.prepareStatement("DELETE FROM Users WHERE userID=?")) {
                s.setString(1,g.getGuestID()); boolean deleted = s.executeUpdate() == 1;
                u.setInt(1,g.getUserID()); if(deleted) u.executeUpdate();
                c.commit(); return deleted;
            } catch (SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
}
