import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./History.css";

async function fetchHistory(token) {
  const base = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";
  const res = await fetch(`${base}/history`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("Failed to load history");
  return res.json();
}

export default function History() {
  const { auth } = useAuth();
  const [entries, setEntries] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchHistory(auth.token).then(setEntries).catch((e) => setError(e.message));
  }, [auth.token]);

  return (
    <div className="history-list">
      {error && <div className="history-error">{error}</div>}
      {entries.length === 0 && !error && <p className="history-empty">No queries yet.</p>}
      {entries.map((e) => (
        <div key={e.id} className={`history-item ${e.valid ? "" : "history-item--rejected"}`}>
          <div className="history-question">{e.question}</div>
          {e.sql && <pre className="history-sql">{e.sql}</pre>}
          {!e.valid && <div className="history-reason">Rejected: {e.rejectionReason}</div>}
          <div className="history-time">{new Date(e.createdAt).toLocaleString()}</div>
        </div>
      ))}
    </div>
  );
}
