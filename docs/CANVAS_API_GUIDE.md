# Canvas LMS API — How to Use It for the Assistant

This guide is for the team implementing the **Canvas AI Assistant**. It explains how to use the [Canvas LMS API](https://developerdocs.instructure.com/services/canvas) so that assignment data (Due Date, Points, Questions, Available until, Time limit, Allowed attempts, etc.) comes from Canvas instead of being typed by hand.

---

## 1. Overview and auth

- **Base:** All access is over **HTTPS** to the school’s Canvas domain (e.g. `https://canvas.school.edu`).
- **Docs:** [Canvas LMS API](https://developerdocs.instructure.com/services/canvas)
- **Auth:** [OAuth2](https://developerdocs.instructure.com/services/canvas/oauth2/file.oauth). Send the access token in the header:  
  `Authorization: Bearer <ACCESS_TOKEN>`
- **Responses:** JSON. Timestamps in ISO 8601 UTC (e.g. `YYYY-MM-DDTHH:MM:SSZ`).
- **Policy:** [Canvas API Policy](https://www.instructure.com/policies/api-policy) — read before production use.

For the **Chrome extension**, the flow is: user logs in to Canvas (or we use OAuth); extension stores an access token; extension (or our backend with the token) calls Canvas API to get courses and assignments. So “student is on Canvas” can mean “we have their token and the current course/assignment ID from the URL or page.”

---

## 2. Relevant endpoints for the assistant

These endpoints give the data you need for **assignment summary, difficulty, and Q&A** (Due Date, Points, Questions, Available until, Time limit, Allowed attempts, etc.).

### Courses

- **List courses (current user):**  
  `GET /api/v1/courses`  
  Returns courses the user is in. Use to show “which course” and to call assignment/quiz endpoints with `course_id`.

- **Single course:**  
  `GET /api/v1/courses/:id`  
  Course name and metadata.

### Assignments (essays, submissions, etc.)

- **List assignments in a course:**  
  `GET /api/v1/courses/:course_id/assignments`  
  Use for workload: get all assignments, then map to “tasks” (title, due date, estimated minutes).  
  Supports `bucket=future|past|undated` and date filters to get “this week.”

- **Single assignment (detail):**  
  `GET /api/v1/courses/:course_id/assignments/:id`  
  Use when the student is on one assignment page. You get one assignment object with fields below.

**Assignment object fields we care about:**

| Our need         | Canvas field              | Notes                                                        |
| ---------------- | ------------------------- | ------------------------------------------------------------ |
| Due date         | `due_at`                  | ISO 8601. Can be null; also check `all_dates` for overrides. |
| Points           | `points_possible`         | Total points.                                                |
| Title            | `name`                    | Assignment title.                                            |
| Description      | `description`             | HTML; use for summary/Q&A (sanitize or strip HTML).          |
| Available from   | `unlock_at`               | When it becomes available (optional).                        |
| Available until  | `lock_at`                 | When it locks (optional).                                    |
| Submission types | `submission_types`        | e.g. online_upload, on_paper.                                |
| Allowed attempts | N/A for plain assignments | Usually 1; for “attempts” we use Quizzes API.                |

So for **Due Date, Points, Available until**, and **assignment summary/Q&A**, the Assignments API is the main source. For **Questions, Time limit, Allowed attempts**, use the Quizzes API below when the item is a quiz.

### Quizzes (time limit, attempts, questions)

- **List quizzes:**  
  `GET /api/v1/courses/:course_id/quizzes`

- **Single quiz (detail):**  
  `GET /api/v1/courses/:course_id/quizzes/:id`  
  Use when the student is on a quiz page.

**Quiz object fields we care about:**

| Our need         | Canvas field                      | Notes                                                               |
| ---------------- | --------------------------------- | ------------------------------------------------------------------- |
| Due date         | `due_at`                          | Same as assignments.                                                |
| Available until  | `lock_at`                         | When it locks.                                                      |
| Available from   | `unlock_at`                       | When it unlocks.                                                    |
| Time limit       | `time_limit`                      | Minutes (Classic Quizzes). New Quizzes may use different structure. |
| Allowed attempts | `allowed_attempts`                | -1 = unlimited.                                                     |
| Questions        | `question_count`                  | Number of questions.                                                |
| Points           | From quiz questions or assignment | May need to sum or use linked assignment.                           |

So for **Time limit, Allowed attempts, Questions**, use the Quizzes API when the current page is a quiz.

### Optional: assignment groups

- **Assignment groups (with assignments):**  
  `GET /api/v1/courses/:course_id/assignment_groups?include[]=assignments`  
  Alternative way to get assignments grouped (e.g. by “Homework”, “Exams”). Use if you want to show workload by group.

---

## 3. How to use this in the assistant

- **“User opens an assignment on Canvas”**
  - From the URL or page, get `course_id` and `assignment_id` (or `quiz_id`).
  - Call `GET /api/v1/courses/:course_id/assignments/:id` or `GET /api/v1/courses/:course_id/quizzes/:id`.
  - From the response, build a small “assignment context” object: due date, points, description (or first N chars), time limit (quiz), allowed attempts (quiz), question count (quiz), unlock_at, lock_at.

- **Workload (weekly view)**
  - Call `GET /api/v1/courses/:course_id/assignments` (and optionally quizzes) for the course(s) the user cares about.
  - Filter by due date for “this week.”
  - Map each item to `{ title, dueDate, estimatedMinutes }` (estimate minutes from points or type).
  - Send that list to our backend `POST /api/workload` — no change to our API.

- **Assignment summary / difficulty / Q&A**
  - Send the same “assignment context” (and optionally description) to our backend.
  - Backend can use `POST /api/suggestions` with a prompt like: “Assignment: [name]. Due: [due_at]. Points: [points_possible]. Time limit: [time_limit] min. Attempts: [allowed_attempts]. Description: [snippet]. User question: [user input]. Answer in 1–2 sentences.”
  - Summary and difficulty can be separate prompts or one structured prompt; same idea: feed Canvas fields into the prompt.

- **Page detection / scraping**
  - If you don’t have OAuth yet or want a fallback, you can detect on the page: Due Date, Points, “Available until”, “Time limit”, “Allowed attempts”, etc., and build the same “assignment context” object.
  - Prefer Canvas API when possible so you get one source of truth and fewer breakages when Canvas changes the UI.

---

## 4. Summary table: “Where do I get X?”

| Data                               | Prefer                                                      | Alternative              |
| ---------------------------------- | ----------------------------------------------------------- | ------------------------ |
| Due date                           | Assignments/Quizzes API: `due_at`                           | Page: “Due” / “Due date” |
| Points                             | Assignments API: `points_possible`                          | Page: “Points”           |
| Questions                          | Quizzes API: `question_count`                               | Page: “X Questions”      |
| Available until                    | Assignments/Quizzes API: `lock_at`                          | Page: “Available until”  |
| Time limit                         | Quizzes API: `time_limit`                                   | Page: “Time limit”       |
| Allowed attempts                   | Quizzes API: `allowed_attempts`                             | Page: “Allowed attempts” |
| Description                        | Assignments API: `description`                              | Page body (HTML)         |
| List of assignments (for workload) | `GET /api/v1/courses/:id/assignments` (+ quizzes if needed) | —                        |

---

## 5. References

- [Canvas LMS API (Instructure)](https://developerdocs.instructure.com/services/canvas)
- [OAuth2](https://developerdocs.instructure.com/services/canvas/oauth2/file.oauth)
- [Canvas API Policy](https://www.instructure.com/policies/api-policy)
- Project flow: [CANVAS_ASSISTANT_FLOW.md](CANVAS_ASSISTANT_FLOW.md)
- Backend endpoints: [FRONTEND_API_GUIDE.md](FRONTEND_API_GUIDE.md)
