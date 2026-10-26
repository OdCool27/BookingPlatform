package dao;

import config.DBUtil;
import models.*;
import java.sql.*;
import java.util.*;

public class PropertyOwnerDAO {
    public static boolean savePropertyOwner(PropertyOwner owner) {
        try (Connection c = DBUtil.getConnection()) {
            c.setAutoCommit(false);
            try {
                UserDAO.saveUser(c, owner);
                try (PreparedStatement s = c.prepareStatement("INSERT INTO PropertyOwners(ownerID,email,balance,totalEarnings) VALUES(?,?,?,?)")) {
                    s.setString(1,owner.getOwnerID()); s.setString(2,owner.getEmail());
                    s.setBigDecimal(3,owner.getBalance()); s.setBigDecimal(4,owner.getTotalEarnings());
                    s.executeUpdate();
                }
                c.commit(); return true;
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    private static PropertyOwner map(ResultSet r) throws SQLException {
        return new PropertyOwner(UserDAO.map(r),r.getString("ownerID"),r.getBigDecimal("balance"),
                r.getBigDecimal("totalEarnings"),new PaymentDetails(),new PayoutDetails(),
                new ArrayList<>(),new ArrayList<>(),new ArrayList<>());
    }
    public static PropertyOwner getOwnerByEmail(String email) { return find("o.email",email); }
    public static User getOwnerByID(String id) { return find("o.ownerID",id); }
    private static PropertyOwner find(String column, String value) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT u.*,o.* FROM PropertyOwners o JOIN Users u ON u.email=o.email WHERE " + column + "=?")) {
            s.setString(1,value);
            try (ResultSet r = s.executeQuery()) { return r.next() ? map(r) : null; }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static List<PropertyOwner> getAllPropertyOwners() {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT u.*,o.* FROM PropertyOwners o JOIN Users u ON u.email=o.email"); ResultSet r = s.executeQuery()) {
            List<PropertyOwner> owners = new ArrayList<>();
            while(r.next()) owners.add(map(r));
            return owners;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean updateUser(User u) { return UserDAO.updateUser(u); }
    public static boolean deleteUser(User u) { return UserDAO.deleteUserByID(u.getUserID()); }
}
