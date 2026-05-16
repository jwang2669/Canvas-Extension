# Oil on Canvas — Backend

Spring Boot backend for the Oil on Canvas browser extension. It will power Canvas-related features (e.g. assignment organization, task management) and a future **AI assistant** for students using Canvas.

This document describes how to run the server, the project layout, and the intended architecture as we add features.

---

## Tech Stack

| Layer     | Technology |
| --------- | ---------- |
| Runtime   | Java       |
| Framework | Spring     |
| Build     | Gradle     |
| Database  | MySQL      |

---

## How to Run the Backend Server

### Prerequisites (to run locally)

- **Java 17** or newer (JDK 17–25 supported; the project targets Java 17). The project includes the **Gradle wrapper** (`gradlew` + `gradle-wrapper.jar`), so you do **not** need to install Gradle.
- If you see **"Unsupported class file major version 69"** (or similar), your JDK is newer than what the current Gradle supports; we use Gradle 9.1, which supports up to Java 25. Upgrade the wrapper or use JDK 17/21 if issues persist.

### Option 1: From monorepo root

From the **repository root** (recommended in a monorepo):

```bash
npm run backend:run
```

This runs `./run.sh` in `apps/backend`, which loads `apps/backend/.env` (if present) then starts the server.

### Option 2: From apps/backend

```bash
cd apps/backend
./run.sh
```

Or without env file: `./gradlew bootRun`.

### Prerequisites (to run thru VM)

- ssh into our team's Ubuntu VM
  - `ssh {your cs user}@cs506x15.cs.wisc.edu`
- clone our project repo into a folder of your choice
- cd to infra folder, this is where our docker compose file lives
- create a .env file here
- copy the contents of .env.example to your .env file
  - make sure to replace the password and key placeholders with actual values

### Start the Containers

Starting the containers should start the backend server, so you won't need to run npm run or any other commands. You can just start the containers and start working on the code as needed.

- in infra, run:
  `docker compose up -d --build`

### Stopping the Containers

- in infra, run:
  `docker compose down`

### To View Logs

- in infra, run:
  `docker compose logs`

### Verify It’s Running

- Server listens on **port 8080** by default.
- Open: [http://localhost:8080/api/health](http://localhost:8080/api/health)
- Expected response:

```json
{
  "status": "up",
  "application": "oil-on-canvas-backend"
}
```

---

## OpenAI API (suggestions)

The backend calls the **OpenAI Chat Completions API** for `POST /api/suggestions`.

- **When running via Docker (VM or local):** Set `OPENAI_API_KEY` in **`infra/.env`** (copy from `infra/.env.example`). Docker Compose passes it into the backend container.
- **When running locally without Docker:** In `apps/backend`, copy `.env.example` to `.env` and set `OPENAI_API_KEY`. When you run `./run.sh` or `npm run backend:run`, that file is loaded automatically.
- **Alternative**: Export in the shell before running: `export OPENAI_API_KEY=your_key_here`.
- **Endpoint**: `POST /api/suggestions` with body `{ "prompt": "your question" }`. Response: `{ "recommendation": "..." }`.
- **Model**: `openai.api.model` (default: `gpt-4o-mini`). Layers: `SuggestionController` → `SuggestionService` → `OpenAIClient` (WebClient).
- If the key is missing or the request fails, the API returns a short fallback message.

**Test the API key:** With the server running (`npm run backend:run`), call the suggestions endpoint:

```bash
curl -X POST http://localhost:8080/api/suggestions \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Give me one short study tip in one sentence."}'
```

If the key in `.env` is valid, you get `{"recommendation":"..."}` with AI text. If not, you get a fallback message.

---

## Canvas API (single-token dev setup)

The backend can talk to the **Canvas LMS REST API** using a single access token, for local development and testing before full OAuth is implemented.

- **Environment variables (via `infra/.env` or shell):**
  - `CANVAS_BASE_URL` — your Canvas base URL, e.g. `https://canvas.instructure.com` or your school domain (no trailing slash).
  - `CANVAS_ACCESS_TOKEN` — a Canvas access token for **your own account**, generated from the Canvas profile page (see official docs, “Manual Token Generation”).
    - Per Canvas API Policy, do **not** ask other users to paste tokens; this single-token mode is for dev/testing only.
- When both variables are non-empty, the backend registers:
  - `canvasWebClient` (in `WebClientConfig`) — `WebClient` preconfigured with `baseUrl=CANVAS_BASE_URL` and `Authorization: Bearer <token>`.
  - `CanvasApiClient` — outbound client that wraps common calls:
    - `GET /api/v1/users/self` → `CanvasUser`
    - `GET /api/v1/courses?enrollment_state=active&per_page=100` → `List<CanvasCourse>`
  - **HTTP:** `GET /api/canvas/courses` — actively enrolled courses (`{"courses":[...]}`). `GET /api/canvas/me` — token holder profile from `users/self` (`{"user":{...}}`). **503** if Canvas env vars are unset; **502** on `/me` if Canvas does not return a profile.
- When either variable is missing/blank, these beans are **not** created and the backend runs without Canvas integration.

This mode allows you to:

- Verify that the backend can reach your Canvas instance with the token.
- Develop and test future services that depend on real Canvas course/user data, while the OAuth2 + Developer Key flow is still being set up.

---

## Current API endpoints

| Method | Path                                   | Description                                                                                                                                                                                                               |
| ------ | -------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/health`                          | Health check; returns `{"status":"up","application":"..."}`.                                                                                                                                                              |
| POST   | `/api/sessions`                        | Start/resume session. Persists user and courses to DB, returns `sessionId`.                                                                                                                                               |
| POST   | `/api/tasks`                           | **DB.** Create task; saves to `todo_item` table.                                                                                                                                                                          |
| GET    | `/api/tasks?week=...`                  | **DB.** Get tasks for a week from `todo_item` table.                                                                                                                                                                      |
| POST   | `/api/analyze`                         | Analyze content (placeholder).                                                                                                                                                                                            |
| POST   | `/api/recommend`                       | Get “what to work on” recommendation (placeholder).                                                                                                                                                                       |
| POST   | `/api/suggestions`                     | **OpenAI.** Body `{"prompt":"...","sessionId?":"..."}`. Returns `{"recommendation":"..."}`. Includes courses and assignments in AI context.                                                                               |
| POST   | `/api/workload`                        | **OpenAI.** Body: `{"tasks":[{"title","dueDate","estimatedMinutes"}]}`. Returns `perDayMinutes`, `totalMinutes`, `weightedPerDay`, `summary`.                                                                             |
| GET    | `/api/canvas/courses`                  | **Canvas API.** Lists courses with active enrollment. Response `{"courses":[...]}`. **503** if not configured.                                                                                                            |
| GET    | `/api/canvas/me` or `/api/canvas/user` | **Canvas API.** Current user profile. Response `{"user":{...}}`. **503** if not configured; **502** if Canvas rejects.                                                                                                    |
| GET    | `/api/canvas/assignments`              | **DB.** Returns assignments from `assignment_cache` table. Query: `?week=current` (Mon-Sun this week), `?week=next` (next week), or omit for next 7 days. Response `{"assignments":[...],"startDate","endDate","count"}`. |

`/api/analyze` and `/api/recommend` are placeholders. All other endpoints are functional.

**Test workload:** With server running and `OPENAI_API_KEY` in `.env`:

```bash
curl -s -X POST http://localhost:8080/api/workload \
  -H "Content-Type: application/json" \
  -d '{"week":"2026-W10","tasks":[{"title":"Essay 1","dueDate":"2026-03-04","estimatedMinutes":90},{"title":"Quiz A","dueDate":"2026-03-05","estimatedMinutes":30},{"title":"Project draft","dueDate":"2026-03-07","estimatedMinutes":120}]}'
```

Expect JSON with `perDayMinutes`, `totalMinutes`, `weightedPerDay`, and `summary` (short AI text).

---

## Project Layout (Current)

MVP REST API (sessions, tasks, analyze, recommend, suggestions + health):

```
apps/backend/
├── .env.example   # Copy to .env and set OPENAI_API_KEY (do not commit .env)
├── run.sh        # Loads .env and runs the server
├── build.gradle
├── settings.gradle
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/com/oiloncanvas/backend/
│   │   │   ├── OilOnCanvasApplication.java   # Entry point
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java
│   │   │   │   └── WebClientConfig.java     # WebClient beans (OpenAI, Canvas)
│   │   │   ├── controller/
│   │   │   │   ├── HealthController.java    # GET /api/health
│   │   │   │   ├── SessionController.java   # POST /api/sessions
│   │   │   │   ├── TaskController.java      # POST /api/tasks, GET /api/tasks?week=...
│   │   │   │   ├── AnalyzeController.java   # POST /api/analyze
│   │   │   │   ├── RecommendController.java # POST /api/recommend
│   │   │   │   ├── SuggestionController.java # POST /api/suggestions (OpenAI)
│   │   │   │   ├── WorkloadController.java   # POST /api/workload (scoring + AI summary)
│   │   │   │   └── CanvasCourseController.java # GET /api/canvas/courses, /me, /assignments
│   │   │   ├── service/
│   │   │   │   ├── SessionService.java
│   │   │   │   ├── TaskService.java
│   │   │   │   ├── AnalyzeService.java
│   │   │   │   ├── RecommendService.java
│   │   │   │   ├── SuggestionService.java
│   │   │   │   ├── WorkloadService.java
│   │   │   │   └── CanvasCourseService.java
│   │   │   ├── client/
│   │   │   │   ├── OpenAIClient.java        # OpenAI Chat Completions
│   │   │   │   └── CanvasApiClient.java     # Canvas LMS API (single-token dev)
│   │   │   └── dto/
│   │   │       ├── HealthResponse.java
│   │   │       ├── SessionRequest.java, SessionResponse.java
│   │   │       ├── TaskRequest.java, TaskResponse.java
│   │   │       ├── AnalyzeRequest.java, AnalyzeResponse.java
│   │   │       ├── RecommendRequest.java, RecommendResponse.java
│   │   │       ├── SuggestionRequest.java, SuggestionResponse.java
│   │   │       ├── TaskItem.java, WorkloadRequest.java, WorkloadResponse.java
│   │   │       ├── AssignmentResponse.java, AssignmentsResponse.java
│   │   │       ├── EnrolledCoursesResponse.java
│   │   │       └── OpenAIChatRequest.java, OpenAIChatResponse.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
└── README.md (this file)
```

---

## Future Architecture (Planned)

```
controller/     →  REST API layer (HTTP, validation only)
service/        →  Business logic (e.g. suggestion generation, Canvas rules)
repository/     →  Data access (MySQL, optional caching)
client/         →  Outbound HTTP (e.g. OpenAI via WebClient, Canvas API)
config/         →  Beans (WebClient, security, etc.)
dto/            →  Request/response and external API DTOs
```

- **Controllers**: Thin; map HTTP to service calls and return DTOs. MVP paths: `/api/sessions`, `/api/tasks`, `/api/analyze`, `/api/recommend`.
- **Services**: Hold business logic. Controllers delegate to services; services may later use repositories and external clients.
- **DTOs**: All API request/response bodies are DTOs; entities are not exposed on the REST API.
- **Dependencies**: Prefer constructor injection.

Planned additions (not implemented yet):

- **Canvas integration**: Endpoints and services to support assignment/course data (e.g. for the extension and AI).
- **AI assistant**: Basic OpenAI integration is in place (`POST /api/suggestions`, `OpenAIClient`, `SuggestionService`). See `RESEARCH/SpringBootWebClient.md` for patterns.
- **MySQL**: Persistence for user preferences, saved state, etc., with repository layer and migrations.

---

## Configuration

- **Port**: Set `server.port` in `src/main/resources/application.properties` (default: `8080`).
- **OpenAI**: Set `OPENAI_API_KEY` in `apps/backend/.env` (copy from `.env.example`) or export it; optional for other endpoints.
- **Canvas (single-token dev)**: Set `CANVAS_BASE_URL` and `CANVAS_ACCESS_TOKEN` (via `infra/.env` or shell). If both are set, `CanvasApiClient` is available and uses that token for Canvas API calls.
- **Environment-specific settings**: Use `application-{profile}.properties` or environment variables; avoid hardcoding secrets.

---

## Related Docs

- [Main project README](../../README.md)
- [Backend implementation summary](../../docs/BACKEND_IMPLEMENTATION.md) — what’s implemented
- [Frontend API guide](../../docs/FRONTEND_API_GUIDE.md) — for frontend devs (endpoints, request/response)
- [Style Guide & Conventions](../../STYLE.md)
- [Spring Boot & WebClient research](../../RESEARCH/SpringBootWebClient.md) — reference for AI/OpenAI integration
