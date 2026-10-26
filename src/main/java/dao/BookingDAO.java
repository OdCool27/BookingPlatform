package dao;

import config.DBUtil;
import models.Booking;
import java.sql.*;
import java.util.*;

public class BookingDAO {
    private static final String INSERT = "INSERT INTO Bookings(bookingID,guestID,propertyID,arrivalDate,departureDate,baseCharge,gct,tef,gart,totalCharge,status,payStatus,createdAt,updatedAt) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE = "UPDATE Bookings SET bookingID=?,guestID=?,propertyID=?,arrivalDate=?,departureDate=?,baseCharge=?,gct=?,tef=?,gart=?,totalCharge=?,status=?,payStatus=?,createdAt=?,updatedAt=? WHERE bookingID=?";

    private static void bind(PreparedStatement s, Booking b) throws SQLException {
        s.setString(1,b.getBookingID()); s.setString(2,b.getGuestID()); s.setString(3,b.getPropertyID());
        s.setString(4,b.getArrivalDate().toString()); s.setString(5,b.getDepartureDate().toString());
        s.setBigDecimal(6,b.getBaseCharge()); s.setBigDecimal(7,b.getGct()); s.setBigDecimal(8,b.getTef());
        s.setBigDecimal(9,b.getGart()); s.setBigDecimal(10,b.getTotalCharge());
        s.setString(11,b.getStatus().name()); s.setString(12,b.getPayStatus().name());
        s.setString(13,b.getCreatedAt().toString()); s.setString(14,b.getUpdatedAt().toString());
    }
    private static Booking map(ResultSet r) throws SQLException {
        // Constructor preserves the stored price snapshot rather than recalculating historical bookings.
        return new Booking(r.getString("bookingID"),r.getString("guestID"),r.getString("propertyID"),
                SQLiteValues.date(r.getString("arrivalDate")),SQLiteValues.date(r.getString("departureDate")),
                r.getBigDecimal("baseCharge"),r.getBigDecimal("gct"),r.getBigDecimal("tef"),
                r.getBigDecimal("gart"),r.getBigDecimal("totalCharge"),Booking.Status.valueOf(r.getString("status")),
                Booking.PaymentStatus.valueOf(r.getString("payStatus")),SQLiteValues.timestamp(r.getString("createdAt")),
                SQLiteValues.timestamp(r.getString("updatedAt")));
    }

    /** IMMEDIATE transactions reserve SQLite's write access before checking availability.
     * This closes the check-then-insert race without adding a reservation/locking business feature.
     * The original inclusive overlap rule is retained, including shared arrival/departure dates.
     */
    public static boolean saveIfAvailable(Booking b, boolean update) {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement s = c.prepareStatement("SELECT * FROM Bookings WHERE propertyID=? AND status='CONFIRMED' AND bookingID<>?")) {
                    s.setString(1,b.getPropertyID()); s.setString(2,b.getBookingID());
                    try (ResultSet r = s.executeQuery()) {
                        while(r.next()) {
                            Booking existing = map(r);
                            if (!(existing.getArrivalDate().isAfter(b.getDepartureDate()) || existing.getDepartureDate().isBefore(b.getArrivalDate()))) {
                                c.rollback(); return false;
                            }
                        }
                    }
                }
                try (PreparedStatement s = c.prepareStatement(update ? UPDATE : INSERT)) {
                    bind(s,b); if(update) s.setString(15,b.getBookingID());
                    if(s.executeUpdate() != 1) { c.rollback(); return false; }
                }
                c.commit(); return true;
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch(SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean saveBooking(Booking b) { return saveIfAvailable(b,false); }
    public static boolean updateBooking(Booking b) { return saveIfAvailable(b,true); }
    public static Booking retrieveBookingByBookingID(String id) {
        List<Booking> list = find("bookingID",id);
        return list.isEmpty() ? null : list.getFirst();
    }
    public static List<Booking> retrieveBookingByGuestID(String id) { return find("guestID",id); }
    public static List<Booking> retrieveBookingByPropertyID(String id) { return find("propertyID",id); }
    public static List<Booking> retrieveAllBookings() { return find(null,null); }
    private static List<Booking> find(String column,String id) {
        try(Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM Bookings" + (column == null ? "" : " WHERE " + column + "=?"))) {
            if(column != null) s.setString(1,id);
            try(ResultSet r = s.executeQuery()) {
                List<Booking> result = new ArrayList<>();
                while(r.next()) result.add(map(r));
                return result;
            }
        } catch(SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean deleteBookingByBookingID(String id) { return delete("bookingID",id); }
    public static boolean deleteBookingByPropertyID(String id) { return delete("propertyID",id); }
    public static boolean deleteBookingByGuestID(String id) { return delete("guestID",id); }
    private static boolean delete(String column,String id) {
        try(Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM Bookings WHERE " + column + "=?")) {
            s.setString(1,id); return s.executeUpdate() > 0;
        } catch(SQLException e) { throw new DataAccessException(e); }
    }
}
