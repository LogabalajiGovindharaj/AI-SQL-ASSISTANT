// Load test for the AI SQL Assistant backend.
// Run with: k6 run load-tests/query-pipeline.js
// Requires k6 (https://k6.io) installed locally, and a running backend.
//
// Env vars:
//   BASE_URL   default http://localhost:8080
//   TOKEN      a valid JWT (log in first and paste the token)

import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const TOKEN = __ENV.TOKEN;

export const options = {
  scenarios: {
    steady_load: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: "30s", target: 10 },
        { duration: "1m", target: 10 },
        { duration: "30s", target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ["p(95)<3000"], // 95% of requests under 3s (LLM calls are the bottleneck)
    http_req_failed: ["rate<0.05"],
  },
};

const QUESTIONS = [
  "Show students with CGPA above 8",
  "Show highest paid employees",
  "Show total sales region wise",
  "Show average student marks",
];

export default function () {
  if (!TOKEN) {
    throw new Error("Set TOKEN env var to a valid JWT before running this test.");
  }

  const headers = {
    "Content-Type": "application/json",
    Authorization: `Bearer ${TOKEN}`,
  };

  const question = QUESTIONS[Math.floor(Math.random() * QUESTIONS.length)];

  const genRes = http.post(
    `${BASE_URL}/generate-query`,
    JSON.stringify({ question }),
    { headers }
  );
  check(genRes, {
    "generate-query status 200": (r) => r.status === 200,
  });

  if (genRes.status === 200) {
    const body = JSON.parse(genRes.body);
    if (body.valid) {
      const execRes = http.post(
        `${BASE_URL}/execute-query`,
        JSON.stringify({ sql: body.sql, question }),
        { headers }
      );
      check(execRes, {
        "execute-query status 200": (r) => r.status === 200,
      });
    }
  }

  sleep(1);
}
