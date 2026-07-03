import React, { useState } from "react";
import { queryApi } from "../api/queryApi";
import { useAuth } from "../context/AuthContext";
import PipelineStepper from "../components/PipelineStepper";
import ResultTable from "../components/ResultTable";
import History from "./History";
import Analytics from "./Analytics";
import Dashboard from "./Dashboard";
import Admin from "./Admin";
import { useVoiceInput } from "../utils/useVoiceInput";
import { exportCsv, exportReport } from "../utils/export";
import "./QueryConsole.css";

const SUGGESTIONS = [
  "Show students with CGPA above 8",
  "Show highest paid employees",
  "Show total sales region wise",
  "Show average student marks",
];

export default function QueryConsole() {
  const { auth, logout } = useAuth();
  const [tab, setTab] = useState("dashboard"); // "dashboard" | "console" | "history" | "analytics" | "admin"
  const [question, setQuestion] = useState("");
  const [stage, setStage] = useState("idle");
  const [sql, setSql] = useState("");
  const [explanation, setExplanation] = useState("");
  const [rejectionReason, setRejectionReason] = useState("");
  const [rows, setRows] = useState(null);
  const [insight, setInsight] = useState("");
  const [suggestions, setSuggestions] = useState([]);
  const [costInfo, setCostInfo] = useState(null);
  const [error, setError] = useState("");

  const voice = useVoiceInput((transcript) => {
    setQuestion(transcript);
    runQuery(transcript);
  });

  function resetResults() {
    setSql("");
    setExplanation("");
    setRejectionReason("");
    setRows(null);
    setInsight("");
    setSuggestions([]);
    setCostInfo(null);
    setError("");
  }

  async function runQuery(q) {
    const text = (q ?? question).trim();
    if (!text) return;

    resetResults();
    setStage("understand");

    try {
      const generated = await queryApi.generate(text, auth.token);
      setSql(generated.sql);
      setExplanation(generated.explanation);

      setStage("validate");
      if (!generated.valid) {
        setStage("rejected");
        setRejectionReason(generated.rejectionReason);
        return;
      }

      setStage("execute");
      const executed = await queryApi.execute(generated.sql, text, auth.token);
      if (!executed.valid) {
        setStage("rejected");
        setRejectionReason(executed.rejectionReason);
        return;
      }
      setRows(executed.rows);
      setCostInfo({
        estimatedRowsScanned: executed.estimatedRowsScanned,
        accessType: executed.accessType,
        optimizationTips: executed.optimizationTips || [],
      });

      setStage("insight");
      setInsight(executed.insight);
      setSuggestions(executed.suggestions || []);
      setStage("done");
    } catch (err) {
      setError(err.message);
      setStage("idle");
    }
  }

  return (
    <div className="console">
      <header className="console-header">
        <div className="console-brand">
          <span className="console-brand-mark">›_</span>
          <span>AI SQL Assistant</span>
        </div>
        <nav className="console-nav">
          <button
            className={`console-nav-tab ${tab === "dashboard" ? "console-nav-tab--active" : ""}`}
            onClick={() => setTab("dashboard")}
          >
            Dashboard
          </button>
          <button
            className={`console-nav-tab ${tab === "console" ? "console-nav-tab--active" : ""}`}
            onClick={() => setTab("console")}
          >
            Console
          </button>
          <button
            className={`console-nav-tab ${tab === "history" ? "console-nav-tab--active" : ""}`}
            onClick={() => setTab("history")}
          >
            History
          </button>
          <button
            className={`console-nav-tab ${tab === "analytics" ? "console-nav-tab--active" : ""}`}
            onClick={() => setTab("analytics")}
          >
            Analytics
          </button>
          {auth.role === "ADMIN" && (
            <button
              className={`console-nav-tab ${tab === "admin" ? "console-nav-tab--active" : ""}`}
              onClick={() => setTab("admin")}
            >
              Admin
            </button>
          )}
        </nav>
        <div className="console-user">
          <span>{auth.email}</span>
          <span className="console-role">{auth.role}</span>
          <button className="console-logout" onClick={logout}>Log out</button>
        </div>
      </header>

      <main className="console-main">
        {tab === "dashboard" ? (
          <Dashboard
            onOpenConsole={() => setTab("console")}
            onOpenHistory={() => setTab("history")}
            onOpenAnalytics={() => setTab("analytics")}
          />
        ) : tab === "history" ? (
          <History />
        ) : tab === "analytics" ? (
          <Analytics />
        ) : tab === "admin" ? (
          <Admin />
        ) : (
        <>
        <form
          className="console-input-row"
          onSubmit={(e) => {
            e.preventDefault();
            runQuery();
          }}
        >
          <span className="console-prompt">❯</span>
          <input
            autoFocus
            className="console-input"
            placeholder="Ask a question about students, employees, or sales…"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
          />
          <button className="console-run" type="submit" disabled={stage !== "idle" && stage !== "done" && stage !== "rejected"}>
            Run
          </button>
          {voice.supported && (
            <button
              type="button"
              className={`console-mic ${voice.listening ? "console-mic--active" : ""}`}
              onClick={() => (voice.listening ? voice.stop() : voice.start())}
              title="Voice input"
              aria-label="Voice input"
            >
              {voice.listening ? "●" : "🎤"}
            </button>
          )}
        </form>

        <div className="console-suggestions">
          {SUGGESTIONS.map((s) => (
            <button key={s} type="button" className="console-chip" onClick={() => { setQuestion(s); runQuery(s); }}>
              {s}
            </button>
          ))}
        </div>

        {stage !== "idle" && (
          <div className="console-panel">
            <PipelineStepper stage={stage} />
          </div>
        )}

        {error && <div className="console-panel console-error">{error}</div>}

        {sql && (
          <div className="console-panel">
            <div className="console-panel-label">Generated SQL</div>
            <pre className="console-sql">{sql}</pre>
            {explanation && <p className="console-explanation">{explanation}</p>}
          </div>
        )}

        {stage === "rejected" && rejectionReason && (
          <div className="console-panel console-error">
            <div className="console-panel-label">Rejected by Validation Agent</div>
            <p>{rejectionReason}</p>
          </div>
        )}

        {rows && (
          <div className="console-panel">
            <div className="console-panel-row">
              <div className="console-panel-label">Results ({rows.length} row{rows.length === 1 ? "" : "s"})</div>
              <div className="console-export-buttons">
                <button
                  type="button"
                  className="console-chip"
                  onClick={() => exportCsv(rows)}
                  disabled={rows.length === 0}
                >
                  Export CSV
                </button>
                <button
                  type="button"
                  className="console-chip"
                  onClick={() => exportReport({ question, sql, explanation, insight, suggestions, rows })}
                >
                  Download report
                </button>
              </div>
            </div>
            <ResultTable rows={rows} />
          </div>
        )}

        {costInfo && (costInfo.estimatedRowsScanned != null || costInfo.optimizationTips.length > 0) && (
          <div className="console-panel">
            <div className="console-panel-label">Query cost & optimization</div>
            {costInfo.estimatedRowsScanned != null && (
              <p className="console-explanation">
                MySQL's plan estimates scanning <strong>{costInfo.estimatedRowsScanned}</strong> row(s)
                {costInfo.accessType && <> (access type: <code>{costInfo.accessType}</code>)</>}.
              </p>
            )}
            {costInfo.optimizationTips.length > 0 && (
              <ul className="console-tips">
                {costInfo.optimizationTips.map((tip, i) => (
                  <li key={i}>{tip}</li>
                ))}
              </ul>
            )}
          </div>
        )}

        {insight && (
          <div className="console-panel console-insight">
            <div className="console-panel-label">Insight</div>
            <p>{insight}</p>
            {suggestions.length > 0 && (
              <div className="console-suggestions">
                {suggestions.map((s) => (
                  <button key={s} type="button" className="console-chip" onClick={() => { setQuestion(s); runQuery(s); }}>
                    {s}
                  </button>
                ))}
              </div>
            )}
          </div>
        )}
        </>
        )}
      </main>
    </div>
  );
}
