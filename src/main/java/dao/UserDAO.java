package dao;

import config.DBUtil;
import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public static boolean saveUser(User user) {
        try (Connection c = DBUtil.getConnection()) { return saveUser(c, user); }
        catch (SQLException e) { throw new DataAccessException(e); }
    }

    // A caller-owned connection lets registration save the base user and subtype in one transaction.
    static boolean saveUser(Connection c, User u) throws SQLException {
        String sql = "INSERT INTO Users(email,passwordHash,firstName,lastName,gender,contactNum,profilePictureURL,isActivated,activationCode,activationCodeExpiry) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(s, u);
            boolean saved = s.executeUpdate() == 1;
            try (ResultSet keys = s.getGeneratedKeys()) { if (keys.next()) u.setUserID(keys.getInt(1)); }
            return saved;
        }
    }

    private static void bind(PreparedStatement s, User u) throws SQLException {
        s.setString(1, u.getEmail()); s.setString(2, u.getPasswordHash());
        s.setString(3, u.getFirstName()); s.setString(4, u.getLastName());
        s.setString(5, u.getGender().name()); s.setString(6, u.getContactNum());
        s.setString(7, u.getProfilePictureURL()); s.setBoolean(8, u.isActivated());
        s.setString(9, u.getActivationCode());
        s.setString(10, u.getActivationCodeExpiry() == null ? null : u.getActivationCodeExpiry().toString());
    }

    static User map(ResultSet r) throws SQLException {
        User u = new User();
        u.setUserID(r.getInt("userID")); u.setEmail(r.getString("email"));
        u.setPasswordHash(r.getString("passwordHash")); u.setFirstName(r.getString("firstName"));
        u.setLastName(r.getString("lastName"));
        String gender = r.getString("gender");
        u.setGender(gender == null ? User.Gender.NOT_SPECIFIED : User.Gender.valueOf(gender));
        u.setContactNum(r.getString("contactNum")); u.setProfilePictureURL(r.getString("profilePictureURL"));
        u.setActivated(r.getBoolean("isActivated")); u.setActivationCode(r.getString("activationCode"));
        u.setActivationCodeExpiry(SQLiteValues.timestamp(r.getString("activationCodeExpiry")));
        return u;
    }

    public static User getUserByEmail(String email) { return find("email", email); }
    public static User getUserByID(int id) { return find("userID", id); }
    private static User find(String column, Object value) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM Users WHERE " + column + " = ?")) {
            s.setObject(1, value);
            try (ResultSet r = s.executeQuery()) { return r.next() ? map(r) : null; }
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static List<User> getAllUsers() {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("SELECT * FROM Users"); ResultSet r = s.executeQuery()) {
            List<User> result = new ArrayList<>();
            while(r.next()) result.add(map(r));
            return result;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean updateUser(User u) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("UPDATE Users SET email=?,passwordHash=?,firstName=?,lastName=?,gender=?,contactNum=?,profilePictureURL=?,isActivated=?,activationCode=?,activationCodeExpiry=? WHERE userID=?")) {
            bind(s, u); s.setInt(11, u.getUserID()); return s.executeUpdate() == 1;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean deleteUserByID(int id) { return delete("userID", id); }
    public static boolean deleteUserByEmail(String email) { return delete("email", email); }
    private static boolean delete(String column, Object value) {
        try (Connection c = DBUtil.getConnection(); PreparedStatement s = c.prepareStatement("DELETE FROM Users WHERE " + column + "=?")) {
            s.setObject(1, value); return s.executeUpdate() > 0;
        } catch (SQLException e) { throw new DataAccessException(e); }
    }
    public static boolean deleteAllUser() {
        try (Connection c = DBUtil.getConnection(); Statement s = c.createStatement()) { return s.executeUpdate("DELETE FROM Users") > 0; }
        catch (SQLException e) { throw new DataAccessException(e); }
    }
}
