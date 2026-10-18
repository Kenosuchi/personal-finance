# Personal Finance Management App — R0 Ticket Breakdown v0.1

Status: Proposed — awaiting approval of this ticket breakdown.  
Prepared: 2026-10-09.  
Release: R0 — Local delivery foundation.  
Capacity: One developer, approximately 10–20 hours/week.  
No application code, repository changes, CI execution, or deployment have been performed.

## Planning authority and release boundary

Sources: personal-finance-release-roadmap-v0.1.md, especially the R0 acceptance criteria; personal-finance-architecture-baseline-v0.1(1).md, including its confirmed no-Playwright constraint. R0 is inferred from the explicitly requested R0-001 ticket IDs. No R1–R4 tickets are included.

The supplied files still retain Proposed headers. The user's current instruction identifies the release/architecture as approved, so this proposal treats their stated R0 scope and recommended architecture choices as planning authority. R0-001 records that authority and any named exceptions; it is not a request to reapprove every previously supplied decision. This new ticket breakdown and its revised estimate remain Proposed. Source files are not rewritten by this proposal.

R0 establishes one Angular application, one Spring Boot modular monolith, and one MySQL schema. It delivers the shell, build/test foundations, migrations/mappings, local security, health/logging, container delivery, CI, and runbook. No FEAT ID is completed by R0.

The database ticket proposes creating the architecture's two initial business tables and minimal persistence mappings as schema foundation. This is an explicit interpretation of R0's migration/mapping criterion, not permission to implement category or transaction workflows. It avoids a dummy infrastructure table and avoids treating Hibernate validation against zero mappings as meaningful evidence.

Excluded across every ticket: category/transaction CRUD APIs and screens, financial reports/charts, mutation concurrency workflows, budget, login, real financial data, remote deployment, automated browser/end-to-end frameworks, Redis/brokers, microservices, and new observability platforms. Those financial behaviors and their feature tests belong to later releases. Test-only fixtures/probes must not become production routes or user features.

## Ticket index and dependency order

Complexity is relative: S = a small bounded change; M = several cooperating components or integration points; L = high uncertainty or breadth, requiring re-slicing before implementation. No L ticket is proposed. Hours include focused tests, ticket-specific documentation, and ordinary debugging; they are estimates, not timeboxes or commitments.

| ID | Title | Type | Direct dependencies | Complexity | Active effort |
| --- | --- | --- | --- | --- | --- |
| R0-001 | Record accepted decisions, contracts, and runtime pins | Task | None | M | 3–5 h |
| R0-002 | Establish backend build, error, and logging foundation | Task | R0-001 | M | 3–5 h |
| R0-003 | Establish Angular shell and HTTP test foundation | Task | R0-001 | S | 3–5 h |
| R0-004 | Establish MySQL schema, migrations, mappings, and integration tests | Task | R0-002 | M | 4–7 h |
| R0-005 | Establish local security and SPA CSRF bootstrap | Task | R0-003, R0-004 | M | 4–7 h |
| R0-006 | Establish readiness and liveness behavior | Task | R0-005 | S | 2–4 h |
| R0-007 | Build non-root application images and same-origin proxy | Task | R0-006 | M | 3–5 h |
| R0-008 | Assemble the private three-service Compose environment | Task | R0-007 | M | 3–5 h |
| R0-009 | Establish meaningful build, test, and image CI | Task | R0-007 | M | 3–5 h |
| R0-010 | Complete runbook and verify R0 release readiness | Task | R0-008, R0-009 | M | 3–5 h |

The list is topologically ordered. Transitive dependencies apply: R0-007 depends on both applications through R0-005/006, and R0-008 includes R0-004's DB prerequisite. R0-002 and R0-003 can be reviewed independently after R0-001; R0-008 and R0-009 can be reviewed independently after R0-007. No parallel developer staffing is assumed.

Total: **31–53 active hours**, versus the roadmap's preliminary **16–24 hours**. The scope is unchanged; the difference comes from decomposing its security, database privileges, container/proxy, and actual testing gates. A 16–24-hour cap would require reusable existing foundations or an explicitly revised release boundary. Neither is evidenced by the supplied documents. At 10–20 hours/week, the revised range is approximately 1.5–5.5 weeks depending on capacity; it is not a release date. Re-estimate after R0-001 and after the first database/security integration.

## Shared Definition of Done

The following applies to every ticket in addition to its ticket-specific DoD:

- Its acceptance criteria are demonstrated, with actual checks recorded rather than a no-op/pass placeholder.
- Relevant focused automated checks and manual checks pass; no arbitrary global coverage target.
- Changes are independently reviewable, consistent with approved module ownership, and do not implement another release's workflows.
- Applicable configuration/contracts/docs are updated in the same change; no secrets, personal financial data, or diagnostic payload contents are committed.
- No unresolved defect invalidates the ticket's promised behavior. An unresolved decision is recorded with its dependent work blocked, not marked Done.
- Existing applicable checks remain passing. No Playwright or substitute automated browser framework is required.
- Completion evidence identifies commands, environment/version, and observed outcomes without leaking credentials. Work is reviewed before it is represented as accepted.

## R0-001 — Record accepted decisions, contracts, and runtime pins

**Type:** Task.  
**Objective and background:** Convert the supplied approved planning baseline into an unambiguous implementation record. Candidate framework versions and operational details are not yet proven build inputs.  
**Complexity / estimate:** M, 3–5 active hours.

**Scope of work**

- Record the accepted architecture choices and any explicit user exceptions, including no-login/local-only/sample-data operation and no automated browser testing. Link to the supplied source documents; do not invent a prior approval date.
- Document the baseline money/date/category/correction rules needed before R1: whole-VND strings, accepted ranges, Vietnam reporting calendar, future-date rejection, normalization/length convention, permanent deletion, and optimistic-version contract. Preserve the source's recommended semantics unless an actual exception is identified.
- Resolve technical wire details left unspecified: error extensions, request trace header, CSRF bootstrap route/header/cookie behavior, exact date/money examples, and the version precondition representation for future DELETE. Do not implement financial endpoints.
- Record frontend/backend package ownership and a short R0 endpoint/security matrix. Document planned finance routes without granting access to nonexistent R1 routes.
- Select supported exact patches for Angular/CLI, Node/npm, TS/RxJS, Java 25, Boot 4, Maven Wrapper, MySQL 8.4, test tooling, and image bases. Retain source candidate lines; verify actual distribution/tag availability and record sources. Use the Boot BOM rather than independent framework overrides. Lockfiles and resolved build/image evidence are completed by the dependent tickets.
- Record the repository location, naming, local start commands to be completed, and GitHub Actions/Docker prerequisites. Use defaults compatible with the baseline; only scope/architecture exceptions need owner decisions.

**Out of scope:** Application scaffolding, installing/running the project, full future endpoint implementation, chart-library selection, exhaustive API design for R2–R4, new product policies, or a dependency-upgrade automation platform.

**Technical constraints:** One repo; frontend/backend/infra/docs/adr; one Maven application; Java 25 without preview features; supported compatible versions; matching Angular/CLI; exact version records without invented image digests. Pin final resolved digests in R0-007/008. R0 is local-only synthetic data; security does not imply authentication.

**Dependencies:** None within R0. Implementation requires an identified target repository and approval of this ticket proposal.

**Acceptance criteria**

1. Given the supplied planning inputs, when the decision record is reviewed, then accepted choices, exceptions, source authority, and remaining verification work are distinguishable; all R0/R1-blocking semantics have a stated rule or named blocker.
2. Given the version manifest, when its entries are compared with official compatibility/release sources, then selected version families are compatible and candidate pins are not represented as successful builds.
3. Given an R0-only endpoint matrix, when future finance routes are examined, then they are documented as later scope and are not anonymously enabled merely for convenience.
4. Given ambiguous operational wire details, when their contract is recorded, then dependent tickets have deterministic expected formats/statuses and no unresolved technical choice is silently called approved.

**Testing expectations:** Documentation review against source requirements and official compatibility sources; verify distribution/tag existence. No application tests or code are expected in this ticket. Successful builds and exact image resolution belong to later tickets.

**Definition of Done:** Shared DoD plus decision/contract/version records are reviewable; repository/CI prerequisites and unresolved exceptions have owners; dependent work can start without assuming undocumented financial or access rules.

## R0-002 — Establish backend build, error, and logging foundation

**Type:** Task.  
**Objective and background:** Provide the smallest backend foundation required by R0, with repeatable builds and observable, consistent failures before financial workflows are added.  
**Complexity / estimate:** M, 3–5 active hours.

**Scope of work**

- Create the single Spring Boot/Maven application and Maven Wrapper with a verified distribution checksum. Configure Java 25 and Boot-managed dependencies.
- Establish business package ownership without empty repositories/controllers/interfaces for hypothetical features. Include only packages/configuration used at this stage.
- Establish standard controller error handling and RFC 9457 Problem Details extensions consistent with R0-001.
- Establish structured console logging and bounded request trace IDs usable in logs and problem responses. Keep request bodies, secrets, and user-entered descriptions out of routine logs.
- Document/test exact money/date serialization contracts through narrowly scoped fixtures/codecs only where necessary. Do not create complete future business DTO families or business validation workflows.
- Establish the focused backend unit/API test command. Full persistence configuration/startup verification arrives in R0-004.

**Out of scope:** CRUD/report controllers, business services, DB migrations, final security-filter behavior, health endpoints, audit logging, APM infrastructure, generated clients, and a generic shared-framework package.

**Technical constraints:** Spring MVC; explicit DTO/contract boundary; no JPA entity serialization; no float/double finance representation; application/problem+json with application code/fieldErrors/traceId; Boot 4-compatible starters/imports; no runtime API that exists solely to demonstrate an error.

**Dependencies:** R0-001. DB-backed application startup is not this ticket's completion claim; R0-004 supplies that evidence.

**Acceptance criteria**

1. Given the pinned Java/Maven setup, when the documented wrapper build/test command runs, then an actual compilable application artifact is produced and at least the applicable focused checks execute.
2. Given a test-only invalid request or controlled failure, when the error handler runs, then it produces the documented status/problem format and trace identifier without exposing stack traces, SQL, or request payloads.
3. Given contract fixtures containing large exact VND values and ISO dates, when serialized/deserialized as applicable, then money remains a decimal string and business dates do not acquire timezone offsets.
4. Given a traced request, when its outcome is examined, then the matching bounded identifier appears in its response and relevant logs without sensitive values.

**Testing expectations:** Focused JUnit/API serialization, exception mapping, and trace propagation checks. Test-only fixtures must not be shipped as production endpoints. No empty generated test or disabled DB integration check substitutes for actual behavior.

**Definition of Done:** Shared DoD plus real build/test evidence, error examples, package ownership, and trace/logging configuration documented; DB readiness is explicitly deferred to R0-004.

## R0-003 — Establish Angular shell and HTTP test foundation

**Type:** Task.  
**Objective and background:** Provide a runnable Angular shell with routing and an HTTP boundary, without prematurely building finance screens. This is enabling infrastructure despite being visible in a browser.  
**Complexity / estimate:** S, 3–5 active hours.

**Scope of work**

- Create the Angular/CLI project with the agreed version line and committed lockfile; document the pinned Node/npm toolchain and clean installation/build commands.
- Provide a common shell, a root page identifying the Personal Finance app as not yet offering finance workflows, and an unknown-route page with a working route back to the root. Exact copy/styling is a small implementation choice, not a new product feature.
- Establish a same-origin relative /api HTTP boundary and local development proxy configuration.
- Establish CLI Vitest and focused shell/routing/HTTP checks. Use existing Angular mechanisms rather than inventing a state/store abstraction.

**Out of scope:** Category/entry/history/report components, dashboard layouts, UI framework selection without need, loading widgets for nonexistent features, global stores, SSR/PWA, charts, authentication, automated browser/end-to-end tests.

**Technical constraints:** Standalone Angular app; supported zoneless setup; minimal styling; relative API URLs; no secrets in frontend configuration; no dependence on blanket detectChanges calls or one global loading flag.

**Dependencies:** R0-001. Proxy configuration can be reviewed before the real backend starts; end-to-end local proxy evidence belongs to R0-007/008.

**Acceptance criteria**

1. Given a clean frontend checkout and the pinned toolchain, when dependencies are installed from the lockfile and production build/tests run, then the actual Angular build and focused Vitest checks pass.
2. Given the application root, when loaded, then the common shell and foundation-only message are visible without finance navigation pretending to work.
3. Given an unknown client route, when opened, then the not-found view offers a working root link inside the common shell.
4. Given an HTTP request through the configured client, when inspected by a frontend test, then it uses the same-origin relative API boundary and does not embed an environment-specific host or secret.

**Testing expectations:** Vitest shell/root/unknown-route tests and a focused HttpClient boundary test. Record manual local shell navigation. Container deep-link/static-asset behavior is checked in R0-007/010.

**Definition of Done:** Shared DoD plus committed lockfile, documented build/test/proxy setup, real build evidence, and working shell routes. No placeholder user features are counted as delivered FEAT IDs.

## R0-004 — Establish MySQL schema, migrations, mappings, and integration tests

**Type:** Task.  
**Objective and background:** Make the approved schema real and prove that Flyway and Hibernate agree against actual MySQL. A clean schema foundation is necessary for R1 and R0's migration/mapping gate.  
**Complexity / estimate:** M, 4–7 active hours.

**Scope of work**

- Create reviewed initial migrations for categories and transactions as defined by the architecture, plus only required Flyway metadata. Include exact money/DATE types, stable IDs, enum-name representation, audit fields, optimistic-version fields, named constraints, and initial indexes.
- Add minimal persistence mappings sufficient for non-vacuous Hibernate validation; do not implement repositories/services solely for future CRUD workflows.
- Configure Flyway as the only migration mechanism, including MySQL support; validate applied migrations/mappings, disable schema auto-update/SQL auto-init and Flyway clean.
- Separate application CRUD credentials from Flyway schema-change credentials and database initialization administration. Establish the provisioning/configuration contract reused by Compose and integration tests; check required credentials early without exposing their values.
- Add actual MySQL Testcontainers integration tests using the same selected MySQL patch as local delivery. Keep container lifecycle and Spring test-context lifecycle aligned.
- State which validations are schema-enforced and which remain application responsibilities in R1, especially rejecting fractional entry input before DECIMAL storage, future dates, normalized names, and category/type compatibility.

**Out of scope:** Production finance APIs, record CRUD workflows, financial report queries, sample-category seeds, migration rollback framework, account/user/balance tables, category locking/version conflict workflows, or full R1 financial acceptance.

**Technical constraints:** MySQL 8.4/InnoDB/utf8mb4; DECIMAL(19,0); DATE transaction date; UTC audit timestamps; no cascade history deletion; explicit nullability; architecture's two-table model; no H2 evidence. Applied migrations are immutable. Use approved length/normalization conventions from R0-001.

**Dependencies:** R0-002 and its R0-001 decision/contract prerequisite. Docker-capable development environment is required. Full Compose packaging is later, not a circular dependency.

**Acceptance criteria**

1. Given an empty MySQL schema and correctly provisioned users, when the backend starts, then Flyway creates the agreed tables and Hibernate validates their actual mappings successfully.
2. Given the migrated schema, when startup repeats, then applied migrations are not rerun destructively and the schema remains valid.
3. Given an altered migration checksum or incompatible mapped schema in an isolated test, when validation runs, then startup/checks fail clearly rather than silently updating the schema.
4. Given limited runtime credentials, when ordinary test-fixture persistence is attempted, then approved CRUD access works; when schema DDL is attempted, then it is rejected. Migration credentials can perform the required migrations.
5. Given missing required credential settings, when the application is started, then configuration fails with actionable setting names and no secret values.
6. Given out-of-range/nonpositive amounts, invalid types, duplicate normalized names within a type, or nonexistent category references in isolated fixtures, when schema constraints are exercised, then invalid rows are rejected according to the schema contract. Application-only validations are not falsely attributed to DECIMAL/FK enforcement.

**Testing expectations:** Real MySQL empty-schema, repeat-start, mapping/checksum failure, constraint, and privilege integration checks; focused exact-value/date persistence round trips. Test fixture data is isolated and synthetic. No claim of financial correctness before R1 workflows exist.

**Definition of Done:** Shared DoD plus passing real-MySQL integration evidence, reviewed migrations/mappings, separate credential configuration, and documented schema/application validation responsibilities. Tables contain no seeded user categories in normal startup.

## R0-005 — Establish local security and SPA CSRF bootstrap

**Type:** Task.  
**Objective and background:** Establish the agreed anonymous local access envelope with working Angular/Spring CSRF integration before feature mutations are implemented. Local operation is not a reason to permit every endpoint or remove CSRF protection.  
**Complexity / estimate:** M, 4–7 active hours.

**Scope of work**

- Implement the documented R0 method/path allowlist and deny unlisted paths. Enable only the CSRF bootstrap surface and the health probes required by R0-006; future finance routes remain unavailable.
- Implement the agreed token bootstrap through the real Angular HTTP boundary, including supported deferred-token/SPA behavior and error handling.
- Keep default security headers; disable unintended generated development users, login form, HTTP Basic, and authentication entrypoints inconsistent with anonymous local scope.
- Keep CORS closed by default, define allowed local Host/Origin behavior, and share the edge-enforcement contract with R0-007.
- Ensure security-filter failures use the same Problem Details/code/trace contract as controller failures.
- Exercise unsafe-method CSRF behavior with a test-only nonfinancial handler. Do not ship a demonstration mutation endpoint.

**Out of scope:** Login, JWT/OAuth, roles, multi-user authorization, business mutation endpoints, financial validation/conflict tests, public TLS/hosting, and a complete future error UX framework.

**Technical constraints:** Supported Boot/Security SPA CSRF integration; relative same-origin URLs; explicit method/path permissions; no wildcard CORS or broad permitAll; no token/credential logging. CSRF is not authentication and does not restrict other local processes.

**Dependencies:** R0-003 and R0-004. Error/trace foundation is inherited through R0-002.

**Acceptance criteria**

1. Given the app on the agreed local origin, when Angular requests token bootstrap, then it obtains the supported CSRF state using the documented cookie/header contract without login or secret-bearing frontend config.
2. Given an isolated allowed test-only unsafe handler, when a request lacks or uses an invalid token, then it is rejected with the documented 403 problem response; with a valid token and allowed origin, the test handler is reachable.
3. Given an unlisted path or disallowed method, when accessed, then the request is denied according to the endpoint matrix rather than redirected to a login page or served a fake business result.
4. Given a bootstrap failure, when Angular handles it, then there is no false readiness/success and no automatic POST retry; the failure remains observable through the agreed HTTP boundary.
5. Given requests from an external origin, when unsafe-request security is exercised, then they cannot mutate the isolated handler and no permissive CORS policy is returned.
6. Given a built runtime artifact, when routes/configuration are inspected, then the test-only mutation handler is absent and no generated development credentials are exposed.

**Testing expectations:** Backend security-filter/API tests for allow/deny, CSRF, problem responses, and headers; frontend HttpClient tests for bootstrap success/failure and XSRF behavior. Actual browser/proxy smoke and Host behavior are verified manually in R0-010. Mutation-specific finance tests remain in R1.

**Definition of Done:** Shared DoD plus explicit security matrix, passing backend/frontend bootstrap checks, documented failure/token handling, and confirmed absence of test-only runtime endpoints/default authentication.

## R0-006 — Establish readiness and liveness behavior

**Type:** Task.  
**Objective and background:** Let local delivery distinguish application process health from database readiness and fail transparently during dependency outages.  
**Complexity / estimate:** S, 2–4 active hours.

**Scope of work**

- Configure minimal Actuator liveness and readiness probes. Include DB availability in readiness; keep it out of liveness.
- Keep health details minimal and operational access restricted by R0-005's policy. Expose only the operational paths required by container probes.
- Document/check status codes and ensure normal logs make startup/dependency failures diagnosable without a monitoring platform or secret output.

**Out of scope:** Prometheus/Grafana, tracing collectors, alerting, dashboards, additional Actuator exposure, an HTTP health substitute that always returns success, or custom polling infrastructure.

**Technical constraints:** Actual DB readiness evidence; independent liveness; bounded failure checks so an outage does not hang indefinitely; no DB credentials/details in public responses. Health is not routed through the frontend SPA proxy by default.

**Dependencies:** R0-005; real DB configuration is inherited from R0-004.

**Acceptance criteria**

1. Given a running backend and available DB, when readiness/liveness are checked, then both report the agreed healthy status.
2. Given a temporary DB outage after successful startup, when probes are checked, then readiness reports unavailable within the documented probe timeout while liveness remains healthy if the application itself is healthy.
3. Given the DB is restored, when checks resume, then readiness recovers without falsely declaring a persistent outage.
4. Given a non-allowed operational path or a health request asking for internal details, when accessed, then restricted endpoints stay denied and only the configured minimal health response is returned.

**Testing expectations:** Focused health-policy tests and a real-MySQL availability/outage/recovery integration check or recorded controlled local check. Use eventual assertions/recorded bounded waits where needed, not hardcoded sleep assumptions. Compose startup gating is R0-008's responsibility.

**Definition of Done:** Shared DoD plus documented probe URLs/statuses/timeouts, outage/recovery evidence, and restricted operational exposure.

## R0-007 — Build non-root application images and same-origin proxy

**Type:** Task.  
**Objective and background:** Package the verified foundations into maintainable runtime images and provide the static/API routing boundary needed by the SPA.  
**Complexity / estimate:** M, 3–5 active hours.

**Scope of work**

- Create multi-stage backend/frontend image builds with appropriate build-context exclusions. Record reviewed patch-specific base images and resolved immutable digests for the target platform.
- Run the backend as a dedicated non-root identity with readable runtime artifact and Java 25. Use reviewed unprivileged Nginx for the frontend on a non-privileged container port; omit Node/build tooling from the frontend runtime.
- Serve navigation through SPA fallback, serve real static files with correct behavior, and proxy the API prefix to the backend service on the same origin.
- Enforce the documented allowed Host/Origin boundary at the appropriate proxy/application layer. Forward necessary CSRF cookies/headers and request trace metadata without exposing operational endpoints.
- Provide actual container health checks for frontend serving and backend readiness, using tools available in the final images.

**Out of scope:** Compose assembly, publishing images to a registry, public deployment, custom domains/TLS, performance tuning, chart assets, and OS-hardening frameworks beyond the approved non-root/pinning boundary.

**Technical constraints:** No latest/floating-only image pins; no invented digest; no secrets in image layers/build contexts; preserve same-origin contract. Non-root frontend/backend is the requirement, not an assumption that every process inside the vendor MySQL image shares those identities.

**Dependencies:** R0-006 and transitive frontend/backend/security prerequisites. Standalone image tests may use an isolated DB/backend test network; they do not require completed Compose work.

**Acceptance criteria**

1. Given the pinned sources/toolchains, when both images are built, then they contain the actual application builds and their resolved base image references are recorded.
2. Given the running application containers, when their process identity and artifact access are checked, then frontend/backend run without root and startup succeeds without permission errors.
3. Given root or a client navigation URL, when requested through the frontend, then Angular is served and can render its appropriate route; given a missing JS/CSS/static asset, then a real error is returned instead of index.html.
4. Given an API request including an unavailable/missing route, when it passes through the proxy, then it reaches backend policy/error handling and returns its actual status/problem response rather than Angular HTML. A denied unknown API route may correctly return 403, not necessarily 404.
5. Given token bootstrap on the local browser origin, when proxied, then the CSRF contract still works. Operational URLs are not exposed through an accidental API/SPA proxy path.
6. Given allowed and disallowed Host/Origin requests, when the documented boundary is exercised, then intended local requests work and disallowed access/mutation attempts fail as specified.
7. Given the runtime image health commands, when invoked, then frontend serving and backend readiness checks execute successfully using installed runtime tools.

**Testing expectations:** Real image builds, process-identity/health checks, HTTP routing/static/proxy checks, and a manual browser bootstrap/navigation check. No automated browser suite; proxy assertions may use HTTP tools and focused backend tests.

**Definition of Done:** Shared DoD plus reviewed Dockerfiles/context exclusions, recorded image references, actual non-root/proxy/health evidence, and no registry publish/deployment.

## R0-008 — Assemble the private three-service Compose environment

**Type:** Task.  
**Objective and background:** Make the R0 foundations reproducibly runnable together from repository-root commands while preserving the local network and credential boundary.  
**Complexity / estimate:** M, 3–5 active hours.

**Scope of work**

- Assemble frontend, backend, and MySQL under infra/, using R0-007 images and the exact MySQL patch used by R0-004 tests; resolve and record the reviewed MySQL image digest.
- Use container service names for DB/API communication. In full-stack mode, publish only frontend to loopback; keep backend/MySQL internal.
- Provision separate runtime/migration/admin DB identities using the established DB configuration contract, keep secrets outside git/images, and retain useful example setting names with synthetic placeholders.
- Store MySQL state in a named volume. Gate backend startup on MySQL service_healthy, and expose meaningful container health results.
- Document normal start/stop/status/log commands and restart behavior as the Compose changes are made; the consolidated clean-checkout runbook belongs to R0-010.

**Out of scope:** Remote staging/production, Kubernetes, registry publishing, additional services, real-data seeding, automatic destructive reset, and optional host-running development overrides unless needed to validate R0 locally.

**Technical constraints:** Only frontend is host-exposed in full Compose mode; localhost-only browser entry; internal service DNS; same MySQL patch across tests/delivery; no credential defaults masquerading as production configuration; no destructive volume deletion in normal start/stop instructions. Startup gating does not replace outage behavior from R0-006.

**Dependencies:** R0-007, including its DB/health prerequisites. Docker Engine/Desktop and Compose must be available.

**Acceptance criteria**

1. Given a clean isolated environment and correctly supplied credentials, when the documented root command starts the stack, then all three real services become healthy and the frontend is usable on the loopback origin.
2. Given a delayed/not-ready MySQL service during startup, when Compose is observed, then backend startup waits for successful DB health rather than racing an unavailable database.
3. Given the resolved network/port configuration, when inspected and checked from the host, then only frontend has a loopback-bound published port; no backend/MySQL host port is opened by full-stack configuration.
4. Given a normal stop/start of the existing stack, when DB state is inspected, then the schema/Flyway history remains present and migrations are not destructively reapplied. Full financial-record persistence evidence follows in R1.
5. Given missing required settings, when startup/config validation is attempted, then it fails clearly without exposing credential values or silently using unintended defaults.
6. Given a running stack that later loses MySQL, when health/status is checked, then backend readiness becomes unavailable while its own liveness remains independent as designed; startup depends_on is not claimed to solve later outages.

**Testing expectations:** Validate Compose configuration without publishing secrets; real clean-volume startup, delayed DB, normal restart/schema persistence, port/network checks, and missing-configuration failure. No finance entries or user-facing workflows are needed.

**Definition of Done:** Shared DoD plus real healthy-stack evidence, root commands, exact MySQL reference, private ports/credentials, and normal restart behavior demonstrated.

## R0-009 — Establish meaningful build, test, and image CI

**Type:** Task.  
**Objective and background:** Verify actual builds and the existing focused tests on a Docker-capable runner so the first product release inherits trustworthy delivery checks.  
**Complexity / estimate:** M, 3–5 active hours.

**Scope of work**

- Add GitHub Actions checks for frontend clean lockfile install/build/Vitest, backend wrapper build/unit/API/MySQL integration tests, and both actual application image builds.
- Match the recorded Node/npm/Java/MySQL lines and reviewed dependency/image pins. Use appropriately pinned workflow actions and minimum required workflow permissions.
- Supply synthetic isolated DB credentials to integration tests. Prevent untrusted pull requests from receiving retained secrets; do not require real financial data or remote DB access.
- Keep job failures/logs useful, fail on applicable test/build/image errors, and document the exact local equivalents. Cache only where it helps the real builds; no cache service is introduced.

**Out of scope:** Automated browser testing, remote deployment, registry publish, branch ruleset administration, code-coverage thresholds, release automation, and a separate DevSecOps tooling platform.

**Technical constraints:** Docker-capable runner; actual MySQL Testcontainers evidence, not H2/no-op tests; actual artifact/image compilation; no conditional skipping presented as a green build; no credential-bearing diagnostic output. GitHub Actions usage is not permission to deploy.

**Dependencies:** R0-007 and its transitive build/test prerequisites. This ticket can be reviewed independently of R0-008's Compose assembly. An accessible GitHub repository with Actions/Docker runner capability is an external prerequisite.

**Acceptance criteria**

1. Given a proposed change on the target GitHub repository, when CI runs, then it installs from the frontend lockfile, executes focused frontend/backend checks including real MySQL integration, and builds both application images.
2. Given an intentionally failing applicable test/build in a temporary review branch, when CI executes, then the relevant job fails; after the change is removed, the actual checks pass. No failing probe is retained in the final branch.
3. Given a clean runner, when integration tests execute, then they provision isolated synthetic DB state and do not depend on a developer's local database or hidden files.
4. Given workflow permissions and log output, when reviewed, then there is no deployment/publish action, unnecessary write scope, secret leakage, or financial data requirement.

**Testing expectations:** One real passing pipeline plus evidence of failure propagation from a temporary non-destructive probe; inspect actual test/build/image logs and compare toolchain versions with the manifest. Local validation alone does not establish GitHub CI success.

**Definition of Done:** Shared DoD plus successful run link and applicable job evidence, local command equivalents, reviewed permissions, and documented external runner/repository prerequisites. If Actions is unavailable, this ticket remains blocked rather than claiming green CI.

## R0-010 — Complete runbook and verify R0 release readiness

**Type:** Task.  
**Objective and background:** Demonstrate that the foundation works as one release from a clean checkout and make its daily local operation repeatable. This is a cross-ticket acceptance gate, not a repeat implementation ticket.  
**Complexity / estimate:** M, 3–5 active hours.

**Scope of work**

- Consolidate the developer runbook: prerequisites, exact versions, credential setting names, root-level install/build/test/start/stop/status commands, local URLs, DB lifecycle, and troubleshooting for configuration/health/proxy failures.
- Document logical MySQL backup/restore commands with backup output outside the DB volume and out of git. Label destructive reset operations explicitly and isolate them from normal operation; do not add scheduled backup infrastructure.
- Execute a clean-checkout full-stack smoke: healthy startup, root and unknown client route, missing asset, actual API/security error, browser CSRF bootstrap, non-root processes, port exposure, DB outage/recovery, and normal restart persistence.
- Perform a basic isolated schema/Flyway-history backup/restore smoke or validate those commands against the R0 DB. It proves the R0 runbook command path only; the full nonempty-record and financial-total restoration acceptance remains R1.
- Record R0 criteria evidence, known limitations, and readiness for R1. Update observed R0 effort for later estimation without generating R1 tickets.

**Out of scope:** Reimplementing passed tickets, new foundation architecture, financial workflows, financial-report restore assertions before those features exist, real-data backups, remote hosting, automatic backups, and roadmap/release commitments beyond R0.

**Technical constraints:** Manual browser checks; isolated synthetic test environment; no secrets in copied commands/evidence; normal stop/start preserves named volume. The final runbook must not describe future functions as already implemented. A volume is not a backup.

**Dependencies:** R0-008 and R0-009. All earlier relevant DoD items must be complete; source/contract exceptions blocking R1 must be resolved or clearly prevent release acceptance.

**Acceptance criteria**

1. Given only the repository, recorded prerequisites, and locally provided credentials, when the runbook is followed from a clean checkout, then the actual healthy three-service foundation runs without undocumented source edits.
2. Given the running stack, when the manual smoke checklist is executed, then every R0 criterion has observed passing evidence or an explicit unresolved blocker; no financial feature is claimed complete.
3. Given the documented backup path, when a logical backup is restored into a fresh isolated DB, then the R0 schema/Flyway state is recovered; no claim is made that unimplemented financial reports were verified.
4. Given normal restart and a temporary DB outage/recovery, when documented commands are followed, then persistent schema, readiness/liveness behavior, and restricted exposure match the agreed baseline.
5. Given the final evidence record, when R0 readiness is reviewed, then it includes the green CI link, actual versions/image references, manual check outcomes, unresolved limitations, and a clear statement that Playwright, finance features, and public deployment were not part of R0.

**Testing expectations:** Consolidated manual browser/HTTP/container smoke and isolated basic restore check. Reuse already recorded focused test evidence; do not duplicate entire test suites without a new failure or concern. Re-run applicable checks after any final fixes.

**Definition of Done:** Shared DoD plus complete clean-checkout runbook, R0 acceptance evidence, basic backup/restore command verification, actual CI proof, and no unresolved issue invalidating R0 or its approved entry conditions for R1.

## Release acceptance coverage

| Roadmap R0 criterion | Main tickets | Expected evidence |
| --- | --- | --- |
| 1. Clean checkout, pins, actual builds/MySQL CI | 001, 002, 003, 004, 008, 009, 010 | Manifest, builds, real MySQL tests, CI link, clean-stack smoke |
| 2. Empty-schema migrations and mapping validation | 004, 008 | Flyway/actual entity mapping checks and schema persistence |
| 3. Same-origin SPA/API/static routing | 003, 005, 007, 010 | Browser shell/bootstrap and real navigation/static/API statuses |
| 4. Non-root, loopback edge, private DB/backend, credentials | 004, 007, 008, 010 | Privilege checks, process identity, ports/network, configuration behavior |
| 5. Healthy DB startup and correct operational probes | 006, 008, 010 | Startup gating, outage/recovery, restricted health details |
| 6. Security/CSRF/problems/money/date contracts exercised | 001, 002, 005, 007, 010 | Documented contracts, focused fixture/security tests, proxy/bootstrap smoke |
| 7. Decisions/versions and R1 semantics settled | 001, 004, 010 | Accepted decision record, reproducible pins, named exceptions/blockers |
| Quality: Vitest, Testcontainers, trace logging, backup/runbook | 002, 003, 004, 009, 010 | Focused actual checks, trace examples, documented verified commands |

## Unanswered questions and blockers

No target repository or implementation has been supplied/inspected. Therefore estimates assume greenfield work; existing code could change both scope and effort. This proposal does not fabricate repository paths, access, branch state, installed tools, pipeline success, or image digests.

| Item | Classification | Resolution owner/ticket | What it blocks |
| --- | --- | --- | --- |
| Which repository/path holds this project, and is there existing code? | Missing implementation context | Developer; record in R0-001 and inspect at implementation start | Repository mutations; reliable reuse/effort assessment |
| Are Actions and a Docker-capable runner available? | External environment prerequisite | Developer; R0-001/R0-009 | Actual MySQL CI/image evidence |
| Exact framework patches and available container digests | Technical readiness work, not a new product approval | R0-001, finalized by 002–004/007/008 | Reproducible builds and image acceptance |
| CSRF bootstrap/error/trace/delete-version wire details and Unicode length counting | Open technical specification, within approved semantics | R0-001 | Dependent security/contract work; R1 feature contracts |
| Any exception to the baseline's recommended money/date/category/deletion/local-data choices? | Scope/architecture exception only if one is actually intended | Product Owner/developer; record in R0-001 | Only the affected work; never silently guess a changed requirement |
| Initial schema/mappings versus Flyway-only bootstrap | Explicit R0 implementation-scope interpretation in this proposal | Approve/revise R0-004 as part of ticket review | Database ticket boundary; no finance workflow is included either way |
| Roadmap estimate 16–24 h versus detailed 31–53 h | Estimate discrepancy, not hidden scope | Review this proposal; re-estimate with observed work | Effort expectation, not dependency ordering |

The provided approval statement is used as authority for the source baselines; their unchanged Proposed headers are an administrative inconsistency, not a reason to repeatedly request approval. New contradictory information or a named exception must be recorded before the affected implementation. Approval of this ticket proposal does not authorize code execution or deployment in this conversation; the user requested planning only.

## Compatibility verification and references

Official documentation was rechecked on 2026-10-09. Angular's current matrix lists Angular 22.0.x with Node ^24.15.0, TypeScript >=6.0.0 <6.1.0, and a supported RxJS 7 range. Spring Boot's requirements list Java compatibility including Java 25. Boot's initialization guide documents the MySQL Flyway module, and Spring Security documents SPA-specific CSRF handling. These checks support the planning constraints; they do not prove the absent repository builds or pin all versions.

- https://angular.dev/reference/versions
- https://docs.spring.io/spring-boot/system-requirements.html
- https://docs.spring.io/spring-boot/how-to/data-initialization.html
- https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html

## Approval boundary

Review the ten ticket boundaries, their dependency order, the explicit initial-schema interpretation, and the revised effort range. On approval, their status may be recorded as Approved for implementation, with named exceptions. Until then they remain Proposed. No implementation, code, release beyond R0, or public deployment is initiated by this document.
