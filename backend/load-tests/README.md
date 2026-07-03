# Load test

Uses [k6](https://k6.io/docs/get-started/installation/).

```bash
# 1. Get a token
curl -s -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","password":"yourpassword"}'

# 2. Run the test
TOKEN=<paste token> k6 run load-tests/query-pipeline.js
```

Ramps 0 → 10 concurrent virtual users over 30s, holds for 1 minute, ramps
down. Fails the run if p95 latency exceeds 3s or the error rate exceeds 5%
(the Claude API round-trip is the expected bottleneck, not the backend
itself).
