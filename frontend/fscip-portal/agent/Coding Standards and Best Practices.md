## Frontend

* **Tech Stack & Tooling**

  * Use **React 18 + TypeScript** with Vite for fast builds and strong typing.
  * Enforce code style via **ESLint** (Airbnb or custom config) and **Prettier** on save/commit.
  * Adopt a **mono-repo with workspaces**: split into `apps/portal` and `libs/ui-components` for shared UI.

* **Component Architecture**

  * Build **presentational** (dumb) and **container** (stateful) components.
  * Organize by **feature folder**:

    ```
    src/
      features/
        accountOverview/
          components/
          hooks/
          styles/
        secureMessaging/
        …
    ```
  * Use **Tailwind CSS** with design tokens for consistent theming.

* **State Management & Data Fetching**

  * Global state via **Redux Toolkit** or Context where appropriate.
  * Side effects handled through **RTK Query** or **React Query** for caching and automatic retries.
  * Always abort stale requests and show skeleton loaders for pending states.

* **Exception Handling**

  * Define a **central exception class** (e.g., `AppError` extending `Error`) to represent all application errors.
  * Create **custom exception subclasses** for distinct scenarios: `NetworkError`, `ValidationError`, `AuthError`, etc.
  * Use **Error Boundaries** at the application root to catch render-time errors and show fallback UI.
  * Wrap unknown or third-party errors in the central `AppError` to enforce a uniform shape and messaging.

* **Configurable Constants & Environment**

  * Extract all changeable values to **`.env` files** (e.g., `.env.development`, `.env.production`) using Vite’s `import.meta.env` convention.
  * Variables include: API base URLs (`VITE_API_BASE_URL`), feature flags, request timeouts, third-party keys.
  * Maintain a **`.env.example`** with documented sections (API, Features, UI Limits) so new developers can onboard quickly.
  * Avoid hardcoding any values in source; reference `import.meta.env.VITE_…` directly.

* **Accessibility & Internationalization**

  * Enforce **WCAG 2.1 AA**: keyboard navigation, ARIA labels, focus management, color contrast checks.
  * Externalize all UI strings with **react-i18next**; support dynamic locale selection.

* **Testing**

  * **Unit Tests**: Jest + React Testing Library, > 90% coverage on critical components.
  * **Integration/E2E**: Cypress for end-to-end flows.
  * **Visual Regression**: Percy or Cypress snapshot testing on key UI components.

* **Performance & Best Practices**

  * Lazy-load routes and heavy components via **React.lazy** + **Suspense**.
  * Optimize bundle size with code splitting and tree shaking.
  * Use `React.memo`, `useMemo`, `useCallback` judiciously.

---
