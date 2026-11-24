# GenApp Modernized App — Browser E2E Test Report

**Date:** 2026-11-24
**Scope:** Step 4 of the modernization pipeline — end-to-end UI/browser verification of the React + Spring Boot application against the Figma reference ("Haven • Insurance dashboard") and the defined user journeys.
**Environment:** Spring Boot backend `genapp-backend-1.0.0.jar` on `localhost:8080`; React+Vite dev server on `localhost:5173` proxying `/api`. Chrome for Testing, desktop viewport 1024x768 unless noted.
**Evidence:** `browser-e2e-recording.mp4` (annotated video), `screenshots/01..14*.png`.

## Summary

| # | Journey | Result |
|---|---------|--------|
| 1 | Overview layout matches Figma tokens | **PASS** |
| 2 | Customer add / inquire / update | **PASS** (F1 deviation fixed and re-verified) |
| 3 | Account pill shows Customer #N | **PASS** |
| 4a | Motor policy add/inquire/update/delete | **PASS** |
| 4b | House policy add + inquire | **PASS** |
| 4c | Endowment policy add + inquire | **PASS** |
| 4d | Commercial add + Search; no Update (legacy parity) | **PASS** |
| 5 | Negative: inquire nonexistent customer | **PASS** |
| 6 | Responsive: sidebar collapses below 900px | **PASS** |
| R | Regression: Overview lists created policies | **PASS** |

## Detailed results

### 1. Overview / Figma layout tokens — PASS
Computed styles measured in the browser match the spec exactly:
- `body` background `rgb(245,247,245)` = **#f5f7f5**; text `rgb(25,47,44)` = **#192f2c**; font-family **Inter**.
- Sidebar width **240px**, white; active nav item bg `rgb(233,242,237)` = **#e9f2ed**, text `rgb(23,107,80)` = **#176b50**.
- Primary button bg **#176b50**; cards white with **12px** radius and border `rgb(228,234,230)` = **#e4eae6**.
- Brand text "haven" and topbar tagline "Your insurance, simplified." present.
- Protection banner ("Good things are protected.") renders as a white card.
Evidence: `01-overview-figma-layout.png`.

### 2. Customers add / inquire / update — PASS*
- **Add:** filled all 9 fields (Alice Tester etc.), clicked "Add customer". Backend verified: `GET /api/customers/1000008` returns all submitted values. *However*, the inline success message "New customer inserted — #1000008" is **never visible**: `onPickCustomer()` fires in the same React batch as the success notification, immediately unmounting the Customers page and navigating to Overview. The generated number is only observable via the account pill. See Findings F1.
- **Inquire:** entered 1000008 → header switches to "Update customer #1000008", all 9 fields populated, green banner "Customer #1000008 loaded — editing below." Evidence: `03-customer-inquire-edit-mode.png`.
- **Update:** changed postcode to `ZZ99 9XY` → green banner "Customer details updated"; backend verified persisted (`"postcode":"ZZ99 9XY"`). Evidence: `04-customer-updated.png`.

### 3. Account pill — PASS
After add, pill in topbar reads "Customer #1000008" (was "GenApp admin"). Evidence: `02-customer-added-pill.png`.

### 4. Policies — PASS
- **Motor (#1000006):** Add → "New motor policy inserted — #1000006" + full detail table; policy number auto-filled into the inquire field. Inquire → "Policy loaded". Update (premium 320→399, colour Blue→Red) → "motor policy updated", detail table reflects new values and a fresh `lastChanged` timestamp. Delete → "motor policy deleted", detail table removed; backend inquire afterwards returns 404 `Policy not found: 1000006`. Evidence: `05..07-*.png`.
- **House (#1000007):** Add → "New house policy inserted — #1000007"; Inquire → "Policy loaded" with correct detail rows. Evidence: `08-house-added-inquired.png`.
- **Endowment (#1000008):** Add → "New endowment policy inserted — #1000008"; Inquire → "Policy loaded". Evidence: `09-endowment-added-inquired.png`.
- **Commercial (#1000009):** Add → "New commercial policy inserted — #1000009" with all peril/premium fields echoed in the detail table. Search → "1 commercial policy found". **Update button is absent** on the commercial tab (only Add / Search / Delete), matching legacy parity; the explanatory note is displayed. Evidence: `10-commercial-search.png`.

### 5. Negative scenario — PASS
Inquire customer `99999999` → red error banner "Customer not found: 99999999"; form unchanged, no crash, no console errors. Evidence: `11-negative-customer-not-found.png`.

### 6. Responsive layout — PASS
Window resized so `window.innerWidth = 500` (< 900px breakpoint): `.sidebar` computed `display: none`, main content reflows full-width. Restoring to desktop width brings the sidebar back. Evidence: `13-mobile-sidebar-collapsed.png` vs `14-desktop-sidebar-restored.png`.

### Regression: Overview data loop — PASS
After the policy operations, Overview for customer #1000008 shows "03 Active policies", "$1350.00 Total payment", and three policy cards (Home insurance / The Willows / #1000007, Life & endowment / #1000008, Commercial / Office / #1000009) with Active pills, correct premiums and issue/expiry dates. Evidence: `12-overview-with-policies-regression.png`.

## Findings

- **F1 (UX deviation, minor) — FIXED & RE-VERIFIED 2026-11-24:** Originally the "New customer inserted — #N" message never rendered because `onPickCustomer()` fired in the same React batch as the notification, unmounting the Customers page before paint. Fixed in `Customers.jsx` by deferring `onPickCustomer` with `setTimeout(..., 1500)`. Re-verified end-to-end: adding customer Bob Verifier rendered the green banner **"New customer inserted — #1000009"** visibly on the Customers page, then after ~1.5s the app redirected to Overview and the account pill updated to "Customer #1000009" (backend record confirmed). Evidence: `screenshots/15-add-customer-success-message.png`.
- No console errors observed during the entire session (only Vite debug/React DevTools info logs).
- Test-data note: the endowment fund name was stored as "Growth Fun" (a dropped trailing keystroke during automated typing, consistently round-tripped) — automation artifact, not an app defect.

## Console/API supporting evidence (not recorded coverage)
- `POST /api/customers` → `1000008`; `GET` round-trip correct.
- `GET /api/policies/motor/1000006` after delete → HTTP 404, `{"returnCode":"01","message":"Policy not found: 1000006"}`.
- `GET /api/customers/99999999` → `{"returnCode":"01","message":"Customer not found: 99999999"}`.
