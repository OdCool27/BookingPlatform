package config;

import org.sqlite.SQLiteConfig;
import java.sql.*;

/** Configured once before services are constructed. No hidden development database fallback. */
public final class DBUtil {
    // volatile makes the configured URL visible to the HTTP worker threads that open connections.
    private static volatile String databaseUrl;
    private DBUtil() {}
    public static synchronized void configure(AppConfig config) {
        // synchronized keeps checking and assigning the URL together if initialization is repeated.
        // Reusing the same URL is safe; switching databases could send later requests to other data.
        if (databaseUrl != null && !databaseUrl.equals(config.dbUrl()))
            throw new IllegalStateException("A process may use only one application database");
        databaseUrl = config.dbUrl();
    }
    public static Connection getConnection() throws SQLException {
        // Main must configure storage first. Never fall back to a development file from a DAO call.
        if (databaseUrl == null) throw new IllegalStateException("Database has not been configured");
        SQLiteConfig options = new SQLiteConfig();
        // SQLite foreign-key enforcement is connection-specific, so enable it for every connection.
        options.enforceForeignKeys(true);
        // Wait up to five seconds for another writer before reporting database contention.
        options.setBusyTimeout(5000);
        // When a DAO begins a transaction, IMMEDIATE reserves write access before its reads.
        // This lets booking conflict checks and the subsequent write run without a competing writer.
        // It does not begin a transaction here; the DAO does that with setAutoCommit(false).
        options.setTransactionMode(SQLiteConfig.TransactionMode.IMMEDIATE);
        // Each caller receives its own connection and must close it with try-with-resources.
        return DriverManager.getConnection(databaseUrl, options.toProperties());
    }
}
