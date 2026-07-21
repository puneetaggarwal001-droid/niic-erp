# NIIC ERP

`attendance_module.html` is the original prototype (plain HTML/CSS/JS + Firebase
Auth/Firestore, everything crammed into one file). It's kept as-is for
reference. `backend/` and `frontend/` are the new split architecture that will
replace it, module by module.

## Status

- **Done**: auth (JWT login, users, roles), attendance module (designations,
  employee master, daily attendance records) — fully wired frontend-to-backend.
- **Backend done, frontend not yet wired**: production module — workstations,
  operations, PC rates, jobs (with colour/size variants + atomic job
  numbering), job requests, routing (with the operation-dependency DAG),
  routing change requests, production entries (merge-on-conflict, routing +
  dependency-flow validation, auto-QC-entry generation), production edit
  requests, operation closures, QC entries + rework loop, and transfer
  challans. See `backend/src/main/java/com/niic/erp/production/package-info.java`
  for the design notes and `db/migration/V2__production_module.sql` for the
  schema. No React pages exist for this module yet — only the REST API.
- **Not yet ported** (package placeholders exist in
  `backend/src/main/java/com/niic/erp/{store,sampling,payroll}`, each with a
  comment listing what it needs to hold): store/inventory, sampling, payroll.
  Build these the same way attendance and production were built — entity →
  repository → service → controller → dto, plus a new Flyway migration per
  module.

## Prerequisites

- JDK 21, Maven, and Node.js 20+/npm — installed via Homebrew
  (`openjdk@21`, `maven`, `node`).

## Backend

```bash
cd backend
mvn spring-boot:run
```

Starts on `http://localhost:8080`. Default profile (`dev`) uses a local H2
file database at `backend/data/erp-dev.mv.db` — no external DB needed. Flyway
runs the migration in `src/main/resources/db/migration/` automatically and
seeds one admin account:

- username: `admin`
- password: `***REMOVED***`

**Change or remove that seed user before this touches real data.**

For Postgres instead of H2, run with `--spring.profiles.active=prod` and set
`ERP_DB_URL` / `ERP_DB_USER` / `ERP_DB_PASSWORD` env vars.

Run tests: `mvn test`.

## Frontend

```bash
cd frontend
npm install
npm run dev
```

Starts on `http://localhost:5173`, proxying `/api` to the backend on port
8080 (see `vite.config.ts`). Log in with the seed admin account above.

## Architecture notes

- **Auth**: stateless JWT (see `security/JwtService.java`), not Firebase Auth.
  Roles: `ADMIN`, `ENTRY_USER`, `STORE_ADMIN`, matching the legacy app's
  `session.role` checks. Per-user feature flags (the old `RIGHTS` registry)
  are stored as a `Set<String>` on `User.rights`.
- **Data model**: the legacy app stored *everything* — attendance, production,
  inventory, payroll — as JSON blobs in one Firestore collection keyed by
  string (`fatt_employees`, `fprod_jobs`, etc., see the `K` object in
  `attendance_module.html`). The new backend gives each of those a real
  relational table instead (see `db/migration/V1__init_auth_and_attendance.sql`).
- **Employee IDs**: `EMP-001`-style codes are still generated the same way
  (`EmployeeIdAllocator`, max-existing-suffix + 1), now backed by a real
  unique DB column instead of a counter key + collision retry loop.
- Aadhaar numbers are stored as plain text for now — flagged with a `TODO` in
  `Employee.java`. Encrypt at rest before any real employee data goes in.
