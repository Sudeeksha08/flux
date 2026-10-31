# Brightloop — subscription workspace

A React + TypeScript frontend using Vite, Tailwind CSS, React Router, Axios, Lucide, Recharts and React Hook Form. The original workspace contained no application files; this is a new, isolated project.

## Run locally

Use Node.js 20.19+ or 22.12+ and npm.

```powershell
npm install
Copy-Item .env.example .env
npm run dev
```

Open the Local URL printed by Vite. The sign-in screen offers Admin, Staff and Customer previews. Demo records are explicitly labeled, exist only in memory, and reset when you reload or sign out. No demo operation contacts a payment processor or sends email. Currency currently uses USD; adapt `money()` in `src/utils/modules.ts` for your business.

```powershell
npm test
npm run typecheck
npm run build
npm run preview
```

Production output is `dist/`. Configure your web server to rewrite frontend routes to `index.html`. This project has not been publicly deployed.

## What is included

- Separate navigation and route permissions for ADMIN, STAFF and CUSTOMER, with account login, registration, forgot/reset password and JWT-ready requests.
- Admin and staff operations workspaces; customer portal with scoped subscriptions, invoices, payment history, profile and plan browsing.
- Dashboard metrics derived from loaded records, revenue and subscription growth charts, subscription health and payment distribution charts, period selection and report CSV exports.
- Shared searchable, paginated record tables, status filters, empty/loading/error states, accessible modal dialogs, validation, confirmation flows and toast feedback.
- Customer/staff/catalog/variant/plan/quotation/template/subscription/invoice/discount/tax/payment modules with create, edit, detail and deletion flows for permitted roles.
- Product/variant price calculations, plan rules, quotation validity, subscription transitions, invoice tax/discount totals and partial payment balances.
- Invoice print preview and browser Save as PDF in demo; server PDF download in API mode.

Quotation, invoice and subscription forms support multiple product lines with per-line quantities, unit prices and taxes. Customer self-service checkout, automatic recurring billing, email delivery and payment processing are backend or future integration work. Customer plan cards browse and show details; they do not purchase a plan. Demo profile editing is explicitly blocked because the preview identity is read-only.

## Structure

- `src/components`: shared dialog, badges and state components
- `src/layouts`: responsive shell and role-specific navigation
- `src/pages/auth`: account forms
- `src/pages/admin`, `src/pages/staff`, `src/pages/customer`: separate role dashboard entry points
- `src/components/dashboard`: shared overview and analytics rendering
- `src/components/management`: shared typed record editor
- `src/pages/customer/Settings.tsx`: profile/account settings
- `src/pages/Management.tsx`: schema-driven management pages used by admin/staff and read-only customer views
- `src/context`: authenticated identity, demo data and server-backed operations
- `src/hooks`: loading, failure and retry handling
- `src/routes`: role and module guards
- `src/services`: Axios client and domain API services
- `src/types`, `src/utils`, `src/assets`: models, schemas, financial/permission helpers and design system

Staff and customer pages share reusable components rather than duplicate page files. `src/utils/modules.ts` defines fields and navigation. `src/utils/business.ts` holds permission and billing helpers. `src/utils/validation.ts` provides shared management validation; `src/services/contracts.ts` validates API response shapes and `src/services/index.ts` selects domain adapters. Charts and management pages load in separate bundles.

## Spring Boot API contract

Set `VITE_API_BASE_URL=http://localhost:8080/api` in `.env`. Restart Vite after changing environment variables. Configure Spring CORS for your frontend origin. Authentication uses a bearer token in sessionStorage; there is no automatic token refresh. A 401 from the current token clears the session and returns to login; stale responses from earlier tokens cannot clear a new session. Session restoration completes before protected-route redirects. API failures display a message and never fall back to a demo success.

Expected authentication endpoints:

- `POST /auth/login` accepts `{email,password}` and returns `{token,user}`.
- `user` is `{id,name,email,role,permissions}`; role is ADMIN, STAFF or CUSTOMER. `permissions` is a string array (omitted permissions default to no grants); staff reporting requires `reports`. For CUSTOMER, `user.id` must equal the corresponding customer record ID (or adapt the ownership mapping).
- `GET /auth/me` returns that user.
- `POST /auth/register` accepts `{name,email,password}`. Public registration must create CUSTOMER accounts only; no role is sent by the frontend.
- `GET /auth/email-available?email=...` returns `{available:boolean}`. The backend must also enforce uniqueness atomically on registration.
- `POST /auth/forgot-password` accepts `{email}`; send reset links to `/reset-password?token=...`.
- `POST /auth/reset-password` accepts `{token,password}`.
- `PUT /auth/profile` accepts `{name,email}`.

Domain collection endpoints are `/customers`, `/staff`, `/products`, `/variants`, `/plans`, `/quotations`, `/templates`, `/subscriptions`, `/invoices`, `/discounts`, `/taxes`, `/payments`. Documents may contain `lines: [{productId,quantity,price,tax}]` plus a document-level fixed `discount`. Legacy single-line records are also accepted. Field names are defined in `src/utils/modules.ts` and examples in `src/utils/demo.ts`.

- `GET /<resource>` returns a flat JSON array of records with string `id` fields, scalar properties and optional typed `lines`. Adapt nested or paginated Spring DTOs in the services before returning them to components.
- `POST /<resource>` creates and returns the complete saved record. The server assigns IDs.
- `PUT /<resource>/:id` updates and returns the complete saved record.
- `DELETE /<resource>/:id` returns a success status (204 supported). Return an error for referenced records.
- `POST /subscriptions/:id/{confirm|activate|pause|renew|close}` applies backend lifecycle/plan rules. Renewal timing and price policy are server-owned.
- `GET /invoices/:id/pdf` returns a PDF blob.
- Errors may return `{message:string}` with an appropriate HTTP error status.

Financial totals in the frontend are previews. The backend must calculate canonical prices, tax, discounts, invoice status and outstanding balances, validate minimum quantities and plan availability, reject overpayments atomically, and enforce uniqueness and referential integrity. Plan permission fields currently use `Autoclose`, `Pausepermission`, `Earlyclosepermission`, and `Renewalpermission`; map these to your Java DTO conventions in the service adapter if needed.

**Authorization is a server responsibility.** Route guards and client filters improve the UI but cannot secure data. Every endpoint must enforce role, staff permission and customer ownership checks. Customer endpoints must only return their own records and authorized public catalog data; never send another customer's data and rely on frontend filtering. Domain relation requests use the same endpoints, so authorize customer-scoped relation lists or adapt the services to dedicated `/me` endpoints. Demo data is intentionally bundled and is not confidential production information.

## Verification and remaining integration work

The standalone typecheck, production build, and 35 regression tests pass. The latest review verified all 29 sidebar destinations, quotation creation, the Templates link from quotation details, required textarea validation, and customer dashboard layouts at 390px and 768px. Local HTTP readiness returned 200. See REVIEW_REPORT.md for the full review and remaining tasks.

No Spring Boot backend or API specification was supplied. Actual JWT authentication, email uniqueness, password email delivery, server authorization, persistence and PDF generation remain unverified until the backend implements the documented contract. Browser automation is a smoke check rather than a comprehensive accessibility or cross-device test suite.
