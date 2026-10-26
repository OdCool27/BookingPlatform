package integration;

import config.DBMigration;
import helpers.PasswordSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class SeedAccountsMigrationTest {
    @TempDir Path directory;

    @Test void seedsActivatedAccountsWithValidHashesAndDoesNotRepeat() throws Exception {
        String url = "jdbc:sqlite:" + directory.resolve("seed.db");
        DBMigration.migrate(url);
        DBMigration.migrate(url);
        try (Connection c = DriverManager.getConnection(url)) {
            String[][] accounts = {
                {"odanecollins@live.com", "Collins@2026", "Odane", "Collins"},
                {"john.smith@example.com", "Smith@2026", "John", "Smith"},
                {"phil.collins@example.com", "Phil@2026", "Phil", "Collins"}
            };
            for (String[] account : accounts) {
                try (PreparedStatement s = c.prepareStatement("SELECT * FROM Users WHERE email=?")) {
                    s.setString(1, account[0]);
                    try (ResultSet r = s.executeQuery()) {
                        assertTrue(r.next());
                        assertTrue(PasswordSecurity.verifyPassword(account[1], r.getString("passwordHash")));
                        assertEquals(account[2], r.getString("firstName"));
                        assertEquals(account[3], r.getString("lastName"));
                        assertTrue(r.getBoolean("isActivated"));
                        assertNull(r.getString("activationCode"));
                        assertNull(r.getString("activationCodeExpiry"));
                        assertFalse(r.next());
                    }
                }
            }
            try (Statement s = c.createStatement()) {
                try (ResultSet r = s.executeQuery("SELECT * FROM Employees WHERE email='odanecollins@live.com'")) {
                    assertTrue(r.next()); assertEquals("ADMIN", r.getString("jobRole"));
                    assertEquals("SUPERADMIN", r.getString("accessLevel"));
                }
                try (ResultSet r = s.executeQuery("SELECT * FROM Guests WHERE email='john.smith@example.com'")) {
                    assertTrue(r.next()); assertEquals("NONE", r.getString("paymentType"));
                    assertEquals("1990-05-15", r.getString("dob"));
                }
                try (ResultSet r = s.executeQuery("SELECT * FROM PropertyOwners WHERE email='phil.collins@example.com'")) {
                    assertTrue(r.next()); assertEquals(0, r.getInt("balance"));
                }
                try (ResultSet r = s.executeQuery("SELECT COUNT(*) FROM Users")) {
                    assertTrue(r.next()); assertEquals(3, r.getInt(1));
                }
                try (ResultSet r = s.executeQuery("PRAGMA foreign_key_check")) { assertFalse(r.next()); }
            }
        }
    }
}
