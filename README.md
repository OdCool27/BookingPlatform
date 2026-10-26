# Osyntra BookingPlatform V1

A Core Java HTTP REST backend for the existing guest, property-owner and booking workflows. The application uses Java's built-in `HttpServer`; Spring Boot and the interactive CLI are no longer required.

## V1 functionality

- Register guest or owner accounts, send a ten-minute email activation code, activate accounts and validate login credentials.
- Browse properties and date availability as an activated user.
- Owners can create, list their own, replace and delete their properties.
- Guests can create confirmed bookings, view their own history and change booking dates.
- Preserve the existing GCT, TEF and GART calculations and inclusive date-overlap rule.
- Send existing booking-confirmation emails.
- Return JSON with consistent errors and provide a public health check.

Cancellation was only a menu item/comment and is **not implemented**. No payment, refund, reporting, review, employee-administration or property-verification endpoints were added.

See [the initial assessment](docs/V1-ASSESSMENT.md) for the source-based inventory and [verification notes](docs/V1-VERIFICATION.md) for the delivered changes and test coverage.

## Technology stack

JDK 24, Maven, `com.sun.net.httpserver.HttpServer`, JDBC, SQLite JDBC 3.51.1.0, Flyway 13.11.1, Jackson 2.19.1, the existing BCrypt implementation, Jakarta Mail 2.0.1 and JUnit Jupiter 5.12.2.

## Architecture and project structure

```text
HTTP client
  -> BookingHttpServer -> AppRouter -> controller
  -> service -> DAO -> SQLite
                |
                -> existing email templates -> SMTP

src/main/java/
  app/Main.java           Startup and manual dependency injection
  config/                 AppConfig, JDBC connections, Flyway
  server/                 Listening socket and worker lifecycle
  router/                 Exact method/path dispatch and error mapping
  controller/             JSON requests, service calls, response DTOs
  dto/                    Explicit request/response records
  http/                   Basic authentication and HTTP/JSON utilities
  services/               Authentication, ownership, booking rules, email
  dao/                    Prepared SQL, mappings and transactions
  models/                 Reusable application data and price calculations
  helpers/                BCrypt, validation, activation, IDs and email templates
src/main/resources/db/migration/V1__initial_schema.sql
src/test/java/integration/  JUnit configuration, database, service and HTTP tests
```

`Main.start` is the **composition root**: it creates each dependency and passes it to the class that needs it. For example, the booking service receives an email sender, and the booking controller receives that service plus an authentication adapter. Tests substitute an email transport that captures messages, without replacing business logic or bypassing activation.

`AppConfig` reads environment variables, validates their values and resolves one absolute SQLite path. `DBUtil` must be configured explicitly; it has no hidden development database fallback. Flyway receives exactly the same URL. A process uses one application database; attempting to reconfigure it to another file fails.

`HttpServer.create` binds a socket. `createContext` only matches a URL prefix; it does not implement REST routing. Therefore the server registers one root context and `AppRouter` matches the complete path and HTTP method itself. Literal paths such as `/properties/mine` take precedence over `{id}`. Known paths with unsupported methods return 405 and an `Allow` header; unknown paths return 404. Trailing slashes are not aliases.

`setExecutor` supplies the bounded worker pool. `start` begins accepting requests; `close` stops the server and shuts down its workers. A JVM shutdown hook calls `close` when the process exits normally.

Controllers never issue SQL. Request DTOs allow only supported fields; unknown fields fail validation. Response DTOs deliberately omit password hashes, activation codes, payment details and internal owner/guest identifiers. Service errors carry business reasons; the router maps these to HTTP statuses.

## Configuration

Set variables in the process environment or your IDE run configuration. No dotenv loader is included; an `.env` file alone has no effect.

| Variable | dev | test | prod |
|---|---|---|---|
| `APP_ENV` | Default `dev` | Explicit `test` | Explicit `prod` |
| `DB_URL` | Default `jdbc:sqlite:data/booking_dev.db`; optional override | Ignored; a new OS temporary directory/database is always allocated | Required, file-based SQLite URL with an absolute path |
| `PORT` | Default 8080 | Default 8080; 0 chooses an available port | Default 8080 |
| `SMTP_HOST` | Required to deliver email | Tests inject a capture transport | Required |
| `SMTP_PORT` | Default 587 when SMTP is configured | Same | Default 587 |
| `SMTP_USERNAME` | SMTP account/from address | Tests do not contact SMTP | Required |
| `SMTP_PASSWORD` | SMTP password/app password | Tests do not contact SMTP | Required |

Unknown environments, invalid ports, non-SQLite URLs, in-memory/URI-style SQLite URLs and relative production database paths fail validation. Parent directories are created automatically. Partial SMTP configuration fails startup. With no SMTP variables in dev, the server can start, but registration returns 503 until mail is configured. Production requires complete SMTP settings before startup.

SMTP uses authenticated STARTTLS with certificate hostname verification and five-second connection/read/write timeouts. Secrets belong in environment variables. The old source-tree SMTP file was removed; Git history was not rewritten.

Test mode intentionally ignores even a valid inherited `DB_URL`, preventing automated tests from selecting a development or production file. Test-mode manual runs create a new temporary database on every start; they are not persistent testing environments. Automated tests remove their temporary databases. A manual test-mode run may leave its temporary directory for inspection.

## SQLite and Flyway

No separate database server or manual table creation is required.

Startup performs:

1. Load/validate environment configuration.
2. Configure JDBC.
3. Apply and validate Flyway migrations.
4. Construct services, controllers and routing.
5. Bind and start the HTTP server.
6. Log the listening address.

An initialization or migration error prevents startup. Flyway records successful versions/checksums in `flyway_schema_history`; restarting does not rerun V1. The original V1 script has not been changed by this conversion. Future schema changes belong in new files, such as `V2__description.sql`. Automatic clean/reset and automatic baselining are not enabled.

Every application connection enables foreign keys and a five-second busy timeout. Account registration saves the base user and subtype in one transaction. Booking writes acquire SQLite's IMMEDIATE transaction before checking overlap and writing, preventing two concurrent requests from confirming the same dates. Foreign keys prevent deleting a property referenced by bookings or reviews; the API returns 409.

Dates are stored as ISO text. Readers also understand numeric millisecond dates produced by the previous JDBC implementation. Guest preferences use JSON text because SQLite does not support JDBC SQL arrays. Historical booking charges are read as stored rather than recalculated during retrieval.

If an existing database has tables but no Flyway history, or a migration checksum mismatch, startup fails for review. Back up and reconcile that database separately; do not delete data or run Flyway repair blindly.

## Build, start and test

Install JDK 24 and Maven. From the project root:

```powershell
mvn clean verify
java --enable-native-access=ALL-UNNAMED -jar target/OsyntraBookingPlatform-1.0-SNAPSHOT.jar
```

The shaded executable JAR includes runtime dependencies and Flyway service registrations. The native-access flag allows SQLite's native library on JDK 24.

Development configuration in PowerShell:

```powershell
$env:APP_ENV = "dev"
$env:PORT = "8080"
$env:SMTP_HOST = "your-smtp-host"
$env:SMTP_USERNAME = "your-mailbox@example.com"
$env:SMTP_PASSWORD = "your-app-password"
java --enable-native-access=ALL-UNNAMED -jar target/OsyntraBookingPlatform-1.0-SNAPSHOT.jar
```

For production, set `APP_ENV=prod` and an explicit URL such as `jdbc:sqlite:C:/Osyntra/data/booking.db`, alongside SMTP settings. These are examples, not committed credentials.

The server binds only to `127.0.0.1`. Place a TLS-terminating reverse proxy in front of it for remote access; never send Basic credentials over public plaintext HTTP. No cloud or proxy deployment configuration is included.

Run tests without packaging:

```powershell
mvn test
```

JUnit starts the HTTP server on port 0 and uses Java `HttpClient`. Tests use isolated temporary databases, captured email messages and real DAOs. They verify that the existing `data/booking_dev.db` and `data/booking_test.db` bytes remain unchanged. SMTP connectivity to a real provider is not tested.

## Authentication and security

V1 has no sessions, JWTs, refresh tokens or new roles. The CLI already checked an email/password with BCrypt and selected guest or owner behavior. HTTP Basic transports those same credentials on each protected request, and the service checks account activation and the persisted subtype. Login returns safe account information; it does not return a token. To stop authenticating, the client stops sending credentials.

All property and booking routes require an activated account. Owners can mutate only their own properties; guests can view/change only their own bookings. Server-side code assigns IDs, ownership, prices, booking status and payment status. Clients cannot submit these fields to override them.

Activation requires the email, registration password and emailed code. It expires after ten minutes. If registration email delivery fails, the inactive account remains persisted. Retry the same registration with the original password to issue a fresh code; retrying does not overwrite profile information. Activated accounts cannot be registered again.

Request bodies are limited to 65,536 bytes. JSON and date errors are controlled responses. Password input is limited to BCrypt's 72-byte maximum. SQL uses prepared parameters. Browser-originated writes with an `Origin` header are rejected to prevent Basic-auth CSRF; no browser/CORS workflow is offered in V1. A production proxy should provide HTTPS, request timeouts and rate limits. The built-in server is not a complete production security gateway.

## Implemented endpoints

All resource routes use `/api/v1`. Only health and the three authentication operations do not require an Authorization header.

| Method | Endpoint | Controller | Service | Description |
|---|---|---|---|---|
| GET | `/health` | AppRouter | — | Process health |
| GET | `/api/v1/health` | AppRouter | — | Compatible health alias |
| POST | `/api/v1/auth/register` | AuthController | AuthService | Register guest/owner; send activation code |
| POST | `/api/v1/auth/activate` | AuthController | AuthService | Activate with email, password and code |
| POST | `/api/v1/auth/login` | AuthController | AuthService | Validate credentials; return safe account |
| GET | `/api/v1/properties` | PropertyController | PropertyManagementService | List properties |
| GET | `/api/v1/properties/mine` | PropertyController | PropertyManagementService | Owner's properties |
| GET | `/api/v1/properties/available?arrivalDate=YYYY-MM-DD&departureDate=YYYY-MM-DD` | PropertyController | BookingService | Existing availability search |
| GET | `/api/v1/properties/{id}` | PropertyController | PropertyManagementService | Get one property |
| POST | `/api/v1/properties` | PropertyController | PropertyManagementService | Create owned property |
| PUT | `/api/v1/properties/{id}` | PropertyController | PropertyManagementService | Replace supported property details |
| DELETE | `/api/v1/properties/{id}` | PropertyController | PropertyManagementService | Delete unreferenced owned property |
| GET | `/api/v1/bookings` | BookingController | BookingService | Guest's history |
| GET | `/api/v1/bookings/{id}` | BookingController | BookingService | Guest's booking |
| POST | `/api/v1/bookings` | BookingController | BookingService | Confirm a booking |
| PUT | `/api/v1/bookings/{id}` | BookingController | BookingService | Change arrival/departure dates |

Successful creates return 201. Property and booking creates include `Location`. Reads/updates return 200; deletion returns 204 with no body.

Errors use `{"code":"INVALID","message":"departureDate must be after arrivalDate"}`. Statuses include 400 (invalid input), 401 (credentials missing/incorrect), 403 (activation/role/ownership), 404, 405, 409 (conflict or referential constraint), 413 (body limit), 414 (path limit), 415 (JSON media type required), 500 (internal error) and 503 (email unavailable). Internal exception details are logged on the server, not returned.

## Example requests and responses

PowerShell examples below run against local development. IDs and activation codes shown in responses are illustrative.

```powershell
$base = "http://127.0.0.1:8080"
Invoke-RestMethod "$base/health"
# {"message":"Server is healthy"}

$registration = @{
  accountType = "OWNER"
  email = "owner@example.com"
  password = "ExamplePass1!"
  firstName = "Jane"
  lastName = "Owner"
  gender = "NOT_SPECIFIED"
  contactNum = "8765550100"
} | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/auth/register" -Method Post -ContentType "application/json" -Body $registration
# 201: {"userId":1,"email":"owner@example.com","firstName":"Jane","lastName":"Owner","activated":false}

$activation = @{ email="owner@example.com"; password="ExamplePass1!"; code="CODE_FROM_EMAIL" } | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/auth/activate" -Method Post -ContentType "application/json" -Body $activation
# 200: {"message":"Account activated"}

$login = @{ email="owner@example.com"; password="ExamplePass1!" } | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/auth/login" -Method Post -ContentType "application/json" -Body $login
# 200: account fields as above, with activated=true

$encoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("owner@example.com:ExamplePass1!"))
$ownerHeaders = @{ Authorization="Basic $encoded" }
$propertyBody = @{
  propertyName="Kingston Stay"; description="Two-bedroom home"
  address=@{ street="Main Street"; suite=""; city="Kingston"; state="Kingston"; postalCode=""; country="Jamaica" }
  latitude=18.0; longitude=-76.8; houseSize=1000; landSize=2000; baseRate=100.00
} | ConvertTo-Json
$property = Invoke-RestMethod "$base/api/v1/properties" -Method Post -Headers $ownerHeaders -ContentType "application/json" -Body $propertyBody
Invoke-RestMethod "$base/api/v1/properties/mine" -Headers $ownerHeaders
```

A guest registration uses `accountType="GUEST"` and additionally requires `nationality` and a past `dob`, for example `"1990-01-01"`. `preferences` is an optional array of strings. Supported gender values are `MALE`, `FEMALE`, `NOT_SPECIFIED`; omitted gender defaults to `NOT_SPECIFIED`.

Register and activate the guest in the same way, then:

```powershell
$guestEncoded = [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes("guest@example.com:ExamplePass1!"))
$guestHeaders = @{ Authorization="Basic $guestEncoded" }
Invoke-RestMethod "$base/api/v1/properties/available?arrivalDate=2030-01-01&departureDate=2030-01-03" -Headers $guestHeaders
$bookingBody = @{ propertyId=$property.propertyId; arrivalDate="2030-01-01"; departureDate="2030-01-03" } | ConvertTo-Json
$booking = Invoke-RestMethod "$base/api/v1/bookings" -Method Post -Headers $guestHeaders -ContentType "application/json" -Body $bookingBody
Invoke-RestMethod "$base/api/v1/bookings" -Headers $guestHeaders
$dates = @{ arrivalDate="2030-02-01"; departureDate="2030-02-04" } | ConvertTo-Json
Invoke-RestMethod "$base/api/v1/bookings/$($booking.bookingId)" -Method Put -Headers $guestHeaders -ContentType "application/json" -Body $dates
```

For two nights at a base rate of 100, the existing calculation produces these booking fields:

```json
{
  "bookingId": "B-generated-uuid",
  "propertyId": "JA-KS-12345",
  "arrivalDate": "2030-01-01",
  "departureDate": "2030-01-03",
  "baseCharge": 200.00,
  "gct": 30.0000,
  "tef": 2,
  "gart": 6,
  "totalCharge": 238.0000,
  "status": "CONFIRMED",
  "paymentStatus": "PENDING",
  "createdAt": "2030-01-01T10:00:00",
  "updatedAt": "2030-01-01T10:00:00"
}
```

The dates/timestamps and IDs in this response are examples. JSON numbers do not guarantee a fixed display scale. A property referenced by this booking cannot be deleted (409); an unreferenced owned property can be deleted with `DELETE /api/v1/properties/{id}`.

## Known limitations and excluded future work

- The original overlap rule is inclusive: an arrival on another confirmed booking's departure date is unavailable. This was preserved intentionally.
- Date changes recalculate nights and charges using the property's current base rate. No modification-window policy existed, so none was invented.
- Existing pricing formulas are preserved, including unconditional 15% GCT and the existing nightly TEF/GART calculation. This conversion does not validate fiscal policy or add currencies/payments.
- Booking confirmation email is best effort after commit. Failure is logged and does not roll back the booking or change a successful response. There is no email queue/retry worker.
- Registration delivery failure leaves a pending account; retry the same credentials to resend. No password reset, token/session management or dedicated resend endpoint was added.
- SQLite permits one writer at a time. Availability reads all bookings; lists are not paginated. Database contention can return a controlled server error.
- The old database might contain inconsistent data from partial CLI writes. The conversion does not delete, repair or baseline such data automatically.
- Existing employee/review DAOs and other model/stub classes remain outside the HTTP surface and are not represented as completed modules.
- Existing repository tracking of database/build files predates this work. Ignore rules do not untrack already tracked files; no Git index reset or history rewrite was performed.
- Future product work explicitly excluded from V1: cancellations/refunds, payment processing, new booking policies, reservation inventory/locks, reports, review/verification workflows, new roles, frontend, microservices, Docker/cloud deployment and new integrations.
