# Team Coding Style Guidelines

This document defines our coding standards for the project. All team members should follow these conventions to keep the codebase consistent and maintainable.

---

## 1. Tech Stack

| Layer      | Technology        |
| ---------- | ----------------- |
| Frontend   | React, JavaScript |
| Backend    | Java, Spring Boot |
| Database   | MySQL             |
| Formatting | Prettier          |
| Deployment | Docker (planned)  |

---

## 2. Formatting Standard — Prettier

We plan to use **Prettier** as our automatic code formatter.

The purpose of using Prettier is to:

- Avoid formatting debates
- Keep diffs clean and readable
- Maintain consistent style across 6 team members
- Reduce review friction

Formatting decisions will follow Prettier configuration rather than personal preference.

---

## 3. Installing Prettier

Check this link for detailed descriptions: https://prettier.io/docs/install

### Initial setup (shared format)

1. Install dependencies at the project root:
   ```bash
   npm install
   ```
2. Apply formatting: `npm run format`
3. Check formatting before CI/PR: `npm run format:check`

Configuration lives in `.prettierrc` and `.prettierignore` at the project root; both are committed so the whole team uses the same rules.

---

## 4. Prettier Configuration

Use a configuration similar to:

```json
{
  "semi": true,
  "singleQuote": true,
  "trailingComma": "all",
  "printWidth": 100,
  "tabWidth": 2
}
```

Store this in `.prettierrc` at the project root (or in each app that uses Prettier).

---

## 5. Using Prettier

To format the entire project:

```bash
npx prettier --write .
```

To format a specific file:

```bash
npx prettier --write <filename>
```

Add a script in `package.json`:

```json
{
  "scripts": {
    "format": "prettier --write ."
  }
}
```

Then formatting can be run using:

```bash
npm run format
```

---

## 6. Naming Conventions

### 6.1 JavaScript / React (Frontend)

| Kind                | Convention              | Example                       |
| ------------------- | ----------------------- | ----------------------------- |
| Variables, params   | camelCase               | `userName`, `isLoading`       |
| Constants           | UPPER_SNAKE             | `API_BASE_URL`, `MAX_RETRY`   |
| Functions           | camelCase               | `fetchUser`, `handleClick`    |
| React components    | PascalCase              | `UserProfile`, `TodoItem`     |
| Custom hooks        | camelCase, `use` prefix | `useAuth`, `useFetch`         |
| Files (components)  | PascalCase              | `UserProfile.jsx`             |
| Files (utils/hooks) | camelCase               | `formatDate.js`, `useAuth.js` |
| CSS modules         | camelCase               | `buttonPrimary`, `cardTitle`  |
| Event handlers      | `handle` or `on` prefix | `handleSubmit`, `onClick`     |

- Use descriptive names; avoid single letters except in short loops (`i`, `j`) or callbacks (`e` for event).
- Booleans: prefer `is`, `has`, `should`, `can` prefix (e.g. `isActive`, `hasError`).

### 6.2 Java / Spring Boot (Backend)

| Kind                    | Convention                            | Example                                  |
| ----------------------- | ------------------------------------- | ---------------------------------------- |
| Classes, interfaces     | PascalCase                            | `UserService`, `OrderController`         |
| Methods, variables      | camelCase                             | `getUserById`, `userName`                |
| Constants               | UPPER_SNAKE                           | `MAX_PAGE_SIZE`, `DEFAULT_TIMEOUT`       |
| Packages                | lowercase                             | `com.project.service`, `com.project.dto` |
| DTOs / Entities         | PascalCase                            | `UserDto`, `OrderEntity`                 |
| Repository interfaces   | PascalCase, `Repository` suffix       | `UserRepository`                         |
| Service interfaces/impl | PascalCase, `Service` / `ServiceImpl` | `UserService`, `UserServiceImpl`         |

- Controllers: `*Controller`; REST resources: plural nouns (e.g. `/users`, `/orders`).
- Avoid abbreviations unless widely known (e.g. `id`, `url`, `dto` are acceptable).

### 6.3 Database (MySQL)

| Kind         | Convention                       | Example                                    |
| ------------ | -------------------------------- | ------------------------------------------ |
| Tables       | snake_case, plural preferred     | `users`, `order_items`                     |
| Columns      | snake_case                       | `user_id`, `created_at`                    |
| Primary key  | `id` or `{table_singular}_id`    | `id`, `user_id`                            |
| Foreign keys | `{referenced_table_singular}_id` | `user_id`, `order_id`                      |
| Indexes      | descriptive                      | `idx_users_email`, `idx_orders_created_at` |

---

## 7. Collaboration Standards

- Keep code readable and simple; prefer clarity over cleverness.
- Do not commit commented-out code; remove or replace with a ticket reference.
- Remove unused variables and imports before merging.
- Keep pull requests focused and limited in scope.
- Avoid mixing refactors and feature additions in one PR unless necessary.
- Run Prettier (and any configured linters) before pushing.

---

## 8. Frontend (React / JavaScript) Conventions

- Use functional components and hooks; avoid class components for new code.
- Prefer named exports for components and utilities when it improves clarity; default export for page/route components is acceptable.
- Keep components small; extract sub-components or custom hooks when a file grows large (>200 lines consider splitting).
- API calls: centralize in a dedicated layer (e.g. `api/` or `services/`) rather than scattering fetch logic inside components.
- Environment-specific config (e.g. API base URL) via environment variables, not hardcoded.

---

## 9. Backend (Java / Spring Boot) Conventions

- Follow standard layered structure: controller → service → repository (or similar).
- Controllers: thin; handle HTTP mapping and validation only; delegate business logic to services.
- Use DTOs for API request/response; do not expose entities directly in REST APIs.
- Prefer constructor injection for dependencies; avoid field injection.
- Use meaningful HTTP status codes and consistent error response format (e.g. `{ "error": "...", "code": "..." }`).

---

## 10. Database (MySQL) Conventions

- Prefer migrations or versioned scripts for schema changes; avoid ad-hoc manual changes in shared DBs.
- Use parameterized queries / JPA or JDBC prepared statements; never concatenate user input into SQL.
- Index columns used in `WHERE`, `JOIN`, and `ORDER BY` where it improves performance; name indexes clearly.

---

## 12. Docker (When Adopted)

- Prefer multi-stage builds to keep image size small.
- Do not store secrets in Dockerfiles or images; use environment variables or secrets management.
- Keep `Dockerfile` and `docker-compose` (if used) under version control and documented (e.g. in README).

---

## 13. Linting and Tooling

- **ESLint** (frontend): Use a shared config (e.g. `eslint-config-prettier`) so ESLint and Prettier do not conflict. Run lint in CI.
- **Java**: Use a consistent formatter (e.g. Google Java Format or project-specific formatter) and run it before committing; consider Checkstyle or Spotbugs for style and basic checks.

---

## 14. Future Updates

This document may be extended with:

- Canvas API integration structure (when applicable)
- Detailed folder structure per app (frontend/backend)
- Testing standards (unit, integration, e2e)
- API versioning and documentation (e.g. OpenAPI) conventions
