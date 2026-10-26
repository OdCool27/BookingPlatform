package config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/** One configuration source for JDBC and Flyway. Relative paths use the process working directory. */
public record AppConfig(String environment, String dbUrl, int serverPort) {
    public static AppConfig load() {
        // Read the process environment here; from(Map) also lets tests supply controlled values.
        return from(System.getenv());
    }

    public static AppConfig from(Map<String, String> values) {
        // Reject misspelled environments rather than silently selecting the development database.
        String env = values.getOrDefault("APP_ENV", "dev");
        if (!Set.of("dev", "test", "prod").contains(env))
            throw new IllegalArgumentException("APP_ENV must be dev, test or prod");
        // Environment values are strings. Convert PORT and report a configuration-specific error.
        // Port 0 asks the operating system for a free port, which is useful for isolated tests.
        int port;
        try { port = Integer.parseInt(values.getOrDefault("PORT", "8080")); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("PORT must be an integer"); }
        if (port < 0 || port > 65535 || (port == 0 && !env.equals("test")))
            throw new IllegalArgumentException("PORT must be 1..65535 (0 allowed in test)");
        String url = values.get("DB_URL");
        try {
            if (env.equals("test")) {
                // Never honor inherited DB_URL in tests: it might point to a production database.
                Path dir = Files.createTempDirectory("osyntra-test-");
                url = "jdbc:sqlite:" + dir.resolve("booking.db");
            } else if (url == null || url.isBlank()) {
                // Development has a convenient default. Production must explicitly name its database.
                if (env.equals("prod")) throw new IllegalArgumentException("DB_URL is required in prod");
                url = "jdbc:sqlite:data/booking_dev.db";
            }
            // Accept ordinary SQLite file paths only. In-memory databases and URI options need
            // different connection/lifetime handling and are not part of this configuration contract.
            if (!url.startsWith("jdbc:sqlite:") || url.contains("?") || url.contains(":memory:") || url.contains("file:"))
                throw new IllegalArgumentException("DB_URL must be a file-based jdbc:sqlite: path");
            // Strip the JDBC prefix before using filesystem APIs; it is not part of the filename.
            String filename = url.substring("jdbc:sqlite:".length());
            if (filename.isBlank()) throw new IllegalArgumentException("SQLite file path is required");
            Path path = Path.of(filename);
            if (env.equals("prod") && !path.isAbsolute())
                throw new IllegalArgumentException("Production DB_URL must contain an absolute path");
            // Resolve relative development paths against the working directory and remove dot
            // segments. JDBC and Flyway both receive this same resolved URL from Main.
            path = path.toAbsolutePath().normalize();
            // SQLite can create the database file, but its parent directories must already exist.
            Files.createDirectories(path.getParent());
            return new AppConfig(env, "jdbc:sqlite:" + path, port);
        } catch (IOException e) {
            // Propagate startup failure instead of attempting to serve requests without storage.
            throw new IllegalStateException("Cannot prepare database directory", e);
        }
    }

    public String getEnvironment() { return environment; }
    public String getDbUrl() { return dbUrl; }
    public int getServerPort() { return serverPort; }
}
