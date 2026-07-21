# Continuation notes (handoff to a new machine/session)

This project is being ported module-by-module from a legacy single-file
prototype (`attendance_module.html`, for "Nivansh India Infant Care" / NIIC)
into a Spring Boot backend + Vite/React/TypeScript frontend. Read this file
first before doing anything else, then treat it as background context, not
a task list to blindly execute.

## Status

- **Auth, Attendance, and Production modules**: backend fully built and
  tested (`backend/src/test/java/com/niic/erp/...ApiTest.java`, run via
  `mvn test`).
- **Production module frontend**: fully built (7 pages under
  `frontend/src/pages/production/`, API client in
  `frontend/src/api/production.ts` + `productionTypes.ts`, wired into
  `frontend/src/App.tsx`). `tsc --noEmit` passes.
- **Not yet done**: full manual browser verification of the production
  frontend end-to-end (masters -> job -> routing -> production entry ->
  QC -> transfer challan) against a freshly-seeded backend. This was
  in progress when the previous session ran out of usage and should be
  resumed next.
- **Not started**: store, sampling, payroll modules (backend or frontend).
  Do not start these without an explicit user request — ask first, per
  established project convention.

## Environment quirks (macOS, Homebrew)

- `node`/`npm`/`mvn` are not on the default shell PATH used by tooling.
  Prefix commands with:
  `export PATH="/opt/homebrew/bin:$PATH"`
- `java -version` fails via the system stub; the real JDK is a keg-only
  Homebrew formula. Set `JAVA_HOME="/opt/homebrew/opt/openjdk@21"` before
  running `mvn`.
- Backend dev DB is an H2 file DB under `backend/data/` (gitignored). If
  you delete it, all previously-created runtime data (the bootstrapped admin,
  jobs, workstations, etc.) is gone. On next startup `AdminBootstrap` recreates
  the admin (set `ERP_ADMIN_PASSWORD`, or read the generated dev password from
  the logs); everything else is re-seeded manually through the UI.

## Known frontend gotcha

`AuthContext` (`frontend/src/context/AuthContext.tsx`) keeps `username`/
`role`/`rights` **in memory only** — only the JWT persists in
`localStorage`. A hard page reload (or the browser tool's `navigate()`,
which does a real reload) leaves `isAuthenticated` true but resets
`auth` to `null`, so `auth?.role === 'ADMIN'` checks wrongly evaluate
false right after a reload. When testing role-gated UI in a browser,
log in once, then navigate via in-app `<Link>` clicks (client-side
routing), not full-page reloads, or you'll misdiagnose this as a bug.

## Recently fixed backend bug (already fixed, for context only)

Several production-module service read methods (`JobService.listActive/
listAll/get`, `ProductionEntryService.listForDate/listForJob`,
`RoutingService.getForJob/findTemplate`,
`TransferChallanService.listPendingForWorkstation/listByStatus`) were
missing `@Transactional(readOnly = true)`, causing unlogged 500s
(`LazyInitializationException`) when their DTOs touched lazy
collections outside a Hibernate session. Fixed, plus
`GlobalExceptionHandler`'s catch-all now logs unhandled exceptions
instead of swallowing them silently, and `ProductionApiTest.java` has
regression assertions covering the exact endpoints that broke.

## Repo / GitHub

- Private repo: `https://github.com/puneetaggarwal001-droid/niic-erp`
  (branch `main`).
- To continue on a new machine: clone the repo, run
  `cd backend && export PATH="/opt/homebrew/bin:$PATH" JAVA_HOME="/opt/homebrew/opt/openjdk@21" && mvn spring-boot:run`
  and in another terminal
  `cd frontend && export PATH="/opt/homebrew/bin:$PATH" && npm install && npm run dev`.
  See `README.md` for full instructions.

## Suggested next step

Resume manual browser verification of the production frontend: log in
as the bootstrapped `admin` (password from `ERP_ADMIN_PASSWORD` or the
startup log), re-create a Stitching + Packing workstation and a
Sew operation on the Masters page, create a test job, save its routing,
log a production entry, and confirm the previously-broken list/GET
endpoints (jobs, routing, entries, transfer challans) now return 200
with correctly nested data. Then continue through QC and transfer
challans to confirm the whole flow works end-to-end. Once verified,
report back and wait for the user's direction on which module to build
next (store/sampling/payroll) rather than starting one unprompted.
