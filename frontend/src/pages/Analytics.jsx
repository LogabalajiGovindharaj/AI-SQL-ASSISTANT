import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./Analytics.css";

const BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

async function fetchAnalytics(token, scope) {
  const path = scope === "all" ? "/admin/analytics" : "/analytics";
  const res = await fetch(`${BASE_URL}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error(`Failed to load analytics (${res.status})`);
  return res.json();
}

export default function Analytics() {
  const { auth } = useAuth();
  const isAdmin = auth.role === "ADMIN";
  const [scope, setScope] = useState("mine");
  const [data, setData] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchAnalytics(auth.token, scope).then(setData).catch((e) => setError(e.message));
  }, [auth.token, scope]);

  const maxTableCount = data
    ? Math.max(1, ...Object.values(data.mostQueriedTables || {}))
    : 1;

  return (
    <div className="analytics">
      {isAdmin && (
        <div className="analytics-scope">
          <button
            className={`console-nav-tab ${scope === "mine" ? "console-nav-tab--active" : ""}`}
            onClick={() => setScope("mine")}
          >
            My usage
          </button>
          <button
            className={`console-nav-tab ${scope === "all" ? "console-nav-tab--active" : ""}`}
            onClick={() => setScope("all")}
          >
            All users (admin)
          </button>
        </div>
      )}

      {error && <div className="history-error">{error}</div>}

      {data && (
        <>
          <div className="analytics-stats">
            <div className="analytics-stat">
              <span className="analytics-stat-value">{data.totalQueries}</span>
              <span className="analytics-stat-label">Total queries</span>
            </div>
            <div className="analytics-stat">
              <span className="analytics-stat-value analytics-stat-value--good">{data.validQueries}</span>
              <span className="analytics-stat-label">Valid</span>
            </div>
            <div className="analytics-stat">
              <span className="analytics-stat-value analytics-stat-value--bad">{data.rejectedQueries}</span>
              <span className="analytics-stat-label">Rejected</span>
            </div>
          </div>

          <div className="console-panel">
            <div className="console-panel-label">Most-queried tables</div>
            <div className="analytics-bars">
              {Object.entries(data.mostQueriedTables || {}).map(([table, count]) => (
                <div key={table} className="analytics-bar-row">
                  <span className="analytics-bar-label">{table}</span>
                  <div className="analytics-bar-track">
                    <div
                      className="analytics-bar-fill"
                      style={{ width: `${(count / maxTableCount) * 100}%` }}
                    />
                  </div>
                  <span className="analytics-bar-count">{count}</span>
                </div>
              ))}
            </div>
          </div>

          {data.recentRejections && data.recentRejections.length > 0 && (
            <div className="console-panel">
              <div className="console-panel-label">Recent rejections</div>
              {data.recentRejections.map((r) => (
                <div key={r.id} className="analytics-rejection">
                  <div>{r.question}</div>
                  <div className="history-reason">{r.rejectionReason}</div>
                </div>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
