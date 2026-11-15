# Personal Finance backend

This directory contains the single Spring Boot application for the Personal Finance project.
R0-002 established the HTTP, error, logging, and money/date wire-contract foundations. R0-004
establishes the MySQL schema, Flyway migrations, minimal JPA mappings, credential separation,
and real-MySQL integration evidence. Financial APIs,
repositories, services, and CRUD workflows remain deferred to R1.

## Toolchain and commands

- Java: 25, without preview features
- Spring Boot: 4.1.1, with Boot-managed dependency versions
- Maven: 3.10.0 through Maven Wrapper 3.3.4

The Maven distribution SHA-512 was checked against Apache's published checksum. The SHA-256
of that same archive is pinned as `distributionSha256Sum` for wrapper verification.

Run the focused non-database checks without Docker:

```bash
./mvnw -Dtest=ApiFoundationTest,SecurityPolicyTest,FinancialJsonContractTest,DatabaseEnvironmentConfigurationTest test
```

Run all tests, including the MySQL 8.4.11 Testcontainers suite:

```bash
./mvnw test
```

Run the clean build and produce the executable JAR. Both complete commands require a working
Docker environment and fail rather than silently skipping the MySQL evidence:

```bash
./mvnw clean verify
```

The integration suite proves empty-schema migration, repeat startup, Flyway checksum failure,
Hibernate mapping failure, schema constraints, runtime-versus-migration privileges, and exact
whole-VND/date persistence. Test data is synthetic and the container lifecycle covers every
Spring context that uses it.

## Local database and application startup

The backend and integration tests use the same MySQL patch, 8.4.11. Create an ignored local
Compose environment file, replace every placeholder password, and start MySQL from the
repository root:

```bash
cp infra/.env.example infra/.env
docker compose --env-file infra/.env -f infra/compose.yml up -d mysql
docker compose --env-file infra/.env -f infra/compose.yml ps
```

The initialization script creates the runtime and migration users only when MySQL initializes
an empty data directory. Changing `infra/.env` does not re-provision an existing `mysql_data`
volume; intentionally provision the retained database as an administrator or start with a new
local volume.

For direct host development, MySQL is published only on `127.0.0.1:3306`. The application
requires separate runtime and migration settings:

- `FINANCE_DATABASE_RUNTIME_URL`
- `FINANCE_DATABASE_RUNTIME_USERNAME`
- `FINANCE_DATABASE_RUNTIME_PASSWORD`
- `FINANCE_DATABASE_MIGRATION_URL`
- `FINANCE_DATABASE_MIGRATION_USERNAME`
- `FINANCE_DATABASE_MIGRATION_PASSWORD`

Spring Boot does not automatically load `.env.local`. After creating the ignored
`backend/.env.local` file with the six settings above, load it into the shell before starting
the backend. Use URLs such as
`jdbc:mysql://127.0.0.1:3306/finance_management?connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true`
and use the same runtime/migration usernames and passwords provisioned by Compose:

```bash
cd backend
set -a
. ./.env.local
set +a
./mvnw spring-boot:run
```

Do not commit `.env` or `.env.local`, print their values, or place credentials in JDBC URLs. The
runtime account is limited to `SELECT`, `INSERT`, `UPDATE`, and `DELETE`. The migration account
owns the schema changes and Flyway history. Database/user creation and grants belong to a third,
provisioning-only administrative identity that the application never receives. Using the same
account for runtime and migration does not satisfy the R0-004 privilege contract.

Flyway is the only schema-change mechanism. Hibernate uses `ddl-auto=validate`, Spring SQL
initialization is disabled, and Flyway clean is disabled. Applied migrations are immutable;
correct an unapplied local migration before acceptance, but add a new migration for any change
after it has been applied to a retained environment.

## Local security and CSRF contract

R0 uses anonymous local access without a login, but still applies a deny-by-default endpoint
policy and CSRF protection. The runtime allowlist is:

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/csrf` | Materialize the Angular-compatible CSRF cookie |
| `GET` | `/actuator/health/liveness` | Reserved for the R0-006 liveness probe |
| `GET` | `/actuator/health/readiness` | Reserved for the R0-006 readiness probe |

Every other method/path combination is denied until its owning ticket explicitly adds it. The
bootstrap response sets a JavaScript-readable `XSRF-TOKEN` cookie; Angular sends its value as
`X-XSRF-TOKEN` on relative unsafe requests. Tokens and request headers must not be logged.

Unsafe requests must contain exactly one `Origin` header equal to
`finance.security.allowed-origin`. Direct development defaults to `http://localhost:4200` and
can be overridden with `FINANCE_SECURITY_ALLOWED_ORIGIN`. R0-007 must set that value to the exact
published frontend origin, preserve the same-origin proxy contract, bind the frontend to
loopback, and reject unapproved Host values. Do not replace the exact origin with a wildcard or
enable permissive CORS.

Missing or invalid CSRF tokens return a `CSRF_REJECTED` 403 Problem Details response. Origin and
endpoint-policy failures return `ACCESS_DENIED`. Both include the request `traceId`. The unsafe
handler used to prove this behavior exists only in test sources and is absent from the runtime
artifact.

## Package ownership

- `com.personal.finance.platform.error` owns the HTTP Problem Details mapping.
- `com.personal.finance.platform.web` owns bounded request tracing and request completion logs.
- `com.personal.finance.categories` owns category persistence and future category behavior.
- `com.personal.finance.transactions` owns transaction persistence and future transaction
  behavior.

Do not expose persistence entities as API DTOs. Do not create generic `common`, `shared`, or
placeholder controller/service/repository packages.

## Error contract

Controller errors use RFC 9457 `application/problem+json`. Standard fields are `type`, `title`,
`status`, `detail`, and `instance`. Extensions are:

- `code`: stable application error code
- `fieldErrors`: optional array containing safe `field` and `message` values
- `traceId`: request correlation identifier

Example validation response:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed.",
  "instance": "/api/v1/transactions",
  "code": "VALIDATION_FAILED",
  "fieldErrors": [
    {"field": "amountVnd", "message": "Invalid value."}
  ],
  "traceId": "5ab2bc7d-686d-4ed9-a128-1d5eac7445e8"
}
```

Framework exception messages, rejected values, stack traces, SQL, credentials, and request
payloads are not returned.

## Request tracing and logs

The optional request header and response header are both `X-Trace-Id`. An incoming value is
accepted only when exactly one value is supplied and it matches
`[A-Za-z0-9][A-Za-z0-9._-]{0,63}`. Otherwise, the backend generates a UUID.

The identifier is available as the `traceId` MDC field while the request runs. Console logs use
Boot's Logstash JSON format. The request completion event records only the method, matched route
template, status, duration, and whether an exception escaped the request chain. It does not log
query strings, headers, bodies, credentials, or user-entered descriptions. Unexpected failures
record a stable error code and exception class without the exception message.

Keep Spring Web logging at INFO in environments containing real data. Its DEBUG/TRACE diagnostics
can include decoded request values, so those levels are not a safe substitute for targeted
diagnostics.

## Financial JSON contract

- Whole-VND values cross the API boundary as canonical decimal strings, never JSON numbers.
- Java financial calculations will use `BigDecimal`, never `float` or `double`.
- Business dates use `LocalDate` and ISO `YYYY-MM-DD`, without times or timezone offsets.

R0-002 tests these rules with test-only fixtures. No production financial DTO or demonstration
endpoint is introduced by this ticket.

## Schema and application validation responsibilities

The schema provides the final integrity boundary for supported enum names, whole values in the
approved positive range after SQL conversion, required expense descriptions, category
references, required nullability, and uniqueness of the supplied `normalized_name` within a
category type. It also prevents category deletion while transaction history refers to it. The
application will still validate these rules in R1 to return useful API errors instead of
exposing database failures.

The following rules cannot be truthfully attributed to `DECIMAL`, `DATE`, or the category
foreign key and remain application responsibilities:

- Reject fractional amount input before conversion or storage; `DECIMAL(19,0)` must not be
  relied on to reject rather than round a fractional value.
- Reject future transaction dates using the approved reporting timezone.
- Trim, normalize to Unicode NFC, lowercase with `Locale.ROOT`, preserve accents, and enforce
  the approved 1–80-character category-name convention before writing `normalized_name`; the
  schema compares the supplied normalized value with a binary collation but does not calculate
  it.
- Verify that a selected category is active and has the same income/expense type as the
  transaction inside the write transaction.

Frontend validation is a usability aid, not an integrity boundary. R0-004 does not claim full
financial correctness or implement category/transaction workflows. Normal startup contains no
seeded user categories.
