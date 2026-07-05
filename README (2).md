<div align="center">

# 🤖 AI SQL Query & Database Assistant Agent

**Ask your database a question in plain English. Get validated, executed SQL — with an explanation, insights, and cost analysis.**

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen?logo=springboot)
![React](https://img.shields.io/badge/React-18-blue?logo=react)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?logo=mysql)
![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker)
![Claude](https://img.shields.io/badge/AI-Claude%20API-D97757)
![Status](https://img.shields.io/badge/status-production--ready-brightgreen)

### 🔗 [**Live Demo →**](https://ai-sql-assistant-frontend-4a6h.onrender.com/)

*(Free-tier Render services spin down when idle — first load after inactivity may take ~30s to wake up.)*

</div>

---

## 🎥 What it does

```
"Show students with CGPA above 8"
              │
              ▼
     🧠 Understand   →   🛡️ Validate   →   ⚙️ Execute   →   💡 Insight
              │
              ▼
   Real SQL, real rows, plain-English explanation, and follow-up suggestions.
```

No manual SQL. No trusting the AI blindly — every generated query passes through
a deterministic safety gate before it ever touches the database.

---

## ✨ Features

| Category | What's included |
|---|---|
| 🧠 **AI Agent Pipeline** | Intent + SQL Generation, Validation, Execution, Insight, and Optimization/Cost agents — Claude-powered, with a non-LLM safety gate in front of the database |
| 🔐 **Auth & Roles** | JWT-based login/signup, `USER`/`ADMIN` roles enforced at the security layer (not just hidden in the UI) |
| 📊 **Dashboard & Analytics** | Usage stats, most-queried tables, rejection log, admin-wide analytics view |
| 📁 **History** | Every query — accepted or rejected — logged per user |
| 📤 **Export** | One-click CSV export and downloadable text reports |
| 🎙️ **Voice Input** | Ask questions by speaking (browser Web Speech API) |
| ⚡ **Query Cost Analysis** | Real MySQL `EXPLAIN` output + optimization tips (missing `WHERE`/`LIMIT`, full table scans, etc.) |
| 🐳 **Dockerized** | One-command local spin-up: MySQL + backend + frontend together |
| ✅ **Tested** | Unit, integration, security, and HTTP-contract tests (Claude API mocked at the HTTP level) |

---

## 🏗️ Production Readiness Pipeline

<details open>
<summary><strong>Click to expand / collapse</strong></summary>

| Phase | Milestone | Status |
|:--:|---|:--:|
| 1 | Architecture — requirements, system design, DB design, agent design, folder structure | ✅ 100% |
| 2 | Backend Foundation — Spring Boot, REST APIs, DB connection, config, logging | ✅ 100% |
| 3 | AI Agent Pipeline — Intent/SQL, Validation, Execution, Insight, Optimization agents | ✅ 100% |
| 4 | Authentication — JWT, login/signup, roles, protected APIs | ✅ 100% |
| 5 | Frontend — React console, history, analytics dashboard, responsive UI | ✅ 100% |
| 6 | Advanced Features — analytics endpoint, CSV export, SQL optimization, cost analysis, voice input, admin dashboard | ✅ 100% |
| 7 | Testing — validation unit tests, integration, security, load, API contract tests | ✅ 100% |
| 8 | Production Hardening — exception handling, Docker fixes, dependency cleanup, config fixes, bug fixes | ✅ 100% |
| 9 | Deployment — push to GitHub, deploy to Render, live testing | ✅ **Live** → [ai-sql-assistant-frontend-4a6h.onrender.com](https://ai-sql-assistant-frontend-4a6h.onrender.com/) |

```
┌─────────────────────────────────────────────┐
│  Architecture → Backend → AI Agents → Auth   │
│      → Frontend → Advanced Features          │
│      → Testing → Hardening → 🚀 Deployed     │
└─────────────────────────────────────────────┘
```

</details>

---

## 🚀 Quick Start

### Try it live
👉 **[ai-sql-assistant-frontend-4a6h.onrender.com](https://ai-sql-assistant-frontend-4a6h.onrender.com/)** — sign up and ask a question.

### Run it locally with Docker

```bash
git clone <this-repo-url>
cd ai-sql-assistant
export ANTHROPIC_API_KEY=sk-ant-...
export JWT_SECRET=$(openssl rand -hex 32)
docker compose up --build
```

- Frontend → http://localhost:3000
- Backend → http://localhost:8080

Full installation, testing, and deployment instructions: see [`SETUP.md`](./SETUP.md) *(detailed setup guide)* and [`architecture.md`](./architecture.md) *(full system design + diagrams)*.

---

## 🧩 Tech Stack

**Backend:** Java 17 · Spring Boot · Spring Security · JWT · JPA/JDBC · MySQL
**AI Layer:** Claude API (Anthropic) · tool-use structured output
**Frontend:** React · Web Speech API
**DevOps:** Docker · Docker Compose · Render

---

## 📄 License

MIT — see [`LICENSE`](./LICENSE).

<div align="center">

Built as a full agentic AI system — from architecture doc to live deployment.

</div>
