# Brightloop frontend architecture review

Reviewed the complete local frontend in `outputs/brightloop`. Existing CRUD flows, charts, role menus, demo mode, exports, invoice printing/PDF integration and account pages were preserved. Changes focus on observable defects, response safety, validation and reusable boundaries.

## Overall assessment

The structure is a workable foundation for a small or medium application. After the fixes below, the requested typecheck and production build pass and the reviewed navigation works. It is backend-ready against the documented contract, but frontend guards do not establish production security. No Spring Boot backend was available for end-to-end authorization or persistence testing.

## Findings fixed

### High: session restoration and stale-request races

Previously, `/auth/me` could finish after sign-out or demo entry and restore an old identity. Protected routes redirected to login before restoration completed. Late resource responses could repopulate cleared state, and an old request's 401 could invalidate a newer session.

Added an explicit restoration state, abortable reads, a session generation guard for asynchronous state writes and token-aware 401 handling. Login responses are validated before token storage. Protected routes retain a requested pathname and sign-in restores it only when it belongs to the same role and is permitted. Files: `src/context/Workspace.tsx`, `src/utils/session.ts`, `src/hooks/useRecords.ts`, `src/routes/ProtectedRoute.tsx`, `src/services/api.ts`, `src/services/auth.ts`, `src/services/contracts.ts`, `src/utils/navigation.ts`.

### Medium: successful HTTP responses were trusted without checking shape

An HTML fallback, malformed user or non-array list could enter application state. Added runtime contract validation for tokens, users, permission arrays, record IDs, scalar record fields and product lines. Missing permissions grant nothing. Invalid data is reported instead of being treated as a saved record or authenticated identity. Creates omit empty client IDs; record/action URL segments are encoded. Errors now distinguish authorization, missing endpoints, validation and conflicts. A network error no longer claims that a server write certainly did not happen.

### Medium: domain services existed but were bypassed

Resource operations previously built adapters inside the context, leaving domain files unused. Added a service registry that selects the existing domain adapters. Moved profile operations into `authService` and invoice PDF fetching into `invoicesService`. Unknown resource keys are rejected. Kept the requested dashboard service as a reserved integration adapter; dashboards currently derive analytics from domain records rather than a backend aggregate endpoint.

### Medium: dashboard role separation and staff requests

Admin, Staff and Customer originally shared a file under the admin directory. Staff dashboard reads included the admin product collection, so an otherwise valid staff session could fail if the backend correctly restricted that endpoint.

Added role dashboard entry points under `pages/admin`, `pages/staff` and `pages/customer`, with shared rendering in `components/dashboard/Overview.tsx`. Staff dashboard reads now omit products, and customer reads omit the customer collection. Existing route and report permission checks remain. Also included the Quotation lifecycle state in subscription distribution, kept status colors stable and corrected the growth headline to count only the selected reporting period. Invoices due today are no longer counted as overdue during that day.

### Medium: incomplete validation and lifecycle bypasses

Required template textareas did not register required validation, whitespace-only names passed, and profile validation had no inline feedback. Shared cross-field validation now checks valid/reversed dates, finite nonnegative numeric fields, whole minimum quantities/usage limits, percentage bounds, existing relations, product lines, selected plan minimum quantities, and payment amounts against outstanding balances. Missing invoices cannot silently bypass payment validation.

Extracted the record editor to `src/components/management/RecordEditor.tsx` and replaced `useForm<Record<string, any>>` with typed form values. Payloads are selected from the module schema. Existing subscription status changes now go through lifecycle actions instead of bypassing plan permissions in the generic edit form; status and plan permissions are checked again at the mutation boundary. Demo renewals advance by the plan billing cycle and clamp month ends/leap days. Demo reference checks include all product lines before permitting a product deletion.

### Medium: broken nested navigation and reused auth forms

The Templates link used a relative path that resolved incorrectly from quotation detail URLs. It now uses an absolute role path. Authentication routes now key the form by page, preventing hidden registration/reset fields or old validation state from leaking into login. Unsupported detail URLs for non-record pages display a missing-page state. Pagination clamps after deleting the final row on a later page.

### Low: mobile keyboard access and dialog labeling

The translated-offscreen sidebar remained keyboard reachable. Closed mobile navigation is now inert. Opening it locks background interaction and body scroll, moves focus inside, traps Tab, provides a close button and handles Escape; closing restores focus. Shared dialogs have accessible title associations. Added reduced-motion support. Profile updates refresh the visible user identity after a successful API call.

## Answers to the ten requested checks

1. **Folder structure — mostly scalable.** Layouts, routing, auth, role pages, shared components, services, hooks, types and utilities have clear boundaries. The new dashboard and editor boundaries reduce coupling. At larger scale, move management-specific schemas/types next to their feature folders and separate authentication from the resource store.
2. **Reusable components — yes.** The shell, shared chart overview, record editor, dialog, status badge, loading/error/empty states, export utility and schema-driven management page are reused. Remaining table/detail markup can be extracted when another use case needs it.
3. **Role routing — correct for frontend access.** Role URL mismatches redirect to the signed-in role; module permissions reject unauthorized pages; staff reports require an explicit grant; customer pages filter owned records. Restoration is now awaited. API authorization remains mandatory.
4. **Dashboards separated — yes at entry points, routes, navigation and data selection.** Charts and presentation are shared intentionally. This avoids copying an entire dashboard per role.
5. **API services — organized and wired.** One environment-driven Axios client, domain adapters, auth adapter, runtime response contracts and resource registry. Backend pagination/nested DTOs require adapter changes rather than component changes.
6. **Authentication — stronger and backend-ready, not production-certified.** Validated identity and role data, session isolation, safe return paths and current-token expiration handling are present. Tokens still live in sessionStorage and are readable by same-origin JavaScript. There is no refresh/revocation flow, cookie-based session design or verified server authorization yet.
7. **Forms — strengthened and regression-tested.** Required text/email/password checks, cross-field dates, numeric bounds, relations, line items, plan minimum quantity and partial payment limits are checked. The backend must repeat all validation and enforce canonical prices and concurrent balance changes atomically.
8. **Navigation — all 29 visible sidebar destinations verified in-browser.** Also verified quotation creation, quotation details → Templates, and invalid-template feedback. Dynamic URLs for records absent from demo data cannot all be exercised. Route permission/return-path helpers are covered by tests.
9. **Responsive UI — implemented and smoke-tested.** The customer dashboard fits 390px and 768px widths without horizontal page overflow. Mobile menu opening, Escape closing and focus return were checked. Desktop navigation was checked for all roles. This is not a complete cross-browser/accessibility/device audit.
10. **Unnecessary duplicate components — none identified.** Thin role dashboard entry points are intentional and share a single implementation. The domain service wrappers provide adapter boundaries. Some repeated billing/status presentation remains a future extraction opportunity, not a blocker.

## Verification completed

- Baseline TypeScript check and production build passed before changes; runtime and architectural defects still existed.
- Final `npm run typecheck`: passed with `strict`, `noUnusedLocals` and `noUnusedParameters` enabled.
- Final `npm test`: 35 tests across four test files passed. These cover permission decisions, billing/partial payments, lifecycle/renewal dates, session generation, response contracts, current/stale 401 handling, create/update adapters, navigation helpers and form validation.
- Final `npm run build`: TypeScript and Vite production build passed. No oversized-chunk warning; charts and pages remain code-split.
- Browser: Admin 14, Staff 8 and Customer 7 sidebar destinations opened with the expected heading/path in demo mode.
- Browser: a valid quotation saved with a computed $129 total; its detail-page Templates link opened `/admin/templates`.
- Browser: a whitespace-only required template textarea displayed validation and did not save.
- Responsive browser check: 390px viewport/page width 375px; 768px viewport/page width 753px (scrollbar accounts for the difference). Mobile menu opened and Escape returned focus to its trigger.
- Local HTTP readiness returned 200. Browser startup initially timed out before local network permission was granted; checks succeeded afterward.

## Remaining work, in priority order

### Required before production

- Implement and test the Spring Boot contract: JWT verification, customer ownership on every read/write, staff permission enforcement, profile access, CORS and backend validation. Never depend on frontend filtering to protect another customer's records.
- Decide and implement a production session strategy. Review HttpOnly/Secure/SameSite cookie sessions or a short-lived bearer/refresh design, logout revocation, and deployment CSP/HTTPS. The current sessionStorage approach remains susceptible to token theft through XSS.
- Complete actual email uniqueness checks, registration, reset-token expiry/one-time use and password email delivery. Confirm public registration can only create CUSTOMER accounts.
- Validate canonical tax/discount/price rules, lifecycle transitions, plan availability, renewal dates, referential integrity and overpayments atomically on the server. Frontend calculations are previews.
- Run backend integration and browser end-to-end tests with realistic role-scoped API fixtures. Unit tests of session guards do not replace browser tests of delayed authenticated restoration.

### Scale and quality follow-up

- Replace generic `RecordData` schemas with domain-specific DTOs/discriminated form types as contracts settle. Nested/paginated DTOs need explicit adapters.
- Split the combined Workspace auth/data/toast context when render costs or team ownership justify it; add server pagination/filtering and a query cache for large collections. Current tables paginate loaded arrays in memory.
- Break the management page's list/detail presentation into focused components as its behavior grows; avoid duplicating the forms per entity.
- Add automated browser regression coverage for all modules, mobile dialogs, keyboard focus, contrast and screen-reader behavior. The responsive check covered a representative customer dashboard, not every page at every size.
- Align plan permission field naming with Java DTO conventions in service adapters. Validate the customer-user ID mapping used for ownership.
- Connect payment processing, recurring billing and server invoice PDFs if required by the backend scope. Customer plan browsing remains informational rather than a checkout flow.
- Review dependency advisories and supported package versions in a separate dependency-maintenance pass. No dependency upgrades or audit claims are included in this review.

## Main modified and new files

Modified: `src/App.tsx`, `src/context/Workspace.tsx`, `src/hooks/useRecords.ts`, `src/routes/ProtectedRoute.tsx`, `src/layouts/Shell.tsx`, `src/components/Shared.tsx`, `src/pages/Management.tsx`, `src/pages/admin/Dashboard.tsx`, `src/pages/auth/Auth.tsx`, `src/pages/customer/Settings.tsx`, `src/services/api.ts`, `src/services/auth.ts`, `src/services/invoices.ts`, `src/utils/business.ts`, `src/assets/styles.css`, `package.json`, `tsconfig.json`, `README.md`, and `DEVELOPMENT.md`.

New: `src/components/dashboard/Overview.tsx`, `src/components/management/RecordEditor.tsx`, `src/pages/staff/Dashboard.tsx`, `src/pages/customer/Dashboard.tsx`, `src/services/index.ts`, `src/services/contracts.ts`, `src/services/contracts.test.ts`, `src/services/api.test.ts`, `src/utils/session.ts`, `src/utils/navigation.ts`, `src/utils/validation.ts`, `src/utils/review.test.ts`, and this report.
