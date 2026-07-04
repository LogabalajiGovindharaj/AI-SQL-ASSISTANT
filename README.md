<<<<<<< HEAD
=======
<<<<<<< HEAD
# AI SQL Query & Database Assistant Agent

Turn plain-English questions into validated, executed SQL with an explanation,
follow-up suggestions, and query-cost feedback — built as a 5-agent pipeline
(Intent+SQL Generation → Validation → Execution → Insight, plus a deterministic
Optimization Agent) on Spring Boot, backed by Claude, with a React console on top.

See [`architecture.md`](./architecture.md) for the full system design and
architecture diagram (ASCII pipeline diagram + component breakdown, folder
structure, and API contract).

## Repo layout

```
.
├── backend/          Spring Boot API + agent pipeline (see backend/README.md)
│   ├── src/main/      application code
│   ├── src/test/      unit + integration test suite
│   ├── load-tests/    k6 load test
│   └── Dockerfile
├── frontend/          React console (see frontend/README.md)
│   └── Dockerfile
├── docker-compose.yml runs mysql + backend + frontend together
├── render.yaml         Render deployment blueprint
└── architecture.md    Phase 1 architecture + diagram reference
```

## 1. Installation (local, without Docker)

**Requirements:** JDK 17+, Maven 3.9+, Node.js 18+, a running MySQL 8 instance.

```bash
# 1. Create the database (schema.sql/data.sql seed it automatically on boot)
mysql -u root -p -e "CREATE DATABASE ai_sql_assistant"

# 2. Backend
cd backend
export ANTHROPIC_API_KEY=sk-ant-...
export JWT_SECRET=$(openssl rand -hex 32)
export DB_HOST=localhost DB_USER=root DB_PASSWORD=yourpassword
./mvnw spring-boot:run    # or: mvn spring-boot:run
# backend now on http://localhost:8080

# 3. Frontend (separate terminal)
cd frontend
npm install
npm start
# frontend now on http://localhost:3000
```

> This repo does not include a checked-in Maven wrapper (`mvnw`). If you
> don't have Maven installed locally, generate one with `mvn -N
> wrapper:wrapper` inside `backend/`, or just use your own `mvn` (both
> commands above have an `mvn` fallback shown).

## 2. Docker setup (recommended)

```bash
export ANTHROPIC_API_KEY=sk-ant-...
export JWT_SECRET=$(openssl rand -hex 32)
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- MySQL: localhost:3306 (root/root, database `ai_sql_assistant`)

Sign up on the frontend, then ask things like "Show students with CGPA above
8" or "Show total sales region wise." The pipeline status (Understand →
Validate → Execute → Insight) updates live as each agent runs.

To promote a user to `ADMIN` (unlocks the Admin page and `/admin/*`
endpoints):
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```
Log in again afterwards so a fresh JWT (carrying the new role) is issued.

## 3. Testing instructions

```bash
cd backend
./mvnw test    # or: mvn test
```

Tests run against an in-memory H2 database (MySQL-compatible mode) — no live
MySQL instance or Claude API key needed. Coverage includes:
- `ValidationAgentTest` — the deterministic SQL safety gate (blocklists, stacked statements, unknown tables)
- `AnalyticsServiceTest` — usage-stat aggregation logic
- `ClaudeApiServiceTest` — real HTTP-level contract tests against a local stub server standing in for `api.anthropic.com` (success, HTTP error, malformed JSON, missing tool_use block, connection failure)
- `SqlGenerationAgentTest`, `InsightAgentTest` — agent behavior with a mocked Claude client (success, missing fields, exception propagation)
- `QueryOrchestratorServiceTest` — full pipeline wiring (accept/reject paths, defense-in-depth re-validation on `/execute-query`, execution-failure propagation)
- `SecurityIntegrationTest` — runs the real Spring Security filter chain: 401 unauthenticated, 403 for USER on `/admin/**`, 200 for ADMIN, public routes reachable

Load test (optional, requires [k6](https://k6.io) and a running backend):
```bash
TOKEN=<jwt from /login> k6 run backend/load-tests/query-pipeline.js
```

> **Environment note:** this project was developed in a sandboxed environment
> without access to Maven Central or the npm registry, so `mvn test` and `npm
> install` could not be executed here to produce a literal pass/fail log.
> Every file was reviewed manually and cross-checked for structural
> correctness, and the JS/JSX was checked for syntax validity, but you should
> run the commands above yourself before treating the build as verified.

## 4. Deployment guide

**GitHub:** this repo is ready to push as-is — see the "Prepare for GitHub"
checklist in the next section.

**Render:** a `render.yaml` blueprint is included at the repo root, defining
the backend and frontend as Docker web services. Render has no managed MySQL
product (only Postgres/Redis), so provision MySQL separately first:
1. A Render **private service** running the official `mysql` Docker image
   with a persistent disk ([reference template](https://render.com/templates/mysql)), or
2. An external managed MySQL provider (PlanetScale, TiDB Cloud, etc).

Then, from the Render dashboard: New → Blueprint → point at this repo. Fill
in the `sync: false` env vars (`DB_HOST`, `DB_USER`, `DB_PASSWORD`,
`ANTHROPIC_API_KEY`) when prompted; `JWT_SECRET` is auto-generated by the
blueprint. Update `CORS_ALLOWED_ORIGINS` and the frontend's
`REACT_APP_API_BASE_URL` build arg in `render.yaml` to match your actual
Render-assigned service URLs before deploying (the placeholders use the
default `onrender.com` naming pattern, which only matches if your service
names are unchanged).

## 5. Prepare for GitHub checklist

- [x] `.gitignore` present at root, `backend/`, and `frontend/` (build
      artifacts, `node_modules`, `target/`, `.env`, IDE files)
- [x] No secrets committed — `ANTHROPIC_API_KEY`, `JWT_SECRET`, and DB
      credentials are all read from environment variables; `application.yml`
      only contains local-dev-only placeholder defaults, never real keys
- [x] Clean folder structure — no stray/duplicate files
- [x] `README.md` (this file) + per-module READMEs in `backend/` and `frontend/`

## What's built

Every phase of the original spec is implemented: architecture doc, all 5
agents (+ a deterministic Optimization/Cost Agent), the full REST API
(`/generate-query`, `/execute-query`, `/history`, `/analytics`,
`/admin/analytics`, `/admin/users`, `/login`, `/signup`, `/health`), JWT auth
with enforced roles, all demo tables + query history, and a React frontend
(Landing, Login/Signup, Dashboard, Console, History, Analytics, Admin) with
CSV export, downloadable reports, and voice input.

**Known limitations:**
- Build/test commands could not be literally executed in the environment
  this was built in (no Maven Central / npm registry access) — verify
  locally with the commands in section 3 before treating this as CI-passing.
- No GitHub repo or live Render deployment exists yet — this repo is
  deployment-ready, but pushing to GitHub and clicking through Render setup
  are manual steps only you can do.
- Voice input requires a Chromium-based browser (Web Speech API is not
  supported in Firefox).
=======
# AI-SQL-ASSISTANT
AI-powered SQL Query &amp; Database Assistant that converts natural language into optimized SQL, executes queries securely, validates results, and provides intelligent database insights through a modern full-stack interface.
>>>>>>> c9cc852df467a0242bb8dc4fef7a3275270a8198
>>>>>>> 0b7090a89db9e444d69d342d43dbd8a7dea115f7
