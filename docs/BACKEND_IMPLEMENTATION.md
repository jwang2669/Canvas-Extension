# Backend Implementation Summary

This document describes what is currently implemented in the Oil on Canvas backend (as of the last update). Use it for handoff, onboarding, or integration planning.

**Current mode:** Request data (e.g. tasks for workload, prompt for suggestions) is entered manually for testing. We plan to use the Canvas API (after OAuth) so the extension can send data from the user’s Canvas context instead.

---

## Tech Stack

- **Runtime:** Java 17+
- **Framework:** Spring Boot 3.2
- **Build:** Gradle 9.1
- **Outbound HTTP:** Spring WebClient (OpenAI)

---

## Implemented Features

### 1. Health check

- **Endpoint:** `GET /api/health`
- **Purpose:** Server availability and name.
- **Response:** `{ "status": "up", "application": "oil-on-canvas-backend" }`

### 2. OpenAI integration

- **Config:** `OPENAI_API_KEY` in `apps/backend/.env` (copy from `.env.example`). Loaded when running via `./run.sh` or `npm run backend:run`.
- **Usage:** Used by suggestions and workload summary.

### 3. Suggestions (OpenAI)

- **Endpoint:** `POST /api/suggestions`
- **Purpose:** Send a free-form prompt and get an AI-generated text suggestion.
- **Request:** `{ "prompt": "string" }`
- **Response:** `{ "recommendation": "string" }`
- **Layers:** `SuggestionController` → `SuggestionService` → `OpenAIClient` (WebClient to OpenAI Chat Completions).

### 4. Workload scoring + AI summary

- **Endpoint:** `POST /api/workload`
- **Purpose:** For a given list of tasks (with due date and estimated minutes), compute:
  - Total estimated minutes per day of week and for the week.
  - Due-date proximity weighting (tasks due sooner count more).
  - A short natural-language summary via OpenAI (e.g. “Tue/Thu look heavy, start Project early”).
- **Request:**  
  `{ "tasks": [ { "title", "dueDate", "estimatedMinutes" } ], "week"?: string, "referenceDate"?: string }`
  - `dueDate`: ISO date (e.g. `"2026-03-05"`).
  - `referenceDate`: optional; if omitted, “today” is used for proximity weighting.
  - **Note:** In the final flow, `tasks` will be derived from Canvas (Canvas API or page detection), not typed by hand. See [Canvas Assistant flow](CANVAS_ASSISTANT_FLOW.md) and [Canvas API guide](CANVAS_API_GUIDE.md).
- **Response:**  
  `{ "perDayMinutes": { "Monday": 90, ... }, "totalMinutes": number, "weightedPerDay": { "Monday": 135.0, ... }, "summary": "string" }`
- **Layers:** `WorkloadController` → `WorkloadService` (scoring + prompt) → `OpenAIClient` for summary.

### 5. Placeholder endpoints (no business logic yet)

These return placeholder or empty DTOs; no database or real logic:

- `POST /api/sessions`
- `POST /api/tasks` — create task
- `GET /api/tasks?week=...` — get tasks for a week
- `POST /api/analyze`
- `POST /api/recommend`

---

## Project layout (relevant parts)

```
apps/backend/
├── .env.example, .env (gitignored), run.sh
├── src/main/java/com/oiloncanvas/backend/
│   ├── OilOnCanvasApplication.java
│   ├── config/          (CorsConfig, WebClientConfig)
│   ├── controller/      (Health, Session, Task, Analyze, Recommend, Suggestion, Workload)
│   ├── service/         (Session, Task, Analyze, Recommend, Suggestion, Workload)
│   ├── client/          (OpenAIClient)
│   └── dto/             (HealthResponse, TaskItem, WorkloadRequest/Response, SuggestionRequest/Response, …)
└── src/main/resources/application.properties
```

---

## Running the backend locally

- From repo root: `npm run backend:run` (uses `apps/backend/run.sh`, loads `.env`).
- From `apps/backend`: `./run.sh` or `./gradlew bootRun`.
- Default port: **8080**. Override with `SERVER_PORT=8081` if needed.

---

## Running the backend on VM

You'll need to have your .env file in infra. See apps/backend/README.md for more details on starting containers/running backend.

- From infra: `docker compose up -d --build`

---

## Related docs

- [Frontend API guide](FRONTEND_API_GUIDE.md) — for frontend developers (endpoints, request/response, what to implement).
- [Canvas Assistant flow](CANVAS_ASSISTANT_FLOW.md) — target flow (data from Canvas API / page, not manual).
- [Canvas API guide](CANVAS_API_GUIDE.md) — how to use Canvas LMS API (OAuth, assignments, quizzes).
- [apps/backend/README.md](../apps/backend/README.md) — run instructions, OpenAI setup, project layout.
