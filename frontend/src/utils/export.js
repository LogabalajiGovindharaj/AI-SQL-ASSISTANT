function triggerDownload(filename, content, mimeType) {
  const blob = new Blob([content], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

function toCsv(rows) {
  if (!rows || rows.length === 0) return "";
  const columns = Object.keys(rows[0]);
  const escape = (val) => {
    const s = String(val ?? "");
    return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
  };
  const lines = [columns.join(",")];
  rows.forEach((row) => lines.push(columns.map((c) => escape(row[c])).join(",")));
  return lines.join("\n");
}

export function exportCsv(rows, filenamePrefix = "query-results") {
  const csv = toCsv(rows);
  triggerDownload(`${filenamePrefix}-${Date.now()}.csv`, csv, "text/csv;charset=utf-8;");
}

export function exportReport({ question, sql, explanation, insight, suggestions, rows }) {
  const lines = [
    "AI SQL Assistant — Query Report",
    `Generated: ${new Date().toLocaleString()}`,
    "",
    "Question:",
    question || "(none)",
    "",
    "Generated SQL:",
    sql || "(none)",
    "",
    "Explanation:",
    explanation || "(none)",
    "",
    "Insight:",
    insight || "(none)",
    "",
    "Suggested follow-ups:",
    ...(suggestions && suggestions.length ? suggestions.map((s) => `- ${s}`) : ["(none)"]),
    "",
    `Results (${rows ? rows.length : 0} rows):`,
    toCsv(rows) || "(no rows)",
  ];
  triggerDownload(`query-report-${Date.now()}.txt`, lines.join("\n"), "text/plain;charset=utf-8;");
}
