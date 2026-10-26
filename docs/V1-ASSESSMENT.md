# Initial assessment — before source modification

This records the assessment presented before implementation. The working tree already had extensive staged/unstaged SQLite and HTTP work; those changes were treated as the starting point. Existing database files and the V1 migration were preserved.

## Architecture found

Java 24 Maven project with mutable models, mostly static JDBC DAOs, thin property/booking services and a large CLI. Main had begun starting HttpServer. AppRouter registered prefix-only root/health handlers; business handlers were empty. No DTOs or authentication service existed.

The POM included Spring Boot parent/starters, PostgreSQL, duplicate SQLite versions, Flyway, Jakarta Mail and JUnit in main scope. Spring APIs were not used for application behavior. An unused Tomcat logging import was present. There was one real JUnit migration test plus a main-source ad hoc test program.

AppConfig used APP_ENV/DB_URL but parsed a missing PORT without a default. Main hardcoded 8080. DBUtil separately loaded db.properties, so Flyway and application writes could target different databases. Foreign keys and busy timeout were already enabled by DBUtil. The initial Flyway filename/schema were valid, and migration failures propagated.

## Functional inventory

“Fully implemented” describes source behavior, not production readiness. Operations without an application workflow were not automatically exposed just because a DAO method existed.

| Operation | Initial status | Evidence / V1 decision |
|---|---|---|
| Password hashing/verification | Fully implemented | Existing BCrypt and PasswordSecurity reused |
| Email/password validation | Fully implemented | Helpers reused; HTTP null/length validation added |
| Guest registration | Partially implemented | CLI collects profile, DOB/preferences; DAO uses unsupported SQL arrays and non-atomic user/subtype writes |
| Owner registration | Partially implemented | CLI collects profile; DAO does not atomically save user/owner |
| Activation code generation/expiry/check | Fully implemented in memory | Six-digit code, ten minutes, clears code on success |
| Activation persistence/HTTP continuation | Partially implemented | CLI activated before save; UserDAO update parameter indexes wrong |
| Guest login | Partially implemented | BCrypt check exists; guest reads fail on SQLite arrays/dates |
| Owner login | Partially implemented | BCrypt check exists; no activation enforcement at login |
| Sessions/tokens/logout state | Not implemented | Logged-in object and process exit only; Basic authentication selected as minimal credential transport |
| User lookup/list | Implemented with defects | ID lookup sets firstName twice; SQL exceptions look like absence |
| User/profile update/delete | Partially implemented | DAO-only, incorrect update bindings/delete SQL; no established self-service workflow, not exposed |
| Property create/get/list/update/delete | Fully implemented at SQL level | CLI/service paths exist; service discards write failures |
| Own-property listing | Fully implemented | ownerID query exists |
| Ownership authorization for mutation | Partially implemented | CLI displays own list but accepts arbitrary ID; no service enforcement |
| Availability search | Fully implemented algorithm | Checks CONFIRMED bookings with inclusive boundaries; fragile null handling |
| Booking confirmation | Partially implemented | Availability + save + email exist; unchecked save, repeated B-1 ID, check/write race |
| Booking charges | Fully implemented | Existing Booking model calculates base, GCT, TEF, GART |
| Own-booking history | Fully implemented | CLI and guest-ID DAO query |
| Booking by ID/property/all | Fully implemented DAO reads with SQLite date risk | Reuse read behavior; restrict HTTP visibility |
| Booking date modification | Partially implemented | CLI changes dates; malformed UPDATE comma, stale base charge, no conflict validation |
| Booking cancellation | Not implemented | Menu text and comments only |
| Booking deletion/status/payment transitions | DAO/model primitives only | No established business workflow; excluded |
| Employee CRUD | Partially implemented | DAO exists; null/transaction risks, no CLI/service workflow |
| Review CRUD | Partially implemented | DAO exists; malformed UPDATE and inconsistent initializer; no CLI/service workflow |
| Transactions, payouts, charge details, service invoices/providers | Not implemented as workflows | Empty DAOs/data models |
| Property photos, facilities, verification, seasonal pricing | Not implemented persistently | Model collections only; PropertyDAO persists core fields |
| Email transport/templates | Partially implemented operationally | Existing activation/booking messages; source-tree SMTP configuration, errors swallowed |
| HTTP routes/controllers | Partially implemented scaffolding | Health/root strings; empty booking/auth handlers |
| Tests | Partially implemented | Single isolated Flyway table test; no service/HTTP tests |

## Reuse and required modifications

Retain the core models, password rules and BCrypt, property SQL, existing pricing and inclusive availability behavior, existing email purpose, SQLite and Flyway. Do not alter the existing migration or reset data.

Move registration/login and CLI-only booking construction/date changes into services. Repair SQLite-compatible account persistence, transaction boundaries, DAO error reporting, malformed SQL and generated booking IDs. Add real HTTP method/path matching, DTOs, JSON utilities, safe errors, request limits, service authorization and server shutdown.

New packages/classes: dto, http, AuthService, AuthController, PropertyController, domain/data exceptions, mail-transport injection seam, SQLite date compatibility mapper and integration tests. Reuse/complete existing BookingController, AppRouter, BookingHttpServer, Main and config classes.

## Risks and scope decisions

No persistent authentication existed. Basic authentication reuses email/password checking, with activated accounts and the existing guest/owner types. It adds no token/session store. Bind to loopback and require TLS at the remote deployment boundary. Never expose sensitive writes anonymously.

DAO success/failure values masked database errors. Registration could orphan users; simultaneous bookings could overlap. JDBC date representation needed verification against TEXT schema columns. ID generation always used B-1 for CLI bookings. Booking read setters recalculated stored charges. SMTP failures could be mistaken for successful delivery.

No cancellation window, refund rule, payment process or new authorization role can be inferred safely. Those remain excluded. Employee/review SQL alone does not justify a new public workflow.

## Proposed contract and sequence

Health; auth register/activate/login; authenticated property list/get/available; owner mine/create/update/delete; guest booking create/history/get/date-update. All business routes use /api/v1. No cancellation endpoint.

Sequence: configuration/JDBC/Flyway startup; HTTP lifecycle and health; routing/DTO/JSON/errors; authentication; property ownership; booking writes/history/date changes; isolated database/service/HTTP tests; CLI/dependency cleanup; accurate README and executable-JAR verification.
