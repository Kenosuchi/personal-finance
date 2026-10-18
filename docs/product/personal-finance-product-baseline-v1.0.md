# Personal Finance Management App — Product Baseline v1.0

Status: Approved

Approved by: Product Owner (user), with “Ok approve” following the initial baseline proposal.

Approval date: 2026-10-08 (Asia/Saigon)

## Purpose and context

Build a practical personal-finance web application while learning enterprise full-stack development. The initial product helps one user record income and spending, understand monthly expenses, and calculate the remaining amount and cumulative running balance from recorded transactions.

One developer with 5+ years of web development experience will work at least 10 hours per week. Delivery uses incremental releases without fixed sprints. Priorities are correctness, maintainability, learning value, and realistic business workflows. Initial usage is on a local computer with sample data.

## Approved MVP scope

| Capability | Approved boundary |
| --- | --- |
| Income entry | Positive whole-VND amount, transaction date, category, and optional description. Multiple income entries per month are allowed. |
| Expense entry | What was purchased, a positive whole-VND amount, transaction date, and category. |
| Corrections | Enter forgotten transactions for previous days; edit entries; delete mistaken entries with confirmation. |
| Categories | Create, rename, and archive flat categories while preserving transaction history. |
| Transaction history | One combined history; list entries and filter by month, income/expense type, and category. |
| Monthly summary | Total income, total expenses, remaining amount, and spending by category for a selected month. |
| Spending chart | Monthly expense totals across the recorded history. |
| Running balance | Cumulative recorded income minus cumulative recorded expenses through a selected date, starting at zero. |

## Approved calculations and rules

- Currency is VND only. Amounts are positive whole đồng values; transaction type determines whether an amount adds to or subtracts from the balance.
- Monthly remaining amount = income recorded for the selected month minus expenses recorded for that month.
- Running balance = all recorded income minus all recorded expenses through the selected date. There is no opening balance.
- Monthly remaining amount and cumulative running balance are distinct metrics. Prior-month remaining amounts do not become income entries in a later month.
- Reports use the transaction date, rather than the entry creation date.
- Backdated entries, edits, and deletions update the affected monthly reports and subsequent running balances.
- Negative remaining amounts are allowed and visibly indicate that recorded expenses exceeded recorded income.
- Transfers between the user's own accounts are outside scope and must not be recorded as income or expenses.
- Calculations describe recorded transactions; they do not verify actual cash, bank holdings, or completeness of the user's records.

Example: September income of 20,000,000 VND and expenses of 12,000,000 VND produce a September remaining amount of 8,000,000 VND. October income of 20,000,000 VND and expenses of 15,000,000 VND produce an October remaining amount of 5,000,000 VND and a cumulative balance of 13,000,000 VND at the end of October, assuming no other transactions.

## Excluded or deferred

- Separate monthly budget feature. The MVP supports spending awareness, comparisons, and identifying expenses above recorded income; it does not claim to detect overspending against a personal budget.
- Refunds, borrowing, and credit-card workflows.
- Login, separate accounts/wallets, multi-user functionality, and shared households.
- Recurring entries, imports, bank integration, receipt uploads, and advanced reports.
- Multiple currencies, investment tracking, and net-worth calculations.

The approved target is a local sample-data MVP. Remote access and real financial data require a separate scope review; they are not approved by this baseline.

## Delivery constraints

- Frontend: a supported Angular version, exact version to be confirmed.
- Backend: Spring Boot 4.
- Java: 25.
- Database: MySQL.
- Infrastructure: Docker Compose.
- Testing: unit and integration tests.
- CI/CD: GitHub Actions.

Exact unconfirmed technology versions, deployment automation details, and architecture choices are not established by this approval. No implementation code is part of this document.

## MVP usability condition

The user can record a month's income and expenses, correct a forgotten entry, and see consistent results in transaction history, monthly summary, spending chart, and running balance.

## Pending specification details

These details remain open and must be resolved before the relevant implementation tickets are finalized:

- Future transaction dates and the default date used for the running balance.
- Reporting calendar/timezone and date-range boundaries.
- Category rename/archive behavior in historical displays and new entries.
- Amount limits, description limits, and other field validation beyond the approved rules.
- Chart treatment of months without records and partial months.
- Exact supported Angular version and other unconfirmed runtime/image versions.

## Change control and planning status

This document records the approved initial scope. Any change to these boundaries must be presented explicitly as a proposal before being treated as a decision. Implementation details may be refined within approved scope, with unresolved business behavior called out.

No release plan, implementation tickets, dates, or effort estimates have been approved. Plan one incremental release at a time, using user value, dependencies, and implementation evidence.

## Decision log

| Date | Decision |
| --- | --- |
| 2026-10-08 | Approved initial baseline v1.0, including one combined transaction history, no opening balance, and deferring a separate budget feature. |
