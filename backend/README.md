# AI SQL Assistant — Backend (Phase 4)

## What's new in Phase 4
- Spring Security + JWT auth
- `POST /signup` and `POST /login` (public)
- `/generate-query` and `/execute-query` now require a valid `Authorization: Bearer <token>` header
- Passwords are BCrypt-hashed, never stored or returned in plaintext
- New signups are always role `USER`; `ADMIN` is granted manually (never self-assigned via signup)

## Run it

```bash
export ANTHROPIC_API_KEY=sk-ant-...
export JWT_SECRET=$(openssl rand -hex 32)   # or any string >= 32 chars
docker compose up --build
```
(Remember to pass `JWT_SECRET` through in `docker-compose.yml`'s backend environment
block if you keep it — it's currently defaulted for local dev only.)

## Try it end-to-end

```bash
# 1. Sign up
curl -s -X POST http://localhost:8080/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Balaji","email":"balaji@example.com","password":"password123"}' | tee /tmp/auth.json

TOKEN=$(python3 -c "import json;print(json.load(open('/tmp/auth.json'))['token'])")

# 2. Generate SQL (now requires the token)
curl -s -X POST http://localhost:8080/generate-query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"question": "Show students with CGPA above 8"}' | tee /tmp/gen.json

# 3. Execute it
SQL=$(python3 -c "import json;print(json.load(open('/tmp/gen.json'))['sql'])")
curl -s -X POST http://localhost:8080/execute-query \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{\"sql\": \"$SQL\", \"question\": \"Show students with CGPA above 8\"}"
```

Calling `/generate-query` or `/execute-query` without a token now returns `401 Unauthorized`.

## What's new in Phase 8
- `GET /analytics` — any authenticated user gets their own usage stats (total/valid/rejected query counts, most-queried tables, recent rejections)
- `GET /admin/analytics` — same stats across **all** users, restricted to `ADMIN` role at the Spring Security filter-chain level (not just hidden in the UI — a non-admin token gets a real `403`)
- All signups default to role `USER`. To promote someone to `ADMIN` for local testing:
  ```sql
  UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
  ```
  They'll need to log in again afterwards so their JWT is reissued with the new role.

## What's new in Phase 9
- `OptimizationAgent`: `/execute-query` now also returns `estimatedRowsScanned` and `accessType` (from MySQL's own `EXPLAIN`) plus deterministic optimization tips (missing WHERE/LIMIT, `SELECT *`, full table scans)
- `GET /admin/users` — ADMIN-only user list
- Real test coverage: `ValidationAgentTest` (12 cases), `AnalyticsServiceTest`, and `SecurityIntegrationTest` (runs the actual Spring Security filter chain against an in-memory H2 DB — verifies 401 for unauthenticated calls, 403 for USER hitting `/admin/**`, 200 for ADMIN)
- `load-tests/query-pipeline.js` — a k6 load test for the generate→execute flow

Run the test suite with:
```bash
./mvnw test
```
(Tests use H2 in-memory, not your MySQL instance — no live DB needed to run them.)

## What's deliberately NOT here yet
- `/history`, `/analytics`
- React frontend (Phase 5, next)
- Query cost analysis, CSV export, fine-grained role permissions, voice input
