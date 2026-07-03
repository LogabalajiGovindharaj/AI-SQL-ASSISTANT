import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./Dashboard.css";

const BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

async function get(path, token) {
  const res = await fetch(`${BASE_URL}${path}`, { headers: { Authorization: `Bearer ${token}` } });
  if (!res.ok) throw new Error(`Failed to load ${path}`);
  return res.json();
}

export default function Dashboard({ onOpenConsole, onOpenHistory, onOpenAnalytics }) {
  const { auth } = useAuth();
  const [stats, setStats] = useState(null);
  const [recent, setRecent] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([get("/analytics", auth.token), get("/history", auth.token)])
      .then(([analytics, history]) => {
        setStats(analytics);
        setRecent(history.slice(0, 5));
      })
      .catch((e) => setError(e.message));
  }, [auth.token]);

  return (
    <div className="dashboard">
      <h2 className="dashboard-greeting">Welcome back, {auth.email.split("@")[0]}.</h2>

      {error && <div className="history-error">{error}</div>}

      {stats && (
        <div className="analytics-stats">
          <div className="analytics-stat">
            <span className="analytics-stat-value">{stats.totalQueries}</span>
            <span className="analytics-stat-label">Total queries</span>
          </div>
          <div className="analytics-stat">
            <span className="analytics-stat-value analytics-stat-value--good">{stats.validQueries}</span>
            <span className="analytics-stat-label">Valid</span>
          </div>
          <div className="analytics-stat">
            <span className="analytics-stat-value analytics-stat-value--bad">{stats.rejectedQueries}</span>
            <span className="analytics-stat-label">Rejected</span>
          </div>
        </div>
      )}

      <div className="dashboard-actions">
        <button className="console-run" onClick={onOpenConsole}>Ask a question</button>
        <button className="console-chip" onClick={onOpenHistory}>View history</button>
        <button className="console-chip" onClick={onOpenAnalytics}>View analytics</button>
      </div>

      {recent.length > 0 && (
        <div className="console-panel">
          <div className="console-panel-label">Recent queries</div>
          {recent.map((r) => (
            <div key={r.id} className="dashboard-recent-item">
              <span>{r.question}</span>
              <span className={r.valid ? "dashboard-badge dashboard-badge--ok" : "dashboard-badge dashboard-badge--bad"}>
                {r.valid ? "valid" : "rejected"}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
