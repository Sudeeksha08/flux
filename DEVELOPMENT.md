# Development record

All paths below are relative to this project. No original application code was overwritten.

## Phase 1 — Inspection and architecture

Inspected the provided directory: only `work/` and `outputs/` existed. Created `package.json`, `index.html`, `tsconfig.json`, `vite.config.ts`, `.env.example`, `.gitignore`, `src/main.tsx`, `src/types/index.ts` and domain schemas in `src/utils/modules.ts`. Installed the requested stack. Inspection and dependency installation completed successfully.

## Phase 2 — Design system

Created `src/assets/styles.css`, `src/components/Shared.tsx`, and `src/layouts/Shell.tsx`: forest green and warm neutral palette, responsive sidebar, reusable cards and tables, modal dialogs with native focus management, state displays, notification toasts and print styling. Desktop browser screenshot inspected; mobile CSS breakpoints are implemented but a physical-device audit has not been run.

## Phase 3 — Authentication and routing

Created `src/pages/auth/Auth.tsx`, `src/routes/ProtectedRoute.tsx`, `src/context/Workspace.tsx` and `src/App.tsx`. Added login/register/forgot/reset forms, validation, demo entry, role redirection and module permissions. Checked sign-in rendering and role navigation in-browser; automated tests cover access decisions. Live auth has no supplied backend to test against.

## Phase 4 — Admin dashboard

Created `src/pages/admin/Dashboard.tsx`, shared loading in `src/hooks/useRecords.ts`, fixture data in `src/utils/demo.ts` and CSV export in `src/utils/export.ts`. Added six overview metrics, period selection, revenue, subscription health, growth and payment status visualizations, recent subscriptions and record links. Admin dashboard and navigation verified in-browser. Data comes from actual loaded records or prominently labeled demo fixtures.

## Phase 5 — Staff dashboard

Reused the dashboard and shell with staff-specific navigation and permission checks rather than duplicating page code. Staff can access operational modules, account settings and explicitly permitted reports. Browser check verified that admin management links are absent. Final cleanup removed a product metric link that staff could not open. Unit tests verify admin modules are blocked for staff.

## Phase 6 — Customer portal

Created `src/pages/customer/Settings.tsx` and customer-specific navigation, readonly record views and plan cards. Customer browser check verified that Olivia sees exactly her single invoice, with no create/edit/delete controls. Client ownership filters complement the required backend authorization. Profile changes require a connected account.

## Phase 7 — Management modules

Created `src/pages/Management.tsx`, with schemas for customers, staff, products, variants, plans, quotations, templates, subscriptions, invoices, discounts, taxes and payments. Added CRUD flows, details, filtering/pagination, confirmations, line-item forms and computed totals. Created `src/utils/business.ts` for permissions, outstanding balance and lifecycle rules. Browser smoke tests saved a product and a two-line invoice in demo mode; invalid incomplete invoice lines were rejected. Customer invoice scope and staff navigation were verified in the production build preview.

## Phase 8 — API integration architecture

Created `src/services/api.ts`, `auth.ts`, `customers.ts`, `products.ts`, `variants.ts`, `plans.ts`, `quotations.ts`, `templates.ts`, `subscriptions.ts`, `invoices.ts`, `discounts.ts`, `taxes.ts`, `payments.ts`, `staff.ts` and `dashboard.ts`. Axios reads the environment base URL, attaches the bearer token and expires the session on 401. Shared resource methods drive CRUD; domain exports are available for Spring DTO adaptation. README documents the endpoint contract and server obligations. No API success is simulated when an API request fails.

## Phase 9 — Verification and fixes

Created `src/utils/business.test.ts` with eight tests for permissions, financial totals, partial payments, lifecycle transitions and multi-line invoices. Fixed the initial form validation TypeScript conflict, a detail-page route, encoded text, role-inappropriate links and large initial bundles. Formatted source with Prettier. `npm test` passes all eight tests; `npm run build` passes TypeScript and Vite. Local server returned HTTP 200. Charts and management pages are code-split; final build has no oversized-chunk warning.

## Integration limits

No backend was present. JWT persistence, email delivery, unique email checks, authorization, server PDF generation and canonical billing rules are ready for integration but not end-to-end verified. Demo records reset on refresh. No payment gateway or automatic billing job is implemented in a frontend. Browser print supplies demo PDF saving; the API PDF button uses the documented server endpoint. Broad mobile/device/accessibility regression testing remains a release task.

## Frontend architecture review follow-up

Completed the requested ten-point review and fixes; see REVIEW_REPORT.md. Added session isolation/response contract tests and validation/navigation coverage (35 tests total), an explicit typecheck command and stricter unused-code checks. Browser checks covered all 29 sidebar destinations across the three roles, quotation creation, the nested Templates link, template validation, and 390px/768px responsive layouts. The previous phase notes above describe the original delivery, not the current verification totals.
