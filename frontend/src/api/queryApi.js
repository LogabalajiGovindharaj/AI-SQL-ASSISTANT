const BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

async function request(path, { method = "GET", body, token } = {}) {
  const headers = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const data = await res.json().catch(() => ({}));

  if (!res.ok) {
    throw new Error(data.error || `Request failed with status ${res.status}`);
  }
  return data;
}

export const authApi = {
  login: (email, password) => request("/login", { method: "POST", body: { email, password } }),
  signup: (name, email, password) => request("/signup", { method: "POST", body: { name, email, password } }),
};

export const queryApi = {
  generate: (question, token) =>
    request("/generate-query", { method: "POST", body: { question }, token }),
  execute: (sql, question, token) =>
    request("/execute-query", { method: "POST", body: { sql, question }, token }),
};
