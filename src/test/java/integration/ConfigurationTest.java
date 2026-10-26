package integration;
import config.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.sql.*;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
    @Test void smtpConfigurationIsExplicit() {
        var empty = new services.EmailService(Map.of());
        assertFalse(empty.configured());
        assertThrows(services.ServiceException.class,() -> empty.sendEmail("test@example.com","test","test"));
        assertThrows(IllegalArgumentException.class,() -> new services.EmailService(Map.of("SMTP_HOST","localhost")).validateConfiguration());
        assertThrows(IllegalArgumentException.class,() -> new services.EmailService(Map.of("SMTP_HOST","localhost","SMTP_USERNAME","user","SMTP_PASSWORD","secret","SMTP_PORT","0")).validateConfiguration());
    }
    @Test void defaultsAndValidation() {
        assertEquals("dev",AppConfig.from(Map.of()).environment());
        assertEquals(8080,AppConfig.from(Map.of()).serverPort());
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("APP_ENV","staging")));
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("PORT","abc")));
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("PORT","65536")));
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("PORT","0")));
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("APP_ENV","prod")));
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("APP_ENV","prod","DB_URL","jdbc:sqlite:relative.db")));
        assertThrows(IllegalArgumentException.class,() -> AppConfig.from(Map.of("DB_URL","jdbc:postgresql:wrong")));
    }
    @Test void testIgnoresInheritedDatabase(@TempDir Path dir) throws Exception {
        Path protectedFile = dir.resolve("production.db"); Files.writeString(protectedFile,"do not change");
        var config = AppConfig.from(Map.of("APP_ENV","test","PORT","0","DB_URL","jdbc:sqlite:" + protectedFile));
        assertNotEquals("jdbc:sqlite:" + protectedFile,config.dbUrl());
        DBMigration.migrate(config.dbUrl());
        assertEquals("do not change",Files.readString(protectedFile));
        Path temporary = Path.of(config.dbUrl().substring("jdbc:sqlite:".length()));
        Files.delete(temporary); Files.delete(temporary.getParent());
    }
    @Test void migrationFailureIsNotSwallowed(@TempDir Path dir) throws Exception {
        String url = "jdbc:sqlite:" + dir.resolve("invalid.db");
        try(Connection c = DriverManager.getConnection(url); Statement s = c.createStatement()) { s.execute("CREATE TABLE Users(wrong TEXT)"); }
        assertThrows(org.flywaydb.core.api.FlywayException.class,() -> DBMigration.migrate(url));
    }
}
