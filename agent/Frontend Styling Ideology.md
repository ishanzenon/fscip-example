# Frontend Styling Ideology Scratchpad

Below are initial brainstorming points to guide styling in our React application. Feel free to refine, reorder, or add more ideas.

## 1. Establish a Design System

* Define color palette, typography scale, spacing units (e.g., 4px grid) as CSS variables or Tailwind theme tokens.
* Create reusable UI primitives (Button, Card, Typography) with consistent base styles.

## 2. Leverage Utility-First & Atomic Styles

* Use Tailwind or CSS Modules with utility classes to minimize custom CSS.
* Encourage composition over duplication: combine utility classes instead of writing new CSS.

## 3. Theming & CSS Variables

* Expose global CSS variables (e.g. `--primary`, `--spacing-base`) in a `:root` or theme provider.
* Allow dynamic theme switching (light/dark) by toggling variable sets.

## 4. Component-Level Styles

* Co-locate styles with components via CSS Modules, styled-components, or Tailwind in JSX.
* Use `@apply` (Tailwind) or mixins to pull in shared patterns.

## 5. DRY Styles & Common Parent

* Elevate shared styles (e.g., container widths, typography) to layout or parent components.
* Avoid repeating margin/padding rules by using wrapper components.

## 6. Mobile-First & Responsiveness

* Write mobile styles as default, cascade up with `sm:`, `md:`, `lg:` breakpoints.
* Use relative units (%, rem, vw) for fluid layouts.
* Test on 320px, 768px, 1024px viewports.

## 7. Consistency & Linting

* Enforce stylelint with rules for property order, no duplicate selectors, and use of variables.
* Integrate Prettier for formatting JSX inline class names.

## 8. Performance & File Size

* Purge unused CSS in production (e.g. Tailwind’s purge setting).
* Limit global overrides to reduce CSS specificity wars.

## 9. Documentation & Onboarding

* Maintain a living style guide or Storybook showcasing components and tokens.
* Document example use cases and anti-patterns.

## 10. Change Management

* Centralize variables: updating a CSS variable or token cascades app-wide.
* Encourage PRs to include style changes in Storybook for review.

---

*Next: Review and refine these points before finalizing into the Coding Standards document.*
