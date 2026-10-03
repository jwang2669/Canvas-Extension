# Frontend API Guide — Oil on Canvas Backend

This document is for **frontend developers**. It describes the backend API you can call from the Chrome extension and what you should implement on the frontend.

**Base URL (local dev):** `http://localhost:8080`  
**All API paths are under:** `/api/...`  
**CORS:** Backend allows all origins for now (extension can call from any origin).

**Current mode:** For testing, request bodies (e.g. tasks for workload, prompt for suggestions) are typed manually. Later we will use the Canvas API (after OAuth) so the extension can send data from the student’s Canvas page.

---

## Quick checklist

| #   | Backend endpoint          | Frontend to implement                                                                                                                                              |
| --- | ------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 1   | `GET /api/health`         | Optional: ping to check if backend is up before calling other APIs.                                                                                                |
| 2   | `POST /api/suggestions`   | UI: input for user question → send `prompt` → show `recommendation` (AI tip/suggestion).                                                                           |
| 3   | `POST /api/workload`      | UI: send current week’s tasks (title, dueDate, estimatedMinutes) → show per-day minutes, total, and AI `summary` (e.g. “Tue/Thu look heavy, start Project early”). |
| 4   | `POST /api/sessions`      | Placeholder only: implement when backend adds real session logic.                                                                                                  |
| 5   | `POST /api/tasks`         | Placeholder only: implement when backend adds task persistence.                                                                                                    |
| 6   | `GET /api/tasks?week=...` | Placeholder only: implement when backend returns real task list.                                                                                                   |
| 7   | `POST /api/analyze`       | Placeholder only: implement when backend adds analyze logic.                                                                                                       |
| 8   | `POST /api/recommend`     | Placeholder only: implement when backend adds recommend logic.                                                                                                     |

**Priority for now:** **#2 (suggestions)** and **#3 (workload)** are fully implemented and ready to integrate.

---

## 1. Health check

- **Method:** `GET`
- **URL:** `http://localhost:8080/api/health`
- **Request body:** none
- **Response (200):**
  ```json
  { "status": "up", "application": "oil-on-canvas-backend" }
  ```
- **Frontend:** Optional. Use to show “Backend connected” or disable AI features when backend is down.

---

## 2. Suggestions (AI free-form prompt)

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/suggestions`
- **Headers:** `Content-Type: application/json`
- **Request body:**
  ```json
  { "prompt": "Give me one short study tip." }
  ```
- **Response (200):**
  ```json
  { "recommendation": "Break your study into 25-minute Pomodoro blocks to stay focused." }
  ```
- **Frontend to implement:**
  - A text input (or button with preset prompts).
  - On submit, `POST` the body above with the user’s (or preset) `prompt`.
  - Display `response.recommendation` in the UI.

---

## 3. Workload (scoring + AI summary)

- **Method:** `POST`
- **URL:** `http://localhost:8080/api/workload`
- **Headers:** `Content-Type: application/json`
- **Request body:**

  ```json
  {
    "week": "2026-W10",
    "referenceDate": "2026-03-02",
    "tasks": [
      { "title": "Essay 1", "dueDate": "2026-03-04", "estimatedMinutes": 90 },
      { "title": "Quiz A", "dueDate": "2026-03-05", "estimatedMinutes": 30 },
      { "title": "Project draft", "dueDate": "2026-03-07", "estimatedMinutes": 120 }
    ]
  }
  ```

  - **`tasks`** (required): array of `{ title, dueDate, estimatedMinutes }`.
  - **`dueDate`**: ISO date string (e.g. `"2026-03-05"`).
  - **`week`** (optional): e.g. `"2026-W10"` for display.
  - **`referenceDate`** (optional): “today” for weighting; omit to use server’s today.

- **Response (200):**
  ```json
  {
    "perDayMinutes": {
      "Monday": 90,
      "Tuesday": 30,
      "Wednesday": 0,
      "Thursday": 120,
      "Friday": 0,
      "Saturday": 0,
      "Sunday": 0
    },
    "totalMinutes": 240,
    "weightedPerDay": {
      "Monday": 135.0,
      "Tuesday": 45.0,
      "Wednesday": 0.0,
      "Thursday": 180.0,
      "Friday": 0.0,
      "Saturday": 0.0,
      "Sunday": 0.0
    },
    "summary": "Tuesday and Thursday look heavy this week. Start the Project draft early so Thursday is manageable."
  }
  ```
- **Frontend to implement:**
  - Collect the current week’s tasks (from Canvas data or user input) with at least: **title**, **due date**, **estimated minutes**.
  - Build the request body and `POST` to `/api/workload`.
  - Show:
    - **perDayMinutes** or **weightedPerDay** (e.g. bar chart or list by day).
    - **totalMinutes** for the week.
    - **summary** as the AI-generated tip (e.g. in a card or tooltip).

---

## 4. Placeholder endpoints (not ready for real data)

These return empty or placeholder JSON. Implement UI only when the backend adds real logic.

- **`POST /api/sessions`** — body: `{}` (or any). Response: placeholder.
- **`POST /api/tasks`** — body: `{}`. Response: placeholder.
- **`GET /api/tasks?week=2026-W10`** — response: `[]`.
- **`POST /api/analyze`** — body: `{}`. Response: placeholder.
- **`POST /api/recommend`** — body: `{}`. Response: placeholder.

---

## Error handling

- **4xx/5xx:** Backend may return JSON like `{ "timestamp", "status", "error", "path" }`.
- **Suggestions / Workload:** If the OpenAI key is missing or the request fails, the backend still returns 200 with a fallback message in `recommendation` or `summary` (e.g. “Suggestion unavailable. Check OPENAI_API_KEY and network.”). Frontend can treat these as “no AI” and still show the rest of the data (e.g. workload numbers without summary).

---

## Summary for frontend

1. **Implement first:** Call **`POST /api/suggestions`** (user prompt → show recommendation) and **`POST /api/workload`** (tasks with due date + estimated minutes → show per-day, total, and AI summary).
2. **Data you need for workload:** For each task: `title`, `dueDate` (ISO string), `estimatedMinutes`.
3. **Base URL:** `http://localhost:8080`; ensure the extension has permission to call it (e.g. `host_permissions` in manifest).
4. **Placeholder endpoints:** Sessions, tasks, analyze, recommend — wire later when backend implements them.

For more detail on what the backend has implemented, see [BACKEND_IMPLEMENTATION.md](BACKEND_IMPLEMENTATION.md).

**Next step (Canvas Assistant):** The goal is to stop sending manually typed payloads and instead get assignment data from the student’s Canvas page (Canvas API or page detection). See [Canvas Assistant flow](CANVAS_ASSISTANT_FLOW.md) and [Canvas API guide](CANVAS_API_GUIDE.md) for the target flow and which Canvas endpoints to use (Due Date, Points, Questions, Available until, Time limit, Allowed attempts, etc.).
