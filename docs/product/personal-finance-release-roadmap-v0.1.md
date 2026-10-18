# Personal Finance Management App — Incremental Release Roadmap v0.1

Status: Proposed — for Product Owner review and approval  
Prepared: 2026-10-09  
Capacity: One developer, approximately 10–20 hours per week  
Delivery model: Incremental releases, no fixed sprint or calendar commitments  
No development tickets or application code are included.

## Inputs and approval context

Planning inputs:
- personal-finance-feature-backlog-v0.1.md, FEAT-001–008 as MVP scope; FEAT-009 explicitly deferred.
- personal-finance-architecture-baseline-v0.1(1).md, including its confirmed constraint that Playwright/automated browser end-to-end testing is not required.

Both attached documents retain Proposed status headers. The user's current instruction identifies them as the baseline for release planning. This roadmap uses that planning authority without rewriting source status headers or inventing an ADR approval date. Record accepted architecture decisions and any exceptions before implementation in R0. Roadmap approval does not implicitly include FEAT-009, real-data usage, remote deployment, or a different operating envelope.

## Strategy and assumptions

Deliver a usable monthly tracker in R1, then complete organization/analysis, cumulative balance, and trends. R0 is an internal readiness milestone, not a user-facing product release. R4 completes the agreed MVP; R1 is an early usable subset.

Proposed delivery slices:
- R1 supports user-created categories from first use; there is no dependency on a predefined category seed list.
- Category creation/selection precedes income/expense entry inside R1.
- Category rename/archive, additional history filters, and category analysis follow in R2.
- Later reports reuse the server-side transaction read/aggregate boundary; no cached financial totals or extra runtime infrastructure is introduced.

Estimate assumptions:
- Greenfield project; no functioning repository implementation has been supplied or inspected.
- Developer has 5+ years of web experience but is learning parts of the chosen stack.
- Estimates are active working hours, not elapsed duration. They include relevant design/API work, implementation, focused unit/integration tests, manual browser acceptance, documentation, and ordinary debugging/learning allowance.
- Simple desktop-first UI; no custom design system, broad mobile optimization, automated browser test suite, public hosting, or login.
- Architecture recommendations are accepted or exceptions are resolved before the relevant release. Major changes to money/date/category/access semantics require re-estimation.
- Exact framework patches and image digests are reverified at implementation readiness; candidate versions in the architecture file are not treated as proven build inputs.
- Docker-capable development and CI environments are available.
- The ranges are preliminary PM estimates, not measured velocity or technical commitments. No extra contingency is added to the totals below.
- Re-estimate after R0 and R1 using actual working hours and defects.

## Roadmap overview

| Release | Objective and delivered value | Included feature scope | Dependencies | Main deliverables | Estimated effort / uncertainty |
| --- | --- | --- | --- | --- | --- |
| R0 — Local delivery foundation | Make the approved design runnable and verifiable; remove setup uncertainty. Internal readiness only. | No new FEAT IDs; enables all product features. | Source scope/architecture decisions recorded; supported-compatible versions and Docker availability verified. | Application shell and backend skeleton; schema/migration foundation; three-service Compose; same-origin proxy; security/error conventions; health/logging; meaningful build/test/image CI; developer runbook. | 16–24 hours; High uncertainty, mainly runtime/image compatibility, security bootstrap, and unfamiliar tooling. |
| R1 — Usable monthly tracker | Record and correct everyday activity, then see the selected month's income, expenses, and remaining amount. First usable app. | FEAT-001 create/list/select categories (partial); FEAT-002/003 income/expense entry; FEAT-004 correction core; FEAT-005 paginated history + month filter (partial); FEAT-006 monthly totals (partial). | R0; categories available before entry; exact money/date/API rules settled. | Category first-use flow; entry/edit/delete forms; monthly history and totals; validation/problem/conflict UX; live server calculation; backup/restore evidence with records. | 56–88 hours; High uncertainty, mainly complete frontend/backend workflows, exact-money handling, corrections, CSRF, and error states. |
| R2 — Organize and explain spending | Make the tracker maintainable as categories and history grow; reveal where money goes. | Complete FEAT-001 with rename/archive; complete FEAT-005 with type/category filters; complete FEAT-006 with category breakdown. | R1; stable category IDs, normalized names, and archive/history rules; transactional category assignment coordination. | Category-management screens; combined filters; readable category spending breakdown; historical-category preservation; race/filter regression evidence. | 28–44 hours; Medium–High uncertainty, mainly archived-category corrections, archive/write race, normalization, and filter interactions. |
| R3 — Cumulative balance | Show net recorded money across months without confusing it with one month's remainder. | FEAT-007; extend FEAT-004 consistency checks to cumulative reporting. | R1 transaction/report foundations; proposed order after R2 for shared conventions. Not technically dependent on full category management. | As-of date selector; cumulative balance API/view; distinction from monthly totals; boundary and backdated-correction tests. | 16–24 hours; Medium uncertainty, mainly inclusive date boundaries, exact aggregate values, and keeping history filters independent. |
| R4 — Spending trends and MVP completion | See spending patterns across months; complete and verify the full approved MVP. | FEAT-008; final cross-feature verification of FEAT-001–008. | R1 expense data/report foundation; proposed order after R3. FEAT-007 is not a technical prerequisite for the chart. | Monthly trend chart with exact readable totals/table; bounded date-range navigation; empty/partial-month presentation; complete MVP acceptance record and restore/regression evidence. | 20–32 hours; Medium uncertainty, mainly chart compatibility/accessibility, range handling, and final integration defects. |

## R0 — Local delivery foundation

Objective: Establish only the foundation needed to build the first product workflow. Do not expand this into an infrastructure showcase.

Acceptance criteria:
1. From a clean checkout, documented commands start frontend, backend, and MySQL. Reviewed versions are pinned; an actual frontend/backend build and MySQL integration check pass in CI.
2. Database migrations run against an empty MySQL schema and mappings validate. The schema is managed by one migration mechanism; there is no automatic update of production-style schema.
3. The frontend serves navigation routes and proxies API requests on the same origin. Missing static files and API routes return their real errors, not the Angular HTML page.
4. Frontend/backend run as non-root; only the frontend is host-exposed on loopback in full Compose mode; MySQL/backend remain internal. Runtime database credentials are separate from migration credentials, and secrets are outside git/images.
5. Backend startup waits for healthy MySQL. Readiness reflects database availability; liveness is independent of a temporary database outage. Operational endpoint exposure is restricted.
6. An explicit endpoint/security policy, CSRF bootstrap approach, standard problem response format, and money/date contracts are documented and exercised at this stage; mutation-specific tests follow when mutations exist.
7. Approved architecture choices/exceptions and compatible version manifest are recorded. Before R1, resolve future-date policy, reporting calendar, amount/description limits, category/type semantics, deletion/version rules, and initial no-login sample-data envelope.

Quality/infrastructure: Reproducible builds; Docker-capable CI; MySQL Testcontainers setup; Angular Vitest; basic traceable logging; backup/restore commands documented. No no-op build checks or invented financial workflow evidence.

## R1 — Usable monthly tracker

Objective: A user can complete one entire recording/review/correction workflow without waiting for later reports.

Acceptance criteria:
1. Starting from no categories, the user can create matching income/expense categories, then save multiple income and expense entries in whole VND. Expense purchase descriptions are required; income descriptions are optional.
2. Invalid/fractional/nonpositive amounts, invalid category/type assignments, and invalid dates are rejected by the backend; frontend errors are actionable. Past-date entry works, and future-date handling follows the recorded architecture decision.
3. The combined history uses stable pagination/order and a month selector. Monthly totals cover the entire selected month, not just the displayed page.
4. January income 10,000,000 VND and expenses 7,000,000 VND produce 3,000,000 VND remaining. February income 5,000,000 VND and expenses 6,000,000 VND produce −1,000,000 VND remaining, visibly negative.
5. Editing an entry's amount/date/category or supported type, and confirmed deletion, update visible affected monthly totals after successful commit. Moving an entry across months updates both periods. Cancelled deletion changes nothing.
6. A stale edit/delete is rejected without overwriting newer data. Repeated submission is disabled while pending; failed saves show no false success. An uncertain timeout instructs the user to check history before retrying rather than silently retrying creation.
7. Entries survive a normal container restart. A logical backup stored outside the DB volume can be restored into a fresh database and reproduces the fixture's entries and monthly totals.

Quality/infrastructure: Unit tests for money/date rules; MySQL integration tests for constraints, migration, CRUD, aggregates, atomic writes, and optimistic conflicts; API/CSRF/problem-response checks; Angular form/loading/error/conflict tests; recorded manual browser acceptance. Implement core concurrency semantics now rather than deferring them to polish.

Feature completeness note: FEAT-001/005/006 are deliberately partial. FEAT-004 core is delivered here; its consistency obligation is verified again for each later report.

## R2 — Organize and explain spending

Objective: Give the user useful categorization and reliable ways to inspect growing history.

Acceptance criteria:
1. Renaming a category updates the displayed current name in existing history/reports without changing transaction amounts, references, or totals.
2. Archived categories cannot be assigned to new transactions or category changes; existing entries remain visible and may retain their archived category during allowed corrections.
3. Archived categories remain available in history filters/report labels. Type stays immutable; duplicate normalized names within a type are rejected, including reserved archived names, following the architecture contract.
4. Month/type/category filters work individually and together with stable pagination. Changing filters cannot display a stale response from a prior selection. History filters do not redefine the whole-month overview.
5. Expense category totals sum exactly to the selected month's expense total. Categories with historical records remain included after archiving.
6. A concurrent archive/new-assignment test demonstrates the agreed transactional ordering; no invalid assignment is committed. R1 acceptance remains passing.

Quality/infrastructure: Category locking/optimistic conflicts and normalization integration tests; archived-entry correction tests; UI category-selection/filter regression; query/projection boundary review. No new runtime services.

## R3 — Cumulative balance

Objective: Provide the explicitly approved running-balance outcome.

Acceptance criteria:
1. Running balance starts at zero and includes every recorded income/expense through the selected date, inclusive, independent of history filters or pagination.
2. For the R1 fixture, balance through January is 3,000,000 VND; through February it is 2,000,000 VND, while February's monthly remaining amount is −1,000,000 VND.
3. Adding a forgotten January expense of 1,000,000 VND changes January's remainder to 2,000,000 VND and the cumulative balance through February to 1,000,000 VND; February's own remainder stays −1,000,000 VND.
4. Backdated edits/deletions, month/year boundaries, and leap dates produce exact values. Same-report fields use a coherent database snapshot; separate HTTP requests are not presented as one shared snapshot.
5. UI wording clearly separates recorded cumulative balance from monthly remainder and verified cash/bank holdings. R1/R2 behavior remains passing.

Quality/infrastructure: MySQL inclusive-date/exact-aggregate and report snapshot tests; negative/empty/large-total formatting tests; manual history-filter independence check. No persisted balance table or cache.

## R4 — Spending trends and MVP completion

Objective: Complete monthly trend understanding and the entire agreed MVP.

Acceptance criteria:
1. Chart and readable totals/table use authoritative monthly expense aggregates. Each month's total agrees exactly with its monthly overview; displayed values remain exact even if chart geometry uses approximate numeric coordinates.
2. The agreed default last-12-month window and bounded navigation (up to 120 months per request under the supplied architecture proposal) allow inspection across recorded history. Invalid ranges are rejected.
3. Months without recorded expenses show zero recorded expenses; labels do not imply proven spending completeness. The current month is identified as partial.
4. Past-date creates, edits, deletions, and date moves update all affected chart months and other visible financial results after successful commit.
5. Manual browser acceptance covers category creation, income/expense entry, correction, monthly/cumulative/trend review, rename/archive with retained history, and delete confirm/cancel; all FEAT-001–008 are accounted for.
6. CI is green for the implemented unit/integration/API/frontend/image checks; documented full-stack Compose smoke and backup/restore regression pass. No unresolved defect blocks entry, corrupts totals, loses data, or breaks the defined local security envelope.

Quality/infrastructure: Focused chart/form/API tests and manual browser evidence; reviewed accessible chart dependency plus nonvisual/readable totals; build/run/restore instructions refreshed. Automated browser testing, remote hosting, and a new observability platform remain outside scope.

## Common release gates

Every user-facing release must:
- Demonstrate its specified behavior using sample data and record expected/observed results.
- Pass relevant automated unit/integration/API tests and manual browser checks; no arbitrary global coverage threshold.
- Preserve exact money values and authoritative server calculations; query reports from current committed data.
- Maintain working loading, empty, validation, unavailable-service, and applicable conflict states.
- Keep module ownership/API contracts/migrations aligned and preserve already delivered behavior.
- Maintain the local-only synthetic-data envelope, same-origin security, private DB, non-root app runtimes, and no-secret logging.
- Produce a reproducible local version with updated run instructions and meaningful CI evidence.
- Stop treating a named volume as a backup; retain separate backup/restore evidence.
- Avoid treating GitHub Actions CI/image builds as authorization for public deployment.

## Feature coverage

| Feature | First delivered | Complete |
| --- | --- | --- |
| FEAT-001 Categories | R1 create/list/select | R2 rename/archive/history preservation |
| FEAT-002 Income | R1 | R1 |
| FEAT-003 Expenses | R1 | R1 |
| FEAT-004 Corrections | R1 core | R1 core; report consistency verified through R4 |
| FEAT-005 History | R1 history/month/pagination | R2 type/category filters |
| FEAT-006 Monthly overview | R1 totals | R2 category breakdown |
| FEAT-007 Running balance | R3 | R3 |
| FEAT-008 Trend | R4 | R4 |
| FEAT-009 Budget | Not scheduled | Requires separate scope approval |

## Effort and calendar interpretation

| Milestone | Cumulative active effort |
| --- | --- |
| First usable monthly tracker, R0 + R1 | 72–112 hours |
| Complete MVP, R0–R4 | 136–212 hours |

At a steady 20 hours/week, the full MVP is approximately 7–11 weeks. At a steady 10 hours/week, approximately 14–22 weeks. The first usable release is approximately 4–6 weeks at 20 hours/week or 8–12 weeks at 10 hours/week. These are rounded arithmetic scenarios, not deadlines; interruptions, major changes, and prolonged learning are not predictable from capacity alone.

No release date is promised. Sequence work by dependencies and acceptance, review the next release after each completion, and update ranges from observed delivery effort. A later technical review may alter these PM estimates.

## Post-MVP and approval request

After R4, review actual use before choosing another release. FEAT-009 monthly budget remains a candidate because controlling spending against a desired ceiling is a different goal from tracking remaining income. Do not assign it a delivery commitment now.

Login/real-data access/remote hosting, multiple accounts, recurring entries, imports, bank integration, refunds, borrowing, and credit-card workflows are not added by this roadmap.

Review and approve or revise: the R0–R4 boundaries, deliberate partial-feature delivery, acceptance gates, and preliminary estimates. No individual development tickets are created by this document.

