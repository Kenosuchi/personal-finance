# Personal Finance Management App — Architecture Baseline v0.1

Status: Proposed — awaiting developer/Product Owner approval.  
Reviewed: 2026-10-09, Asia/Saigon.  
Input: personal-finance-feature-backlog-v0.1.md.  
Deliverable: feasibility review, architecture baseline, draft ADRs, and approval register. No application code, tickets, release commitments, or deployment are included.

Confirmed testing constraint (2026-10-09): Playwright and automated browser/end-to-end testing are not required. Verify browser workflows manually; retain focused frontend and backend automated tests.

## 1. Scope and feasibility

The eight MVP features are feasible with one Angular SPA, one Spring Boot application, and one MySQL database. The product is a manually entered income/expense tracker. It is not a bank ledger, account reconciliation system, or double-entry accounting system.

The supplied file identifies the underlying product baseline as approved, but marks the feature backlog itself as Proposed. The user describes the supplied scope/backlog as approved. Use FEAT-001–008 as the working scope for this review; confirm the backlog's approval status before tickets, without silently rewriting its status. FEAT-009 remains a deferred candidate. Login, remote access, and real financial data remain separate scope-review items.

The modular monolith, Java 25, Spring Boot 4 family, MySQL, solo-development focus, and no-code boundary are current user constraints. Everything more specific below is a proposal unless explicitly identified as a requirement inherited from the backlog.

| Feature | Technical approach | Main correctness concern |
| --- | --- | --- |
| FEAT-001 Categories | Flat typed categories; stable IDs; rename and archive | Archiving must preserve history; historical naming needs a product decision |
| FEAT-002 Income | Multiple positive whole-VND entries | Exact arithmetic; category type; past dates |
| FEAT-003 Expenses | Same transaction model with EXPENSE type | Required purchase description; no negative amounts/refund workaround |
| FEAT-004 Corrections | Atomic updates and confirmed deletion | Old and new reporting periods both change; concurrent edits |
| FEAT-005 History | Filtered, paginated combined list | Stable ordering; archived categories remain filterable |
| FEAT-006 Monthly overview | Exact SQL aggregates by transaction date | Monthly net must not be confused with cumulative balance or budget |
| FEAT-007 Running balance | All income minus expenses through an inclusive date | Starts at zero; not restricted by history filters; not verified funds |
| FEAT-008 Trend | Server-supplied monthly expense aggregates | Missing months and partial recording must not imply complete data |

Example acceptance anchor: January income 10,000,000 and expenses 7,000,000 gives January remaining 3,000,000. February income 5,000,000 and expenses 6,000,000 gives February remaining −1,000,000 and cumulative balance through February of 2,000,000. Editing a January expense must change January results and subsequent cumulative balances immediately.

## 2. Proposed technology baseline

Version observations were checked against official documentation on the review date. These are candidate versions, not a tested dependency manifest.

| Component | Proposal | Reason and compatibility |
| --- | --- | --- |
| Angular and CLI | Angular 22.0.x, matching CLI; pin a reviewed patch at setup | Official compatibility table supports this line. Angular 19 is unsupported. Angular 21 remains a supported fallback if its continuity materially benefits the developer |
| Node/npm | Node 24 LTS, candidate 24.21.0; its bundled npm | Angular 22 requires Node 24.15.0 or later within the 24 line. Pin exact versions and commit package-lock.json |
| TypeScript/RxJS | TS 6.0.x; RxJS 7.x within the Angular-supported range | Angular 22 lists TS >=6.0.0 <6.1.0 and RxJS ^7.4.0; use CLI-compatible patches |
| Java | Eclipse Temurin 25 LTS; current observed release 25.0.4.1+1 | No preview features; same reviewed Java release in development, CI, and runtime |
| Spring Boot | Candidate stable 4.1.1 | Official requirements permit Java 25; use the Boot dependency BOM, not manually selected Spring/Hibernate/Jackson versions |
| Build | Maven Wrapper, reviewed supported 3.9.x patch | One Maven application; pin wrapper distribution and checksum |
| MySQL | MySQL 8.4 LTS; candidate 8.4.12 | Official release notes list 8.4.12. Verify container tag/digest availability before pinning it; tests and Compose use the same patch |
| Persistence | Spring Data JPA; Boot-managed MySQL Connector/J; Flyway including flyway-mysql | CRUD via JPA; aggregates via explicit projections/queries; one schema migration mechanism |
| Frontend tests | CLI Vitest; manual browser workflow checks | Vitest is the current CLI default; no automated browser/end-to-end framework is required |
| Backend tests | Boot-managed JUnit/Mockito; MySQL Testcontainers | Actual MySQL behavior, constraints, migrations, and aggregates |

Exact Angular patches, chart-library choice, testing patches, and container digests are implementation-readiness work. No claim is made that every transitive dependency or Docker image has been verified. Recheck support and security updates when implementation starts. Do not copy older Boot starter names, testing imports, or dependency versions without checking Boot 4 documentation.

## 3. Architecture and boundaries

One repository containing frontend/, backend/, infra/, and docs/adr/. One backend build, JAR, deployment, and MySQL schema. Use business packages rather than a global controllers/services/repositories layout. There is no requirement for Maven multi-module builds, JPMS modules, Spring Modulith, or a generic ports-and-adapters framework.

Runtime: browser → frontend static server/reverse proxy → Spring MVC REST API → MySQL. The proxy serves Angular and routes /api to the backend on the same browser origin. The dev server uses a corresponding local proxy.

| Boundary | Frontend responsibility | Backend responsibility | Allowed dependencies |
| --- | --- | --- | --- |
| Categories | Category list, create/rename/archive, category selectors | Own category records, validation, availability checks, locking | No dependency on transactions or reports |
| Transactions | Combined history, filters, entry/edit forms, delete confirmation | Own transaction records, commands, history queries, exact aggregate query facade | Categories' public application API |
| Reports | Monthly overview, cumulative balance, expense trend | Read-only orchestration, date ranges, report response assembly | Transactions' read facade and Categories' batched read API |
| Platform | Shell, routing, HTTP/error handling, genuinely reused controls | Security, configuration, problem responses, health/logging | Business modules must not depend on platform business logic |

Within each backend business package, keep controller/DTO, application service, domain/entity, and repository responsibilities clear without manufacturing interfaces for every class. Controllers do not contain finance rules. Public module APIs expose DTOs/value types, never JPA entities or repository objects. Transactions own the aggregate SQL for their table; reporting does not bypass boundaries by importing private repositories. Reports fetch category names in batches, avoiding per-row lookups. A category foreign key is an intentional schema relationship, not a license for arbitrary cross-module writes.

Frontend: standalone components, lazy feature routes, typed reactive forms, HttpClient services, signals for local UI state and RxJS for request composition. Follow the supported zoneless setup; updates must notify Angular through signals, async pipe, or documented mechanisms. Keep loading/error state per independent request. No NgRx, shared global loading flag, SSR, or PWA initially. One chart dependency can be added for FEAT-008 after reviewing support and accessibility; do not build a chart framework. Provide readable totals/table alongside the chart.

Reports are authoritative on the server. After successful mutation, refetch affected visible history/reports; do not maintain competing frontend balances. No optimistic financial mutations. Simple package-boundary review is enough initially; add automated dependency checks if violations become a recurring risk.

## 4. Financial, date, and category rules

### Already required by the backlog

- Currency is VND; entry amounts are positive whole units. Income and expense are different types, not signed entry amounts.
- Multiple income entries and past-date entries are supported.
- Monthly remaining = selected-month income − selected-month expenses.
- Cumulative balance = all entered income − all entered expenses through the selected date, inclusive, starting from zero.
- Negative results are valid and visible. There is no overdraft block.
- Refunds, borrowing, credit-card workflows, multiple accounts, and budgets are outside the MVP.

### Proposed product clarifications requiring approval

| Question | Proposed rule | Trade-off |
| --- | --- | --- |
| Reporting calendar | Gregorian months; Asia/Ho_Chi_Minh determines today/default month | Predictable Vietnam behavior; no per-user timezone setting |
| Transaction date | Calendar DATE, ISO YYYY-MM-DD; accept past dates and reject future dates | Avoids accidental planned transactions; scheduled entries would need new scope |
| Input bounds | Amount 1–999,999,999,999 VND; category name 1–80 characters after trimming; description max 1,000 characters | Explicit defaults proposed for validation, not inferred requirements; final Unicode length-count convention belongs in the API specification |
| Descriptions | Expense description required after trimming; income description optional | Matches the stated purchase-recording interaction |
| Category identity | Income/expense type immutable; unique normalized name per type, including archived categories | Keeps classification stable; archived names cannot be silently reused as a different category |
| Historical names | Show current category name everywhere, including old entries; group by stable ID | Simpler and coherent; does not preserve the old label as it appeared on entry day |
| Archive behavior | Cannot select archived category for a new entry or category change; existing entry may retain it while amount/date/description is corrected | History remains editable; archived categories stay visible in history filters/reports |
| Transaction type correction | Allow type change with a matching active category and valid description | Supports correction without adding refund semantics |
| Transaction deletion | Permanent deletion after confirmation; no restore/audit feature | Simplest mistake correction; recovery depends on backups |
| Initial categories | User creates categories; no assumed predefined seed list | Less hidden product behavior; first-use UI must explain category creation |
| Trend coverage | Default last 12 calendar months through current month; browsable ranges across all recorded history, at most 120 months per request | Bounded responses; whole history remains accessible through multiple ranges |
| Empty/gap months | Return expense total zero and recorded entry count; label as recorded data, current month partial | Zero does not prove no spending occurred or that a month is complete |

Selection filters on transaction history do not alter cumulative balance. Monthly overview is one whole month's result, and any later filtered analytics require separate scope. Backdated editing affects results by transaction date, never created_at.

## 5. Database conventions and money representation

Two business tables initially: categories and transactions. Flyway's metadata table is additional infrastructure. No balances, monthly report snapshots, accounts, users, or budget tables are introduced.

| Table | Conceptual fields |
| --- | --- |
| categories | id, type, name, normalized_name, archived_at, version, created_at, updated_at |
| transactions | id, type, amount_vnd, transaction_date, category_id, description, version, created_at, updated_at |

Use InnoDB; utf8mb4; snake_case SQL names; explicit nullability; named foreign keys, checks, and indexes. Use auto-increment BIGINT IDs and expose IDs as strings in JSON. Persist enum names rather than Java ordinals; VARCHAR plus a check constraint avoids a database ENUM migration dependency. No cascade deletion of categories or transactions. Archived categories are retained permanently under this MVP proposal; category deletion is not exposed.

Proposed money representation: DECIMAL(19,0) in MySQL, BigDecimal in Java, and decimal digit strings in JSON, including aggregate totals. Reject fractional input before storage; never let scale conversion silently round it. Whole-VND frontend input remains a canonical digit string, and exact whole-unit operations/formatting use bigint where needed. Do not calculate authoritative totals in JavaScript Number, Java double, or SQL FLOAT. Chart coordinates can use Number strictly for geometry; tooltips/totals retain the exact string value. Negative report totals are serialized as signed strings.

DECIMAL/BigDecimal is slightly more verbose than BIGINT/long but keeps one exact representation across entry and aggregate calculations. JSON strings avoid relying on JavaScript's safe-integer limit. SQL SUM over exact inputs remains exact; null sums become zero. Detect aggregate/serialization overflow rather than truncating.

Constraints: amount within approved positive range; supported type; non-null valid category reference; required expense description; unique (type, normalized_name). Proposed normalization is trim, Unicode NFC, and Locale.ROOT lowercase, with an explicit binary comparison for normalized_name; preserve accents. Java and database enforcement must agree. Category/type compatibility and future-date checks are enforced by the application inside the write transaction; frontend checks are convenience only.

Start with transaction_date/id ordering support and category_id/transaction_date/id index; inspect query plans before adding more indexes. Monthly queries use date >= monthStart AND date < nextMonthStart so the date predicate can use an index. History ordering is transaction_date DESC, id DESC, avoiding ties between same-date entries. Time-of-creation does not claim the order of purchases within a day.

Use UTC audit timestamps with explicit JDBC/session timezone settings, and DATE for business dates. Flyway alone changes the schema: validate migrations and Hibernate mappings; ddl-auto=validate, SQL auto-init disabled, Flyway clean disabled. Never edit an applied migration. Development sample data is separate and opt-in.

## 6. Writes, consistency, and errors

Each create/edit/delete/archive is one database transaction. Responses indicate success only after commit. Use JPA optimistic versions for transaction and category edits; a stale edit/delete returns 409 and the UI offers reload rather than overwriting another tab's work.

On create/category reassignment, lock/check the selected category until the mutation commits; archiving takes the same category lock. An archive that commits first makes a subsequent new assignment fail. A new assignment that commits first remains valid historical data after later archiving. Use a consistent lock order and test the race. This is short database coordination, not distributed locking.

Each multi-query report executes in a single explicit read-only repeatable-read transaction for a coherent snapshot. Separate HTTP report requests can observe different commits; the UI does not claim a cross-request snapshot. There are no cached report totals to invalidate. Query from current committed transaction data on each request.

Disable repeated submits while pending and do not automatically retry POST. A timeout after server commit has an uncertain outcome: tell the user to check history before retrying. This baseline does not guarantee exactly-once creation. If automatic retries/offline submission become requirements, propose a persisted idempotency key separately; do not treat duplicate amount/date/description as proof of a duplicate transaction.

API conventions:

- /api/v1/categories, /api/v1/transactions, /api/v1/reports/monthly-summary, /api/v1/reports/running-balance, /api/v1/reports/expense-trend.
- GET reads; POST creates with 201 and Location; PUT replaces editable resource fields with version; DELETE transaction with an agreed version precondition and 204. Archive is an explicit resource operation. Category type is immutable. PUT never silently creates a missing record.
- ISO dates/months/timestamps; camelCase DTO fields; explicit VND currency metadata on reports; decimal strings for money; no JPA entity serialization.
- History: optional month/type/categoryId filters; page is zero-based, default size 20 and max 100; fixed stable ordering. Reject invalid filter values. Do not expose Spring Page serialization as the public contract; use a small documented envelope.
- 400 for malformed requests, validation, and invalid ranges; 404 missing resource; 409 version/category availability/name conflicts; 403 CSRF/access denial; 500 unexpected failures; 503 dependency unavailable. Authentication adds 401 only if introduced later.
- RFC 9457 application/problem+json via Spring ProblemDetail, with stable application code, fieldErrors, and traceId extensions. Cover security-filter errors as well as controller exceptions. Never return SQL, stack traces, credentials, or entered descriptions in error diagnostics.
- Keep an OpenAPI contract in docs for routes, DTOs, constraints, and examples. No requirement for a runtime Swagger dependency or generated clients in the baseline.

## 7. Security and operating envelope

Proposed initial envelope: one local user, synthetic/example data, loopback-only access, no login. This is an assumption requiring explicit acceptance because the backlog excludes login, remote access, and real-data usage from its current review. It is not a production financial-data security design. CSRF does not provide authentication, and other local processes/users can reach an anonymous local API.

Retain Spring Security for an explicit method/path allowlist, CSRF protection for mutations with the supported Angular SPA token bootstrap flow, and default security headers. No generated development user, form login, HTTP Basic, JWT, OAuth, or authorization roles. Deny unlisted endpoints. Do not use a broad permitAll on every URL or disable CSRF merely because access is local. CORS stays closed by default; same-origin proxying avoids wildcard CORS. Validate allowed Host/Origin behavior at the local edge and test that an external origin cannot mutate data.

Render user text as text; avoid untrusted innerHTML. Bound request bodies, page/range sizes, and string fields. Use parameterized queries and separate DTOs to prevent arbitrary entity-field binding. Financial API responses are not stored in localStorage; return appropriate no-store policy. Secrets live outside git and images, with synthetic placeholders in documentation; missing configuration fails startup clearly.

MySQL is not published in full Compose mode. Backend is internal by default; host-exposed debugging is an explicit loopback-only override. Non-root backend/frontend containers. Application runtime uses a non-root MySQL account with CRUD privileges; a separate migration credential has schema-change privileges. The database's own initialization admin credential is not the application's runtime identity.

Only minimal readiness/liveness are available for health probes; other Actuator endpoints are denied. Readiness includes database availability; liveness does not fail just because MySQL is temporarily down. Restrict health details and avoid routing operational endpoints through the public SPA proxy by default.

Before real data, LAN, or internet use, reopen authentication, owner isolation, HTTPS, backups, credential handling, and deployment decisions. Multi-user access requires ownership columns and authorization, not just a login screen. Do not add those columns and workflows speculatively now.

## 8. Testing and necessary infrastructure

| Layer | Required evidence |
| --- | --- |
| Domain/unit | Exact positive/negative totals, input scale rejection, date ranges, leap-year and boundary behavior |
| Backend MySQL integration | Flyway from empty schema; constraints; aggregates; archive/assignment race; optimistic conflicts; edits/deletes affecting old and new months; repeatable report reads |
| API/security | DTO serialization, validation/ProblemDetail, filters/pagination, unsupported methods, denied endpoints, CSRF bootstrap and rejected unsafe requests |
| Angular Vitest | Form validation; exact amount formatting; archived category selection; loading/error/empty states; stale-edit UX; request cancellation for changing filters |
| Manual browser acceptance | Create categories and income/expense → correct an entry → verify monthly/cumulative/trend results → archive category → verify retained history; delete confirmation/cancel; record expected and observed results |
| Delivery | Reproducible builds; non-root runtime; Compose health, persistence after restart, SPA deep links, API/static 404 behavior, backup restore rehearsal |

Use MySQL Testcontainers rather than H2 for persistence evidence because the application relies on MySQL constraints, collations, DATE behavior, and exact aggregates. Mock isolated collaborators sparingly; tests should demonstrate behavior instead of mirroring method calls. No arbitrary blanket coverage percentage. Manual browser acceptance supplements focused automated tests. Do not add an automated browser/end-to-end framework as a baseline or CI requirement.

Required infrastructure: Docker Engine/Desktop and Compose; three services (frontend proxy/static server, backend, MySQL); MySQL named volume; environment-specific configuration; structured console logging with request trace IDs; minimal health endpoints; GitHub Actions build/test/container-build checks; documented logical backup/restore. A named volume is persistence, not a backup. Keep backup artifacts outside the database volume and out of git; demonstrate restoration before accepting retained data.

Use multi-stage image builds. Pin reviewed patch-specific image tags and immutable digests; do not invent digests in this review or use latest. Node builds the SPA and is absent from its runtime. Use a reviewed unprivileged Nginx release, non-privileged container port, and Java 25 runtime. Compose gates backend startup on MySQL service_healthy, but the backend must still behave correctly when the DB later becomes unavailable. SPA fallback applies to navigation; missing static files and /api routes return real errors instead of index.html.

Development can run Angular/backend directly with MySQL in Docker using a loopback DB override; full-stack local acceptance uses all three containers. CI integration tests need a Docker-capable runner. Docker Compose health/persistence and backups are delivery requirements, not extra FEAT IDs. Remote hosting, environments called staging/production, and automatic deployment are not assumed approved.

Excluded dependencies: Redis, message broker, Kubernetes, Elasticsearch, service discovery, event sourcing, CQRS framework, distributed locks, Grafana/Prometheus stack, and microservices. None solves an evidenced MVP requirement. Add infrastructure only for a measured problem or an explicitly approved learning objective.

## 9. Draft ADRs

All ADR statuses below are Proposed. These records document candidates for approval; they are not accepted architecture decisions. The modular-monolith constraint itself is already mandated by the user.

### ADR-001 — One application with business module boundaries

**Context:** Eight closely related features, one developer, no separate scaling or team ownership needs.  
**Proposal:** One Maven/Spring Boot app with categories, transactions, reports, and minimal platform packages; one schema and one Angular app.  
**Alternatives:** Global technical-layer packages are initially quick but weaken ownership; Maven modules/Modulith add enforcement and setup; microservices add deployment and consistency costs without a requirement.  
**Consequences:** Low operating cost and local ACID transactions; package discipline must be maintained by review. No generic architecture framework.  
**Verification:** Review dependency directions and ensure reporting never writes financial records.  
**Revisit:** Repeated boundary violations or an actual independently operated capability.

### ADR-002 — Supported runtime families and reproducible pins

**Context:** Earlier learning intent included Angular 19, which conflicts with supported-version constraints.  
**Proposal:** Angular 22/Node 24 LTS, Java 25 LTS, Spring Boot stable 4.1.1 candidate, MySQL 8.4 LTS; pin reviewed patches and container digests at setup; let the Boot BOM manage compatible dependencies.  
**Alternatives:** Angular 21 remains a supported continuity option; Angular 19 fails the support constraint; preview versions add avoidable compatibility risk.  
**Consequences:** Current supported learning stack; exact manifest requires build/integration proof and maintenance updates.  
**Verification:** Compatibility matrix, clean frontend/backend builds, actual MySQL integration, image availability, and supported test tooling.  
**Revisit:** End of support, relevant security fix, or incompatible required dependency.

### ADR-003 — Exact whole-VND values end to end

**Context:** Monetary correctness includes aggregate totals, JSON, and frontend display.  
**Proposal:** DECIMAL(19,0), BigDecimal, JSON decimal strings, exact frontend string/bigint handling; positive entries and type determine sign.  
**Alternatives:** BIGINT/long is simpler for individual values but requires aggregate bounds/overflow discipline; Number/double/FLOAT are rejected for authoritative values.  
**Consequences:** More explicit parsing/formatting; no decimal rounding or JS safe-integer dependency. Chart geometry is approximate while shown values remain exact.  
**Verification:** Round trips near input limits, rejected fractions, aggregate precision, negative balances, overflow failure.  
**Revisit:** Multi-currency or fractional currency introduces a new money model.

### ADR-004 — Calendar dates and live derived reports

**Context:** Backdated corrections affect multiple periods; audit timestamps must not drive financial reporting.  
**Proposal:** DATE business fields, Vietnam reporting calendar, no future entries, zero opening balance; compute reports on demand inside coherent read transactions.  
**Alternatives:** Instant-based business dates risk timezone shifts; cached totals/materialized snapshots add invalidation; opening balance adds scope.  
**Consequences:** Consistent date semantics and immediate correction effects; aggregates cost queries. Separate requests are not one shared snapshot.  
**Verification:** Month boundaries, leap days, backdated edits, same-report consistency, January/February acceptance example.  
**Revisit:** Measured query latency, month-closing requirements, or scheduled entries.

### ADR-005 — Stable categories with current-name history

**Context:** Renaming/archive must preserve financial records.  
**Proposal:** Stable IDs, immutable category type, archive without delete, current labels for history, new assignment only to active matching category.  
**Alternatives:** Name snapshots preserve original labels but complicate editing/aggregation; category cascade deletion destroys history.  
**Consequences:** Simpler reports, but historical labels change after rename. Normalized names stay reserved when archived.  
**Verification:** Rename/archive visibility, historical filters, duplicate names, unchanged archived assignments during corrections, archive/write race.  
**Revisit:** User needs original-label audit history or restore/category merge workflows.

### ADR-006 — Mutable entries, atomic corrections, bounded concurrency

**Context:** Mistake correction is core scope; bank-grade immutability and audit requirements are absent.  
**Proposal:** Edit entries, permanent confirmed deletion, optimistic version preconditions, short category locks, authoritative server reports. Disable repeated submissions and automatic POST retries.  
**Alternatives:** Soft deletion adds pervasive filters and restore semantics; append-only adjustments/event sourcing add workflows excluded from scope; automatic retries require persisted idempotency.  
**Consequences:** Direct corrections and small schema; no audit trail/undelete or exactly-once create guarantee. Backup and uncertain-submit UX matter.  
**Verification:** Stale edits/deletes, atomic rollback, old/new month totals, duplicate-click/timeout UX.  
**Revisit:** Audit, undo, offline entry, or automatic retries become actual requirements.

### ADR-007 — Same-origin REST and standard problem responses

**Context:** One SPA consumes one synchronous backend.  
**Proposal:** /api/v1 REST with DTOs, bounded pagination, exact date/money formats, RFC 9457 errors, version conflict contract, documented OpenAPI.  
**Alternatives:** GraphQL and a universal response wrapper add no evidenced value; JPA entity exposure couples clients to persistence.  
**Consequences:** Familiar debuggable API; contract and frontend error mapping must remain aligned. Reverse proxy setup is needed.  
**Verification:** Contract serialization, status/error tests, security errors, actual API/static failures through proxy.  
**Revisit:** Independently evolving external consumers or a new integration requirement.

### ADR-008 — Local synthetic-data access boundary

**Context:** Login, remote access, and real-data use require separate product review.  
**Proposal:** Loopback-only anonymous local learning app with synthetic data, explicit security rules, CSRF/headers, private DB, non-root app containers, separate runtime/migration credentials.  
**Alternatives:** Authentication now expands product scope; anonymous public/LAN access does not fit this envelope.  
**Consequences:** Less identity complexity; no privacy boundary against other local users/processes. Real data and remote access require new decisions.  
**Verification:** Port exposure, external-origin mutation rejection, endpoint deny rules, absence of default credentials/secret leakage.  
**Revisit:** First real-data or remote-access requirement, before enabling it.

### ADR-009 — Real-database testing and small Compose delivery

**Context:** SQL behavior and migration correctness matter more than broad mocked coverage. Docker/CI are stated delivery requirements.  
**Proposal:** Focused unit/API tests, MySQL Testcontainers, Angular Vitest, manual browser acceptance, three-service Compose, health/logging, CI, backup/restore. Automated browser/end-to-end testing is not required under the confirmed user constraint.  
**Alternatives:** H2 is faster but not the deployed database; full observability/cloud orchestration adds operating work; manual-only checks make regression evidence hard to repeat.  
**Consequences:** Requires Docker locally/CI and some slower integration checks; catches actual database/delivery failures without extra runtime services.  
**Verification:** Required behavior matrix and restore evidence above.  
**Revisit:** Measured CI cost, operational incidents, or approved hosting scope.

## 10. Approval register and next boundary

| Approval ID | Decision to confirm | Recommendation |
| --- | --- | --- |
| AP-01 | Backlog status | Confirm FEAT-001–008 approved; FEAT-009 remains deferred; no prior R1 is assumed |
| AP-02 | Supported frontend line | Angular 22 + Node 24 LTS; Angular 21 only if continuity is preferred |
| AP-03 | Initial operating/data envelope | Local-only synthetic data without login; revise scope now if real data or remote use is intended |
| AP-04 | Financial representation and persistence | Exact VND strings/DECIMAL/BigDecimal; live server reports; proposed two-table model |
| AP-05 | Date, bounds, and chart semantics | Accept or revise the proposed clarification table, especially future dates, reporting timezone, limits, and gap months |
| AP-06 | Category and correction semantics | Current-name history, archive rules, no predefined categories, permanent deletion, no audit/restore |
| AP-07 | Architecture/API/delivery standards | Approve proposed module realization, REST/errors/concurrency, test matrix, local Compose/CI/backup baseline |

AP-01–07 are user decision gates, not external permission prompts. A response can approve the baseline with named exceptions; exact patch/image pins remain a technical readiness check rather than a repeated product approval. ADR acceptance records should identify who approved and when. Approval of this review must not silently alter the uploaded backlog, approve FEAT-009, create a release plan, or authorize remote deployment.

After resolving these decisions, the next artifact is an accepted baseline/ADR revision and precise API/schema specification. Release planning and development tickets are separate steps on request. No implementation should start on unresolved financial/access semantics.

## Official references

All checked 2026-10-09. These sources verify framework/platform facts; the product policies and architecture choices above are proposed judgments, not vendor requirements.

- Angular support: https://angular.dev/reference/releases
- Angular compatibility: https://angular.dev/reference/versions
- Node release status: https://nodejs.org/en/about/previous-releases
- Temurin support: https://adoptium.net/support
- Spring Boot requirements: https://docs.spring.io/spring-boot/system-requirements.html
- MySQL release tracks: https://dev.mysql.com/doc/refman/8.4/en/mysql-releases.html
- MySQL 8.4.12 release notes: https://dev.mysql.com/doc/relnotes/mysql/8.4/en/news-8-4-12.html
- MySQL exact aggregate types: https://dev.mysql.com/doc/refman/8.4/en/aggregate-functions.html
- Boot migrations and MySQL Flyway module: https://docs.spring.io/spring-boot/how-to/data-initialization.html
- Spring ProblemDetail/RFC 9457: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html
- Spring Security SPA CSRF: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
- Boot Testcontainers: https://docs.spring.io/spring-boot/reference/testing/testcontainers.html
- Angular testing: https://angular.dev/guide/testing
- Angular zoneless: https://angular.dev/guide/zoneless
- Angular reactive forms: https://angular.dev/guide/forms/reactive-forms
- Compose dependencies/health: https://docs.docker.com/reference/compose-file/services/
