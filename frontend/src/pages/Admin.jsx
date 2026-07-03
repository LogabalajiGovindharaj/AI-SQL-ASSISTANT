import React, { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import "./Admin.css";

const BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

export default function Admin() {
  const { auth } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState("");

  useEffect(() => {
    if (auth.role !== "ADMIN") return;
    fetch(`${BASE_URL}/admin/users`, { headers: { Authorization: `Bearer ${auth.token}` } })
      .then((res) => {
        if (!res.ok) throw new Error(`Failed to load users (${res.status})`);
        return res.json();
      })
      .then(setUsers)
      .catch((e) => setError(e.message));
  }, [auth.token, auth.role]);

  if (auth.role !== "ADMIN") {
    return <p className="history-empty">Admin access required. Your account role: {auth.role}.</p>;
  }

  return (
    <div className="admin">
      {error && <div className="history-error">{error}</div>}
      <div className="result-table-wrap">
        <table className="result-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td>
                  <span className={u.role === "ADMIN" ? "dashboard-badge dashboard-badge--ok" : "dashboard-badge"}>
                    {u.role}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
