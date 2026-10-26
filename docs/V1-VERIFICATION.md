# V1 implementation and verification

## Delivered changes

- Completed Main startup, one environment-derived database configuration, migration-before-listen behavior and server/executor shutdown.
- Reused AppRouter as an exact method/path dispatcher with 404/405/Allow behavior.
- Added authentication/property controllers, completed BookingController, and introduced explicit boundary records and shared JSON/error handling.
- Extracted existing CLI authentication and booking construction/date-change logic into services.
- Repaired user/guest/owner persistence and account transactions. Guest preferences are JSON TEXT. ISO dates plus legacy millisecond readers match the existing SQLite schema.
- Added service ownership checks and minimal HTTP Basic credential transport, using existing BCrypt and existing account subtypes.
- Made booking availability-check/write atomic using SQLite IMMEDIATE transactions. Kept the original inclusive overlap rule and price formulas.
- Preserved stored charge snapshots during DAO reads, corrected date-change repricing and generated non-repeating booking/account IDs.
- Externalized SMTP credentials and injected email transport for isolated tests.
- Removed CLI, ad hoc main-source UnitTest, SQL Server probe, obsolete db.properties/SMTP config and Spring/PostgreSQL dependencies.
- Preserved the original V1 migration and existing database files.

## Significant files

| Files | Purpose |
|---|---|
| pom.xml | Core Java dependencies, JUnit and executable shaded JAR |
| app/Main.java | Manual dependency construction and startup |
| config/AppConfig.java, DBUtil.java, DBMigration.java | Validated environments, JDBC safety and migrations |
| server/BookingHttpServer.java, router/AppRouter.java | Listener/workers, routes and error mapping |
| controller/AuthController.java, PropertyController.java, BookingController.java | Functional HTTP handlers |
| dto/ApiDtos.java, http/*, helpers/Json.java | Explicit data contracts, body limits, JSON, authentication |
| services/AuthService.java, PropertyManagementService.java, BookingService.java | Extracted business rules and authorization |
| dao/UserDAO.java, GuestDAO.java, PropertyOwnerDAO.java, BookingDAO.java, PropertyDAO.java | Correct SQLite persistence, failures and transaction behavior |
| dao/SQLiteValues.java, DataAccessException.java | Legacy date compatibility and controlled persistence errors |
| services/EmailService.java, MailSender.java, helpers/EmailSender.java | Environment-based SMTP and test transport |
| helpers/AccountActivation.java | Existing activation logic without console interaction, SecureRandom codes |
| models/Booking.java, User.java | Stored charge preservation; remove database lookup/security fields from display formatting |
| src/test/java/integration/* | Real database/service/HTTP verification |
| README.md, docs/V1-ASSESSMENT.md | Startup, contract, examples, limitations and original assessment |

Removed: app/CLI.java, app/UnitTest.java, helpers/MSSQLConnect.java, src/smtp.properties, src/main/resources/db.properties and empty src/test/java/example.env.

## Automated coverage

JUnit exercises:
- Exact health/resource routes, unknown paths, method rejection and Allow headers.
- Guest/owner registration, persisted activation, expiry, resend-by-retry, login and safe account serialization.
- Property CRUD, owner-only writes and cross-owner rejection.
- Booking creation, own-history/get, role/ownership rejection, date changes, repricing, missing resources and conflict rollback.
- Simultaneous bookings: exactly one 201 and one 409.
- Malformed/null/trailing/unknown-field JSON, missing media type, oversized bodies and bad query parameters.
- Foreign keys, date/activation constraints and registration rollback.
- Email outage behavior for registration and committed bookings.
- Safe 500 JSON and recovery after a temporary database failure.
- Legacy numeric dates and preservation of stored historical charges.
- Migration re-execution without reapplying V1; expected tables; failed migration propagation.
- Test database isolation, configuration rejection and unchanged existing database bytes.

Tests use temporary SQLite databases, an ephemeral HTTP port, real HttpClient requests and captured email. No live SMTP provider is contacted. Non-V1 employee/review persistence is not newly certified.

## Final verification results (2026-10-25)

- `mvn -q clean verify`: passed, exit code 0. Maven downloaded missing build plugins after network permission was granted.
- 18 JUnit tests: 13 API/database/service integration tests, 4 configuration tests and the preserved migration test. Zero failures, errors or skipped tests.
- Executable `target/OsyntraBookingPlatform-1.0-SNAPSHOT.jar` started through `java -jar` with `APP_ENV=test`, `PORT=0` and an inherited development DB_URL that test configuration ignored.
- Packaged Flyway migration completed; live `GET /health` returned 200, `application/json; charset=utf-8` and `{"message":"Server is healthy"}`.
- JAR inspection found Main and the V1 migration, with no CLI, ad hoc UnitTest, SQL Server probe or Spring framework classes.
- CRLF-aware `git diff --check` passed. Existing database bytes were unchanged by automated tests.
- Expected server logs during tests include a deliberately induced database failure and email-delivery failure; these tests passed by checking controlled behavior and recovery.
- Toolchain warnings concern Maven's Guice use of deprecated Unsafe and SQLite native access on JDK 24. The documented launch command supplies the native-access flag.

No known blocking V1 implementation/test failures remain. Real SMTP delivery and external TLS/proxy deployment were not exercised. The README documents those operational requirements and the intentionally excluded functionality.
