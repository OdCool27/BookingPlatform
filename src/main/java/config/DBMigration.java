package config;
import org.flywaydb.core.Flyway;
import java.util.logging.Logger;

/** Flyway records successful versioned scripts and validates their checksums on later starts.
 * Do not catch migration failures here: startup must stop rather than serve an incompatible schema.
 */
public final class DBMigration {
    private DBMigration() {}
    public static void migrate(String jdbcUrl) {
        // Main passes the same resolved URL used by DBUtil. SQLite files do not need JDBC credentials.
        Flyway.configure()
                .dataSource(jdbcUrl,null,null)
                // classpath finds scripts in resources during development and inside the packaged JAR.
                .locations("classpath:db/migration")
                // Fail on incorrectly named scripts instead of silently leaving a migration unapplied.
                .validateMigrationNaming(true)
                // Disable Flyway's schema-clean operation; startup must never reset application data.
                .cleanDisabled(true)
                // load builds Flyway; migrate validates migration history/checksums and applies only
                // pending versions. An exception escapes to Main, so the server does not start.
                .load().migrate();
        // Reaching this line means migration completed or the schema was already up to date.
        Logger.getLogger(DBMigration.class.getName()).info("Database migrations complete");
    }
}
