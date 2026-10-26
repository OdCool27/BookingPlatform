# Osyntra BookingPlatform V1

React, TypeScript and Vite frontend connected to the existing Core Java HTTP API. Application actions use real requests; there are no simulated successes or unsupported payment/cancellation actions.

## Run locally

Requirements: Node 22.12+ (verified with Node 24), Java 24 and Maven. Use `npm.cmd` on Windows if PowerShell blocks `npm.ps1`.

From the repository root:

```powershell
.\start-dev.ps1
```

The backend owns email delivery, account activation, authentication and business services. The frontend only sends HTTP requests and displays the returned results. No email-service configuration is needed in the frontend; consult the backend documentation for backend setup.

In a second terminal, from `frontend`:

```powershell
npm.cmd ci
npm.cmd run dev
```

Open **http://127.0.0.1:5173**. Vite uses a strict port. The development launcher explicitly trusts both `localhost` and `127.0.0.1` on ports 5173 (development) and 4173 (preview). It builds the backend and inherits your existing SMTP settings. Stop the old backend before running it. Production defaults are unchanged.

If you run `app.Main` directly in IntelliJ, add this variable under **Run > Edit Configurations > your backend configuration > Environment variables**, then stop and restart that run configuration:

```text
TRUSTED_FRONTEND_ORIGINS=http://localhost:5173,http://127.0.0.1:5173,http://localhost:4173,http://127.0.0.1:4173
```

Setting this in the frontend's `.env.local` or in a separate terminal does not configure an IntelliJ-launched Java process. A 403 saying `Frontend origin is not trusted` occurs before registration, activation or SMTP runs. Use the exact scheme, host and port from the browser address bar, with no trailing slash.

Optional: copy `.env.example` to `.env.local`.

| Setting | Default | Purpose |
| --- | --- | --- |
| `API_PROXY_TARGET` | `http://127.0.0.1:8080` | Vite server-side target |
| `VITE_API_BASE` | `/api/v1` | Relative browser prefix, no trailing slash |
| `TRUSTED_FRONTEND_ORIGINS` | empty | Backend environment: comma-separated exact trusted origins |

Restart the relevant process after configuration changes. Vite forwards `/api` and `/health`, preserving Origin. Java checks each supplied Origin against exact allowlist membership. Empty configuration rejects supplied browser origins; absent Origin remains supported for non-browser clients. Wildcards, paths, userinfo, queries and fragments are invalid configuration. Untrusted, null, duplicate and combined origins are rejected. Java remains bound to loopback.

This setup uses same-origin requests. Direct cross-origin access and CORS/preflight are not implemented or needed. The frontend rejects absolute API prefixes.

## Behavior

- Register as guest or owner, activate using the emailed six-digit code, then log in. Activation works after refresh by re-entering email, password and code. Explicit registration retry with the original password can send a fresh code; it does not replace the pending profile.
- Owner access requires successful `/properties/mine`, including an empty array. Only a 403 permits probing `/bookings` for guest access. Network errors, 401 and 500 never imply a role.
- Guests browse properties, search availability, confirm bookings, inspect history/details, and change dates. Creation immediately confirms the booking. Updated charges come from the backend using the property's current rate.
- Owners create/edit complete property records and confirm deletion. Referenced properties report deletion conflicts.
- Credentials and private data remain in React memory. Logout unmounts private views; refresh requires login. Basic encoding is UTF-8-safe. No browser storage or cookies persist credentials.
- Date-only values remain strings; server timestamps display without timezone conversion. IDs retain exact values. Money formatting does not invent a currency code.
- Ambiguous booking submission failures offer history recovery without automatic retry. Availability is informational; confirmed booking overlap includes departure dates.

## Source layout

`src/api/`: typed client and HTTP errors. `src/auth/`: memory-only account state. `src/components/`: form/status helpers. `src/pages/`: account, property and booking workflows. `src/types/`: API DTOs. Hash navigation requires no static-host route rewrites.

Backend changes are limited to `../src/main/java/router/AppRouter.java` and the new `../src/test/java/integration/BrowserOriginTest.java`. Existing authentication/business services are unchanged.

## Build and tests

```powershell
npm.cmd test
npm.cmd run build
npm.cmd run preview
# From repository root:
mvn.cmd test
```

Production output is `dist/`. Preview uses `http://127.0.0.1:4173`, with the same proxy configuration and an explicitly trusted origin.

Verification completed:

- TypeScript check and Vite production build passed.
- All 27 frontend tests passed, covering UTF-8 Basic, request contracts, 204, error statuses, connection/non-JSON failures, cancellation, role probes, registration, activation, navigation, logout, explicit 503 retry, invalid date order, confirmation, 409 feedback and ambiguous-booking recovery.
- All 20 Java tests passed, including trusted and untrusted origins and existing business integration tests.
- Chrome smoke test passed through Vite to real Java services and isolated SQLite: guest/owner registration, activation and login; empty dashboard; property POST/PUT/DELETE with 204; availability; booking creation/history/date changes; server totals 238.00 and 357.00; overlap and referenced-deletion 409; logout and empty browser storage.

### Browser verification against your backend

Start the backend and frontend using the development instructions above. Use test accounts and properties:

1. Register a guest and an owner using inboxes you can access. Enter the activation codes delivered by the backend, then log in with each account and verify the appropriate navigation.
2. As the owner, create, edit and delete an unbooked property. Create another property for the guest checks.
3. As the guest, search availability, confirm a booking, open booking history/details, and change its dates. Check the returned charges and conflict messages.
4. Log out and refresh; verify that login is required again.

The earlier automated browser verification used an isolated backend test fixture. That fixture and its mail-capture script have been removed from the frontend; ongoing browser checks use the existing backend and its normal activation emails.

## Deployment and limitations

Serve `dist/` over **HTTPS** behind a production reverse proxy forwarding `/api` and `/health` to Java on loopback. Set the exact public HTTPS origin in `TRUSTED_FRONTEND_ORIGINS`. Preserve Origin and Authorization; never strip Origin to bypass validation. Basic is encoding, not encryption. Do not publicly expose Java or the Vite development server. Do not cache protected API responses or log credentials/password bodies.

Real email delivery remains a backend verification concern; the earlier activation checks used captured test emails. Remote HTTPS deployment was not performed. Direct cross-origin CORS is unsupported because same-origin proxying is the supported topology. The API does not provide currency, images, account roles or booking property names; the app uses neutral amounts, visual placeholders, endpoint-based role detection, and property-name lookup with an ID fallback. Features excluded from V1 remain excluded.
