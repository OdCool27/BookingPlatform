package integration;

import config.DBMigration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

public class DBMigrationTest {

    @TempDir
    public Path tempDir;

    @Test
    public void migrate(@TempDir Path tempDir) throws Exception {
        Path dbPath = tempDir.resolve("test.db");

        String url = "jdbc:sqlite:" + dbPath;

        DBMigration.migrate(url);

        try (Connection conn = DriverManager.getConnection(url)) {

            var statement = conn.prepareStatement(
                    """
                    SELECT name
                    FROM sqlite_master
                    WHERE type = 'table'
                    AND name = 'Bookings'
                    """
            );

            try (statement;
                 ResultSet rs = statement.executeQuery()) {

                assertTrue(rs.next());
                assertEquals("Bookings", rs.getString("name"));
            }
        }
    }
}
