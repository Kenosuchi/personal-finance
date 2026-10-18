# Personal Finance backend

This directory contains the single Spring Boot application for the Personal Finance project.
R0-002 establishes only the build, HTTP error, request tracing, structured logging, and
money/date wire-contract foundations. Persistence, security, health probes, and financial
workflows are intentionally deferred to their later R0/R1 tickets.

## Toolchain and commands

- Java: 25, without preview features
- Spring Boot: 4.1.1, with Boot-managed dependency versions
- Maven: 3.10.0 through Maven Wrapper 3.3.4

The Maven distribution SHA-512 was checked against Apache's published checksum. The SHA-256
of that same archive is pinned as `distributionSha256Sum` for wrapper verification.

Run the focused backend checks:

```bash
./mvnw test
```

Run the clean build and produce the executable JAR:

```bash
./mvnw clean verify
```

Neither command requires Docker or a database for R0-002.

## Package ownership

- `com.personal.finance.platform.error` owns the HTTP Problem Details mapping.
- `com.personal.finance.platform.web` owns bounded request tracing and request completion logs.
- Future category, transaction, and reporting packages own their respective business behavior,
  but are not created until a ticket introduces real implementation.

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
