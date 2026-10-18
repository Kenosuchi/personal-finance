# Personal Finance Management App — Feature Backlog v0.1

Status: Proposed — awaiting Product Owner approval  
Date: 2026-10-09 (Asia/Saigon)  
Source: Approved product baseline v1.0, approved 2026-10-08.

This backlog translates the approved scope into product capabilities. Its feature grouping, priorities, dependencies, and complexity ratings are recommendations awaiting approval. The approved baseline remains unchanged. No development tickets or release commitments are established here.

## Planning interpretation and assumptions

- MVP means the complete approved MVP, not the previously proposed R1. The R1 boundary has not been approved.
- Must Have means necessary to fulfill the approved MVP. Should Have means valuable but can follow it. Could Have means optional pending evidence of need. No Could Have feature is added merely to fill a priority band.
- Complexity is a relative product-scope assessment for one developer, not an effort estimate or commitment. Medium includes interactions with dates, categories, historical corrections, or reporting.
- Dependencies refer to required product capabilities, not technical architecture or an inflexible development sequence. Dependency IDs are FEAT IDs.
- Categories must be selectable when recording transactions. Whether predefined categories are available before full category management is an unapproved delivery assumption.
- The chart visualizes recorded monthly expense totals; it does not establish that all transactions for a month have been entered.
- Display layout, future-date policy, reporting calendar/timezone, validation limits, historical category naming, and empty/partial-month chart behavior remain unresolved.

## Feature backlog

| Feature ID | Feature name | User problem being solved | Business value | High-level description | Main user interactions | Priority | Dependencies | Complexity | MVP / Post-MVP recommendation and rationale |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| FEAT-001 | Category management | Fixed or obsolete labels make spending hard to organize. | Keeps entries meaningful and category reports useful. | Create, rename, and archive flat income/expense categories while preserving transaction history. | View categories; create or rename one; archive a used category. | Must Have | None | Medium | MVP — approved scope; provides the classification needed for meaningful spending analysis. Full management may follow initial entry workflows in incremental delivery. |
| FEAT-002 | Record income | The user cannot calculate remaining money without recording incoming funds. | Establishes the income side of monthly and cumulative calculations. | Record multiple income entries with positive whole-VND amount, transaction date, category, and optional description; allow past dates. | Enter amount/date; select category; optionally describe income; save. | Must Have | FEAT-001 for category availability; full category management need not precede entry if predefined categories are separately agreed | Medium | MVP — essential input for monthly remaining amount and running balance. |
| FEAT-003 | Record expenses | The user forgets what was purchased and how much was spent. | Creates the spending history that every spending report requires. | Record what was purchased, a positive whole-VND amount, transaction date, and category; allow past dates. | Describe purchase; enter amount/date; select category; save. | Must Have | FEAT-001 for category availability; same delivery caveat as FEAT-002 | Medium | MVP — the central tracking capability and source of spending analysis. |
| FEAT-004 | Correct transaction entries | Mistyped amounts, dates, or categories and accidental entries make reports inaccurate. | Maintains trust in totals as records change. | Edit income/expense entries or delete mistakes with confirmation; update affected reports and running balances. | Select an entry; edit and save; or request deletion and confirm/cancel. | Must Have | FEAT-002, FEAT-003 | Medium | MVP — manual tracking is not dependable without correction. Forgotten entries are handled by past-date entry in FEAT-002/003. |
| FEAT-005 | Transaction history and filtering | The user cannot verify or find recorded activity efficiently. | Makes records reviewable and supports corrections. | Show one combined history with filters by month, income/expense type, and category. | Browse entries; choose a month/type/category; clear filters. | Must Have | FEAT-002, FEAT-003 | Medium | MVP — provides visibility into the entries underlying financial results. |
| FEAT-006 | Monthly spending overview | Raw entries do not explain how much remains or where the month's money went. | Answers the main monthly-spending question and reveals expenses above income. | Show monthly income, expenses, remaining amount, and expense totals by category using transaction dates; visibly show a negative remaining amount. | Select a month; inspect totals and category breakdown. | Must Have | FEAT-002, FEAT-003 | Medium | MVP — delivers the primary product outcome. Category breakdown is included here rather than treated as a separate dashboard feature. |
| FEAT-007 | Cumulative running balance | A single month's remainder does not show the net result of all recorded activity. | Supports the explicitly requested running-balance goal. | Show cumulative income minus expenses through a selected date, starting at zero; keep it distinct from monthly remainder. | Select an as-of date; view the cumulative result. | Must Have | FEAT-002, FEAT-003 | Medium | MVP — explicitly approved; measures recorded net funds rather than verified bank/cash holdings. |
| FEAT-008 | Monthly spending trend chart | The user cannot easily see whether spending is rising or falling across months. | Makes longer-term spending patterns visible. | Visualize monthly expense totals across recorded history. | Open the trend chart; compare monthly totals. | Must Have | FEAT-003 | Medium | MVP — explicitly requested and approved. It can follow accurate recording and summaries within incremental delivery. |
| FEAT-009 | Monthly spending budget | Income alone does not define the user's desired spending ceiling. | Helps control spending against an intentional limit. | Candidate capability: set one monthly spending limit and compare actual expenses with it; category budgets remain outside this proposal. | Set a monthly limit; view spent, remaining, or exceeded amounts. | Should Have | FEAT-003, FEAT-006 | Medium | Post-MVP — explicitly deferred; a future proposal, not approved scope. Validate whether awareness from summaries and trends is insufficient before adding it. |

## Prioritization and postponement

All eight baseline features are Must Have for the complete approved MVP because removing one would change the agreed boundary. This is not a requirement to deliver all eight at once. The central value sequence is recording and correction, then reviewing results, then understanding patterns. Category availability is a prerequisite for categorized entry; the timing of full category management remains a release-planning question.

The budget feature is postponed because the approved first product tracks recorded money rather than enforcing a separate spending target. Expenses greater than income indicate negative monthly net cash flow; expenses below income do not prove that spending is within a desired personal budget.

Do not add recurring entries, imports, receipt uploads, advanced reports, multiple accounts, or bank integration without evidence that the manual MVP creates a concrete unmet need. Refunds, borrowing, and credit-card workflows remain excluded. Login, remote access, and real financial data require separate scope review.

Docker Compose, database setup, testing, and GitHub Actions are delivery requirements, not user-facing product features, so they have no FEAT IDs in this table.

## Approval boundary

Approve or revise this feature backlog before development tickets are created. Approval of the backlog does not automatically approve FEAT-009 for implementation, resolve the open specification details, or approve the earlier R1 proposal.

