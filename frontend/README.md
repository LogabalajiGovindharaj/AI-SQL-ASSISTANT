# AI SQL Assistant — Frontend (Phase 5)

React Query Console: login/signup, natural-language question input, live
pipeline status (Understand → Validate → Execute → Insight), generated SQL,
result table, and AI insight with follow-up suggestions.

## Run it

```bash
npm install
npm start
```
Runs on http://localhost:3000 and calls the backend at http://localhost:8080
by default. Override with an `.env` file:
```
REACT_APP_API_BASE_URL=http://localhost:8080
```

The backend must be running (see `../backend/README.md`) with CORS already
configured to allow `http://localhost:3000`.

## What's here
- `pages/Landing.jsx` — intro screen before login
- `pages/Login.jsx` — login + signup, stores the JWT via AuthContext
- `pages/Dashboard.jsx` — post-login overview: stats + recent queries + shortcuts
- `pages/QueryConsole.jsx` — the main flow: calls `/generate-query`, then
  `/execute-query`, rendering each pipeline stage as it happens, plus CSV
  export, a downloadable text report, and voice input (Web Speech API, no
  backend involved)
- `pages/History.jsx` — past queries for the logged-in user
- `pages/Analytics.jsx` — usage stats, with an admin-only "all users" toggle
- `pages/Admin.jsx` — user list (ADMIN only; backend enforces this too, not just the UI)
- `components/PipelineStepper.jsx` — visualizes the real 4-stage backend
  pipeline (Understand / Validate / Execute / Insight), not a decorative
  progress bar
- `components/ResultTable.jsx` — renders query result rows
- `utils/export.js` — CSV export + plain-text report download (client-side, no backend endpoint needed)
- `utils/useVoiceInput.js` — wraps the browser's SpeechRecognition API

## What's deliberately NOT here yet
- CSV export and voice input use browser-native APIs rather than a backend endpoint — this is simpler and works offline, but voice input requires a Chromium-based browser (Web Speech API isn't supported everywhere, e.g. Firefox)
