# Canvas AI Assistant — Target Flow

This document describes the **intended user flow** and where data comes from. Right now the backend accepts manually typed request bodies; the goal is to **automatically** use data from the student’s Canvas screen (via Canvas API and/or page detection).

---

## Current vs target

|                       | Current                                                           | Target                                                                                                                                        |
| --------------------- | ----------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| **Data source**       | User (or tester) types JSON (e.g. tasks, prompt) in curl/Postman. | Student is on Canvas; extension/backend gets assignment data **from the page or Canvas API**.                                                 |
| **Workload**          | Manual `tasks` array.                                             | Tasks built from **Canvas assignments** (due date, points, etc.) or from **current page** when viewing an assignment.                         |
| **Suggestions / Q&A** | User sends a free-text `prompt`.                                  | User asks questions **in context of the assignment they’re viewing**; backend has assignment metadata (and optionally description) to answer. |

---

## Target flow (what to build toward)

1. **Student opens an assignment on Canvas**  
   (e.g. assignment detail page, quiz page).

2. **Extension / frontend gathers assignment context**
   - **Preferred:** Call [Canvas API](https://developerdocs.instructure.com/services/canvas) (with the user’s token) to get:
     - Course and assignment (or quiz) details: **due date, points, description, time limit, allowed attempts, available until**, etc.
   - **Fallback or supplement:** Detect visible elements on the page (e.g. Due Date, Points, “Available until”, “Time limit”, “Allowed attempts”) to fill in what the API doesn’t or to work before OAuth is set up.

3. **Send that context to our backend**
   - **Workload:** Send a list of “tasks” (title, due date, estimated minutes) — derived from Canvas assignments for the week.
   - **Assignment summary / difficulty / Q&A:** Send the current assignment’s metadata (and optionally description) so the backend can:
     - **Summarize** the assignment.
     - **Estimate difficulty** (e.g. from points, time limit, question count).
     - **Answer the user’s questions** about the assignment (user types a question; backend uses assignment context + OpenAI).

4. **Backend (already or to be added)**
   - **Workload:** Already accepts `POST /api/workload` with `tasks`; no change to contract. Frontend just stops “typing by hand” and instead builds `tasks` from Canvas.
   - **Analyze / summary / difficulty / Q&A:** Use `POST /api/suggestions` (or a dedicated analyze endpoint) with a prompt that includes assignment metadata (and optionally description) so the model can summarize, estimate difficulty, and answer questions.

So: **no new backend behavior was implemented here** — only this flow and the use of the Canvas API are documented. Implementation work is: **frontend/extension** (get data from Canvas API + optional scraping) and **backend** (analyze/summary/difficulty/Q&A using that data; can start from existing suggestions endpoint).

---

## Features to implement (aligned with this flow)

1. **Assignment summary**  
   Input: assignment metadata (and optionally description) from Canvas.  
   Output: short natural-language summary (e.g. via existing or new endpoint using OpenAI).

2. **Assignment difficulty**  
   Input: same metadata (points, time limit, question count, etc.).  
   Output: difficulty estimate (e.g. “Medium”, or a short explanation). Can be part of the same analyze/summary response.

3. **Q&A about the assignment**  
   Input: assignment context + user’s question.  
   Output: answer grounded in that assignment (e.g. “How many attempts do I have?” → “You have 2 attempts.”). Can use `POST /api/suggestions` with a prompt that includes assignment context + question.

4. **Where the data comes from**
   - Use [Canvas LMS API](https://developerdocs.instructure.com/services/canvas) where possible (courses, assignments, quizzes).
   - Optionally detect or scrape on-page elements (Due Date, Points, Available until, Time limit, Allowed attempts, etc.) when API is not available or to double-check.

See [CANVAS_API_GUIDE.md](CANVAS_API_GUIDE.md) for which Canvas endpoints to call and how they map to Due Date, Points, Questions, Available until, Time limit, Allowed attempts, etc.
