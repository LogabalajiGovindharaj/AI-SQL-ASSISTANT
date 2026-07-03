import React from "react";
import "./ResultTable.css";

export default function ResultTable({ rows }) {
  if (!rows || rows.length === 0) {
    return <p className="result-empty">No rows returned.</p>;
  }

  const columns = Object.keys(rows[0]);

  return (
    <div className="result-table-wrap">
      <table className="result-table">
        <thead>
          <tr>
            {columns.map((col) => (
              <th key={col}>{col}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, i) => (
            <tr key={i}>
              {columns.map((col) => (
                <td key={col}>{String(row[col])}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
