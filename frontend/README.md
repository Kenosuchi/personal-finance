# Personal Finance frontend

This directory contains the Angular shell for the Personal Finance application. R0-003 provides
only the common shell, root and not-found routes, and the same-origin HTTP boundary. Finance
workflows are intentionally not implemented yet.

## Pinned toolchain

- Node.js: 22.23.2 (also recorded in `.nvmrc` and `package.json`)
- npm: 10.9.8
- Angular framework: 21.2.25
- Angular CLI/build tooling: 21.2.27
- TypeScript: 5.9.3
- RxJS: 7.8.2
- Vitest: 4.1.12

The committed `package-lock.json` records the complete resolved dependency graph. Use `npm ci`
rather than `npm install` when verifying a clean checkout. The commands below use Corepack so the
`packageManager` field selects the pinned npm release instead of an unrelated global npm version.

## Install, build, and test

From this directory:

```bash
corepack npm ci
corepack npm run build
corepack npm run test:ci
```

`corepack npm run build` uses Angular's production configuration. `corepack npm run test:ci` runs
the focused Vitest checks once, without watch mode.

For local development:

```bash
corepack npm start
```

Open `http://localhost:4200/`. The project-local Angular CLI is used through the npm scripts; a
global `ng` installation is not required.

## HTTP boundary and development proxy

Frontend API requests use the same-origin relative base path `/api`. Application code must not
embed a backend hostname, deployment URL, credential, or secret.

During `corepack npm start`, Angular reads `src/proxy.conf.json` and forwards `/api/**` to the local
backend at `http://localhost:8080`. The proxy target is development tooling configuration;
browser-facing requests remain under `/api`.

R0-003 verifies the request boundary with `HttpTestingController` and reviews this proxy
configuration. Live frontend-to-backend and container proxy evidence belongs to R0-007/R0-008.

At application startup, Angular performs one `GET /api/csrf` request through this same boundary.
The backend sets the standard `XSRF-TOKEN` cookie, and Angular's built-in XSRF support copies it
to the `X-XSRF-TOKEN` header on relative unsafe requests. Bootstrap failure rejects application
initialization; it is not converted into readiness, retried automatically, or followed by a
mutation request. No backend URL, token, credential, or secret is stored in frontend
configuration.

## Manual shell check

With the development server running:

1. Open `/` and confirm the common shell and foundation-only message are visible.
2. Open an unknown client route such as `/missing/nested-route`.
3. Confirm the not-found view appears inside the same shell.
4. Select **Return to Home** and confirm the URL and root content return to `/`.

Automated browser/end-to-end testing, SSR, PWA support, authentication, finance screens, global
state management, and UI frameworks are outside this ticket.
