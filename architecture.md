# AI SQL Query & Database Assistant Agent
## Architecture & Folder Structure — Phase 1

---

## 1. System Overview

The system turns a plain-English question into a validated, executed SQL query plus a
plain-English explanation of the results. It is built as a **pipeline of five small,
single-purpose agents** rather than one large prompt, so each step can be tested,
logged, and swapped independently.

```
User (React UI)
      │  "Show students with CGPA above 8"
      ▼
Spring Boot REST API  (/generate-query, /execute-query)
      │
      ▼
┌─────────────────────────────────────────────────────────┐
│                    AGENT PIPELINE                        │
│                                                            │
│  1. Intent Agent   → structured intent (table, filters)  │
│  2. SQL Gen Agent   → SQL string                          │
│  3. Validation Agent→ pass / reject + reason              │
│  4. Execution Agent → runs SQL against MySQL, returns rows│
│  5. Insight Agent   → summary + follow-up suggestions     │
└─────────────────────────────────────────────────────────┘
      │
      ▼
MySQL (ai_sql_assistant)
      │
      ▼
JSON response → React result table + chart + insight panel
```

**Why a pipeline instead of one prompt:** validation and execution are the two steps
where a mistake is expensive (a bad DROP statement, a query that times out). Isolating
them lets us apply hard-coded guardrails (regex/AST checks, permission checks) that
don't depend on the LLM behaving correctly, and lets us log/replay each stage
separately for debugging and auditing.

---

## 2. Tech Stack — decisions and why

| Layer | Choice | Why |
|---|---|---|
| LLM | **Claude API** (`claude-sonnet-5`) | Strong structured-output and instruction-following for SQL generation; supports forced JSON/tool-use output so we don't have to regex-parse free text. |
| Agent orchestration | Plain Java service classes for Phase 1; **LangGraph**-style state machine later | A 5-step pipeline doesn't need a heavyweight framework yet. We start simple and only add LangChain/LangGraph in Phase 3+ if we need branching, retries, or multi-agent negotiation (e.g., an agent asking another agent for clarification). Introducing the framework before it's needed adds indirection without benefit. |
| Backend | Spring Boot + Spring Security + JPA | Matches the mandatory stack; Spring Security gives us role-based access (USER vs ADMIN) and endpoint-level auth almost for free. |
| Database | MySQL | Mandatory; also the target the "Validation Agent" needs to understand (MySQL-specific syntax, e.g. `LIMIT`). |
| Frontend | React | Mandatory; component boundaries map cleanly to Query Console / Result Table / Analytics. |
| Deployment | Docker + GitHub + Render | Mandatory; Docker Compose gives us backend + MySQL as one reproducible unit for local dev and Render deploy. |

**Key architectural decision — the LLM never touches the database directly.**
The LLM only ever produces text (SQL string, explanation text). A separate,
non-LLM component (`QueryExecutionService`) is the only code path allowed to open a
JDBC connection. This is what makes the Validation Agent meaningful: it's a real
gate, not just a suggestion the model could ignore.

---

## 3. Agent Design

### Agent 1 — Intent Agent
- **Input:** raw user text
- **Output (structured JSON):** `{ "action": "SELECT", "entities": ["students"], "filters": [{"field":"cgpa","op":">","value":8}] }`
- **Model call:** Claude API with a JSON-only system prompt and a `tool_use`-style schema so output is guaranteed parseable, not scraped from prose.

### Agent 2 — SQL Generation Agent
- **Input:** structured intent + table schema (injected into the prompt so the model
  never has to guess column names)
- **Output:** a single SQL statement
- **Guardrail:** system prompt explicitly forbids `DROP`, `DELETE`, `UPDATE`, `ALTER`,
  `TRUNCATE`, `GRANT` unless the caller's role is ADMIN and a write flag is set.

### Agent 3 — Validation Agent
- **Not an LLM call.** Deterministic checks for reliability:
  - keyword blocklist (`DROP`, `DELETE`, `--`, `;--`, stacked queries)
  - statement must start with `SELECT` for standard users
  - table/column names must exist in the known schema (prevents hallucinated columns)
  - basic cost heuristic: reject unbounded `SELECT *` on large tables without a `LIMIT`
- Rejection returns a reason, which is fed back to the user (and optionally back to
  Agent 2 for one automatic retry with the error as context).

### Agent 4 — Execution Agent
- Runs the validated SQL via a read-only (or scoped) JDBC connection.
- Enforces a query timeout and row-count cap.
- Returns rows + column metadata.

### Agent 5 — Insight Agent
- **Input:** the original question + the result rows (summarized, not the full raw
  table, to keep the prompt small)
- **Output:** 1–2 sentence insight + 2–3 suggested follow-up questions.

---

## 4. Backend Folder Structure

```
backend/
├── src/main/java/com/aisql/assistant/
│   ├── controller/
│   │   ├── QueryController.java        # /generate-query, /execute-query
│   │   ├── AuthController.java         # /login, /signup
│   │   ├── HistoryController.java      # /history
│   │   └── AnalyticsController.java    # /analytics
│   ├── service/
│   │   ├── QueryOrchestratorService.java   # runs the 5-agent pipeline in order
│   │   ├── ClaudeApiService.java           # thin wrapper around api.anthropic.com
│   │   ├── QueryExecutionService.java      # only class allowed to hit MySQL for user queries
│   │   └── AuthService.java
│   ├── agent/
│   │   ├── IntentAgent.java
│   │   ├── SqlGenerationAgent.java
│   │   ├── ValidationAgent.java
│   │   ├── ExecutionAgent.java
│   │   └── InsightAgent.java
│   ├── prompt/
│   │   ├── IntentPrompt.java
│   │   ├── SqlGenerationPrompt.java
│   │   └── InsightPrompt.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── QueryHistoryRepository.java
│   │   └── ... (JPA repos for students/employees/sales are demo-only, not core product tables)
│   ├── model/
│   │   ├── User.java
│   │   ├── QueryHistory.java
│   │   └── Role.java
│   ├── dto/
│   │   ├── GenerateQueryRequest.java
│   │   ├── QueryResultResponse.java
│   │   └── InsightResponse.java
│   ├── security/
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthFilter.java
│   │   └── JwtUtil.java
│   ├── config/
│   │   └── ClaudeApiConfig.java        # base URL, model name, timeouts
│   └── utils/
│       └── SqlSafetyUtils.java         # blocklist / AST helpers used by ValidationAgent
└── src/main/resources/
    ├── application.yml
    └── schema.sql / data.sql           # seeds users/students/employees/sales tables
```

## 5. Frontend Folder Structure

```
frontend/
├── src/
│   ├── pages/
│   │   ├── Landing.jsx
│   │   ├── Login.jsx
│   │   ├── Dashboard.jsx
│   │   ├── QueryConsole.jsx      # main NL → SQL interaction
│   │   ├── History.jsx
│   │   ├── Analytics.jsx
│   │   └── Admin.jsx
│   ├── components/
│   │   ├── Sidebar.jsx
│   │   ├── ResultTable.jsx
│   │   ├── QueryEditor.jsx
│   │   ├── PromptSuggestions.jsx
│   │   ├── InsightPanel.jsx
│   │   └── Charts/
│   ├── api/
│   │   └── queryApi.js           # calls /generate-query, /execute-query
│   ├── context/
│   │   └── AuthContext.jsx
│   └── App.jsx
```

---

## 6. API Contract (Phase 1 subset in bold)

| Method | Path | Purpose |
|---|---|---|
| **POST** | **/generate-query** | question → intent → SQL → validation result (does not execute) |
| POST | /execute-query | runs a validated SQL id/string, returns rows + insight |
| GET | /history | past queries for the logged-in user |
| GET | /analytics | usage stats, most-queried tables |
| POST | /login | auth |
| POST | /signup | create account |

---

## 7. Claude API Integration Notes

- Model: `claude-sonnet-5`, called server-side only (key never reaches the browser).
- Intent Agent and SQL Generation Agent request structured/JSON output so parsing is
  deterministic.
- Schema (table/column names) is injected into every SQL-generation call — the model
  is never asked to guess a schema it wasn't given.
- `max_tokens` kept small (a single SQL statement or a short JSON object), which keeps
  latency and cost predictable per query.

---

## 8. Phase Roadmap

- **Phase 1 (this doc)** — architecture + folder structure, no code yet.
- **Phase 2 (next)** — minimal working slice: `students` table only, Intent + SQL
  Generation collapsed into one Claude call, Validation Agent (blocklist + SELECT-only),
  a single `/generate-query` endpoint, executed against a local MySQL/H2 instance, and
  a curl/Postman-level end-to-end demo (no auth, no UI yet).
- **Phase 3** — add Execution + Insight agents, all 4 demo tables, `/execute-query`.
- **Phase 4** — auth (Spring Security + JWT), `/history`, `/login`, `/signup`.
- **Phase 5** — React frontend (Query Console first, then Dashboard/History/Analytics).
- **Phase 6** — Docker Compose, README, deploy to Render.
- **Phase 7** — advanced features (CSV export, query cost analysis, role permissions,
  voice input) and test suites.

---

## 9. What's Next

Phase 2 will produce: a runnable Spring Boot project with one endpoint
(`POST /generate-query`), one Claude-backed agent, the Validation Agent's safety
checks, and a walkthrough showing "Show students with CGPA above 8" go end-to-end
to a real SQL result — all runnable locally with Docker Compose (Spring Boot + MySQL).
