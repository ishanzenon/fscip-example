
# **Comprehensive Epic Backlog – Financial Services Customer Interaction Portal (FSCIP)**

_(Every story is written at a level that can be pasted unchanged into Jira / Azure DevOps. All stories include a definition of done (DoD) and per‑function subtasks so junior engineers know exactly what to pick up.)_

---

## **Epic A – Identity & Access Management**

|Story ID|Story Title|Story Description & Acceptance / DoD|Sub‑tasks (FE / BE / QA&INT)|
|---|---|---|---|
|**A‑1**|**Customer Registration Wizard**|Visitors can complete a 3‑step wizard (Profile → Credentials → KYC upload) to create an account.**DoD**: Email/mobile uniqueness validated; OTP triggered; user persisted as **PENDING**; audit log recorded; unit & Cypress tests pass.|FE ▶️ Build `RegistrationWizard` with react‑hook‑form, drop‑zone, progress bar.BE ▶️ `POST /auth/register`, JPA entity + mapstruct DTO, Twilio OTP producer, audit event.QA ▶️ Swagger contract, Postman collection, Cypress happy + negative flow.|
|**A‑2**|OTP Verification Flow|New users enter 6‑digit OTP to activate the account. Locked after 5 bad attempts.**DoD**: OTP stored in Redis (TTL 10 min); success → status = ACTIVE; failure returns 400; log entry.|FE ▶️ `OtpModal` with countdown.BE ▶️ `POST /auth/otp/verify`, Redis lookup, attempt counter.QA ▶️ Unit tests for expiry & lockout.|
|**A‑3**|Password Reset (Forgot / Change)|Users can request reset link and change password in profile.**DoD**: Tokenised link emailed; strength rules enforced; old tokens invalidated.|FE ▶️ `ForgotPasswordPage`, `ChangePasswordDialog`.BE ▶️ `/auth/password/reset-request`, `/auth/password/reset-confirm`.|
|**A‑4**|Two‑Factor Authentication Toggle|Customers may enable/disable 2FA (email + OTP). Default = ON for Admins/RMs.**DoD**: Setting stored, login flow adapts, audit saved.|FE ▶️ Profile switch + confirmation modal.BE ▶️ Keycloak custom attribute, REST endpoint.|
|**A‑5**|Customer Login (Password / OTP)|Standard login with password **or** OTP only (if no password set).**DoD**: JWT issued, refresh flow works, CSRF cookies set.|FE ▶️ `/login` page, form validation.BE ▶️ Spring Security authentication provider, JWT issuer.|
|**A‑6**|Employee SSO (SAML)|Employees authenticate via corporate IdP SAML.**DoD**: Successful SAML handshake, roles mapped, logout SAML back‑channel.|BE ▶️ Configure Keycloak IdP, metadata import.QA ▶️ Simulate SAML with stub IdP.|
|**A‑7**|Session Management & Secure Logout|Idle timeout 15 min, absolute 8 h; logout clears tokens.**DoD**: Activity ping resets idle; logout redirect; session invalidated server side.|FE ▶️ Axios interceptor, idle detector.BE ▶️ Spring Security `SessionRegistry`; logout endpoint.|
|**A‑8**|Last Login Display|Dashboard shows last successful login date/time.**DoD**: Attribute updated on login, shown in header tooltip.|BE ▶️ Persist in `user.last_login_at`.FE ▶️ UI badge.|
|**A‑9**|Role Management Dashboard|Admins can grant/revoke roles per user.**DoD**: Role change effective immediately; prevented for self‑downgrade; audit row.|FE ▶️ `RoleTable` with MUI DataGrid editing.BE ▶️ `PUT /admin/users/{id}/roles`.|
|**A‑10**|User Deactivation / Reactivation|Admin toggles active flag; deactivated users cannot log in.**DoD**: Endpoint returns 204; audit saved; UI badge “Suspended”.|FE ▶️ Context menu action + confirm.BE ▶️ `PATCH /admin/users/{id}/status`.|
|**A‑11**|Audit Log Viewer|View paginated list of auth events with filters.**DoD**: Supports actor, type, date filters; CSV export; RBAC enforced.|FE ▶️ `AuditTable` virtualised; export button.BE ▶️ `/audit` query endpoint.|
|**A‑12**|Access Logs Download|Auditors can download full access logs (zip CSV).**DoD**: Background job streams zip; link emailed when ready.|BE ▶️ Spring Batch job, S3 temp URL.FE ▶️ “Request Logs” button modal.|
|**A‑13**|Consent & Privacy Settings|Users review T&Cs, privacy policy; store consent version.**DoD**: Blocking modal until acceptance; version hash persisted.|FE ▶️ Markdown viewer modal.BE ▶️ `consents` table CRUD.|
|**A‑14**|Failed Login Lockout|Lock account for 15 min after 5 consecutive failures.**DoD**: Attempts counter persists; unlock by Admin possible.|BE ▶️ Spring Security `AuthenticationFailureHandler`.|
|**A‑15**|KYC Upload & Validation|Upload PAN, Aadhaar, passport etc.; server validates file type & clarity.**DoD**: Files in S3, metadata stored, status=PENDING_KYC.|FE ▶️ Drop‑zone with preview.BE ▶️ Malware scan stub + OCR clarity check.|
|**A‑16**|External KYC Service Integration|Hit vendor API for ID verification.**DoD**: Vendor response stored, status=VERIFIED/REJECTED.|BE ▶️ Feign client, retry, circuit breaker.|
|**A‑17**|Profile Update (Name, Address)|Customers edit profile; address change requires proof doc.**DoD**: Field validation; change history recorded.|FE ▶️ Form & doc upload.BE ▶️ `PATCH /profile`.|
|**A‑18**|GDPR Account Deletion Request|User requests delete; soft‑locks account; admin approval triggers hard delete job.**DoD**: Data purged, audit saved, email confirmation sent.|BE ▶️ Workflow status machine, scheduled deleter.|
|**A‑19**|Email & Phone Change Verification|Changing email/phone requires OTP to new contact.**DoD**: Update only after OTP verified; old tokens invalidated.|BE ▶️ Temp contact fields, verification endpoint.|
|**A‑20**|Admin “View As Customer”|Admin can impersonate a customer session (read‑only).**DoD**: Impersonation token, banner warning, audit record.|BE ▶️ JWT with `impersonated_user_id` claim.FE ▶️ Red banner + exit button.|
|**A‑21**|API Rate Limiting & Throttling|Protect auth endpoints (≤10 req/min/IP).**DoD**: 429 on exceed; logs; env configurable.|BE ▶️ Bucket4j filter; integration test.|
|**A‑22**|Accessibility Compliance for Auth UIs|WCAG 2.1 AA for all auth & profile pages.**DoD**: Axe‑core CI passes; keyboard nav; ARIA labels.|FE ▶️ Fix labels, focus traps; QA ▶️ Axe scan.|

---


## **Epic B – Feature Tabs & Customer Journeys

(Granular Backlog with Full Descriptions, DoD & Sub‑Tasks)**

---

> **Legend for Sub‑tasks**  
> • **FE** – Front‑end (React + TypeScript) • **BE** – Back‑end (Spring Boot) • **QA** – Quality Assurance & Integration  
> (If a task spans FE and BE, list it under both headings to keep ownership unambiguous.)

---

### **Section B‑1 Account Overview Tab**

|ID|Title|User Story|Definition of Done|Sub‑Tasks|
|---|---|---|---|---|
|**B1‑1**|**Accounts Card Grid**|_As a customer I want each of my accounts summarised as responsive cards so that I can quickly see my balances at a glance._|‑ Cards render < 500 ms with skeleton loader. ‑ Each card shows masked account number, balance (formatted), account type icon, currency symbol. ‑ Clicking a card sets it as the “current account” in global state. ‑ Unit tests ≥ 90 % for component.|**FE** Build `AccountCard` + `AccountGrid`; state hook in Context. **BE** `GET /accounts/summary` returns array with id, type, balance, currency, maskedNo. **QA** Contract test; Cypress visual regression.|
|**B1‑2**|**Multi‑Account Toggle**|_As a customer with more than one account I need a simple toggle/drop‑down so I can switch the context for all Account‑Overview sub‑components._|‑ Dropdown lists all active accounts. ‑ Selecting fires global event → re‑queries transactions & charts. ‑ URL reflects `?accountId=` param for deep‑linking.|**FE** Create `AccountSelector` tied to router. **BE** Same endpoint returns `isPrimary` flag; update docs. **QA** Cypress: ensure URL reload restores selected account.|
|**B1‑3**|**Paginated Transaction Table**|_As a customer I want to scroll through my transactions quickly without long load times._|‑ Infinite scroll or pagination (page size = 50). ‑ Columns: Date, Narration, Debit, Credit, Balance. ‑ Sorting by Date default desc; sortable on columns. ‑ Empty‑state artwork when none.|**FE** Virtualised list using `react‑window`. **BE** `GET /transactions?accountId=&page=&size=&sort=`. **QA** Back‑end perf test p95 < 200 ms for 1 k rows.|
|**B1‑4**|**Transaction Filtering & Search**|_As a customer I want to filter transactions by amount, keyword and date range so that I can find specific entries._|‑ UI filter drawer with min/max amount, keyword, date range picker. ‑ Combined query executed server‑side; results update table & count badge. ‑ Clear‑filters button resets state.|**FE** Filter drawer; redux slice for filters. **BE** Add filter params; text search index on `narration`. **QA** SQL injection negative tests; a11y of filter drawer.|
|**B1‑5**|**CSV / PDF Transaction Export**|_I need to export filtered transactions for bookkeeping._|‑ “Export” button enabled when ≥ 1 result. ‑ Server streams CSV or PDF (bank letterhead) < 10 MB. ‑ Signed URL expires after 5 minutes.|**FE** Dropdown (CSV/PDF) → call export API → trigger download. **BE** Streaming writer (OpenCSV & iText); S3 temp object with pre‑signed URL. **QA** Compare exported total vs UI total.|
|**B1‑6**|**Balance Trend Line Chart**|_I want a visual of balance over time so I can recognise spending patterns._|‑ React‑Chart renders line with debit/credit toggle. ‑ Time buckets: 7 d, 30 d, 90 d, custom. ‑ Tooltip shows date, closing balance. ‑ Handles > 5 k points by down‑sampling.|**FE** `TrendChart` component; skeleton loader. **BE** Aggregation query endpoint `/accounts/{id}/trend`. **QA** Snapshot test of chart; performance test for 1 year range.|
|**B1‑7**|**Upcoming Payment Alerts**|_I want alerts for EMIs or scheduled transfers due in the next 30 days so I never miss a payment._|‑ Alert list appears above cards when ≥ 1 alert. ‑ Each alert has due‑date badge (red if < 3 days). ‑ Dismiss persists per user (local + server flag).|**FE** Alert banner component, dismiss action. **BE** Cron job inserts `payment_alert` rows nightly; `/alerts?type=payment`. **QA** Edge case: exactly at 30 days shows.|
|**B1‑8**|**Real‑Time Balance Push**|_When my balance changes I want to see it without refreshing._|‑ WebSocket/STOMP pushes `BalanceUpdate` events. ‑ Card flashes green (credit) or red (debit) for 2 s. ‑ Reconnect logic with exponential back‑off.|**FE** WS hook; animation; toast on failure. **BE** Kafka→WS bridge publishes after core banking event. **QA** Simulate push in Cypress; disconnect/reconnect test.|
|**B1‑9**|**Mini‑Statement PDF Download**|_I want a quick 10‑transaction PDF I can print._|‑ Button on card “Mini statement”. ‑ PDF ≤ 2 pages, < 150 kB. ‑ Digital signature field embedded.|**BE** iText template generator. **FE** Call & download. **QA** Verify format, digital sig validity.|
|**B1‑10**|**Tax Summary Report**|_I need an annual interest & TDS report for tax filing._|‑ Year picker default current FY. ‑ Generates PDF with summary table, QR code verification. ‑ Stored for 7 years in S3.|**BE** `/reports/tax-summary?year=` endpoint. **QA** Data accuracy test vs sample fixture.|
|**B1‑11**|**Accessibility & Keyboard Navigation**|_Visually impaired users must fully operate Account‑Overview via keyboard & screen reader._|‑ All interactive elements reachable via Tab order. ‑ ARIA labels on cards/table rows. ‑ Axe‑core scan score ≥ 95 %.|**FE** Add focus styles, ARIA. **QA** Axe pipeline integration.|
|**B1‑12**|**Graceful Empty & Error States**|_When there is no data or an error, I want meaningful messaging rather than a blank page._|‑ Empty illustrations & call‑to‑action when 0 accounts. ‑ Retry button on network error. ‑ 500 errors logged to Sentry.|**FE** Placeholder components. **BE** Return RFC7807 problem JSON on error.|

---

### **Section B‑2 Product Applications Tab**

|ID|Title|User Story|Definition of Done|Sub‑Tasks|
|---|---|---|---|---|
|**B2‑1**|**Product Catalog Grid**|_As a customer I want to browse available financial products in a visually engaging grid so I can choose what suits me._|‑ Cards show product icon, name, teaser rate, CTA “Apply”. ‑ Category filter chips (Loans, Cards, Deposits, Investments). ‑ Lazy‑load images.|**FE** `ProductGrid`, category context. **BE** `GET /products?category=` returns marketing meta. **QA** Lighthouse perf ≤ 2 s.|
|**B2‑2**|**Product Detail Drawer**|_Clicking a product reveals key details and eligibility highlights._|‑ Drawer slides from right; includes FAQ accordion. ‑ CTA leads to questionnaire wizard.|**FE** Drawer component; route management.|
|**B2‑3**|**Dynamic Eligibility Questionnaire Core**|_Questionnaire should adapt per product so one code‑path serves many products._|‑ Questionnaire schema JSON delivered by BE. ‑ Form generated with `react‑jsonschema‑form`. ‑ Field‑level validation from schema.|**FE** Generic `DynamicForm`. **BE** `/products/{id}/questionnaire-schema`.|
|**B2‑4**|**Client‑Side Validation & UX**|_I expect instant feedback if I miss a required field._|‑ Red asterisks for required; inline error text; prevents next‑page nav.|**FE** Form config; unit tests.|
|**B2‑5**|**Document Upload Step**|_I must upload mandatory docs before submission, matched to checklist._|‑ Checklist auto‑ticks on upload success. ‑ Accepts PDF/JPEG/PNG ≤ 5 MB each. ‑ Drag‑and‑drop area, progress bar.|**FE** Upload component; BE pre‑signed S3 URL util. **BE** Persist `application_document` row.|
|**B2‑6**|**Real‑Time Rule Validation Integration**|_While I fill the form I want immediate “pass/fail” indicators so I don’t waste time._|‑ On every page change, FE sends snapshot to `/rules/validate`. ‑ Response array of ruleErrors; show inline banners. ‑ API SLA < 300 ms.|**BE** Implement validation controller, Redis memoise. **QA** Perf test 1 k QPS.|
|**B2‑7**|**Submit Application & Reference ID**|_After completing all steps, I submit and receive a reference number._|‑ POST returns `applicationId`, `referenceNo`. ‑ Application status = SUBMITTED. ‑ Confirmation screen, email sent.|**BE** `POST /applications`; event to Kafka. **FE** Success page.|
|**B2‑8**|**Duplicate Application Detection**|_I should not be able to submit the same product twice in 24 h._|‑ BE checks (customerId, productId, submittedAt < 24 h). ‑ Returns 409 with existing appl ref. ‑ FE shows “Already in progress” modal.|**BE** Unique constraint/index & rule.|
|**B2‑9**|**Application Status Timeline**|_I want to track my application through review stages._|‑ Stepper: Submitted → UnderReview → Approved/Rejected. ‑ Poll every 15 s, WS overrides. ‑ Timestamp on each step.|**FE** `StatusStepper`; BE `/applications/{id}/status`.|
|**B2‑10**|**Cancel Application Flow**|_If I change my mind before approval I can cancel._|‑ Allowed only when status in {Submitted, UnderReview}. ‑ BE sets status=CANCELLED; reason stored. ‑ FE confirm dialogue.|**BE** `PATCH /applications/{id}/cancel`.|
|**B2‑11**|**Loan Application Extra Fields**|_Loan products need amount, tenure, collateral._|‑ Fields in schema; EMI preview widget. ‑ Collateral upload optional.|**FE** EMI calc util; BE validation rule.|
|**B2‑12**|**Credit Card Limit Check**|_Credit card apps should instantly show max eligible limit._|‑ BE returns `maxLimit` after credit‑score check. ‑ FE slider capped.|**BE** Integrate credit bureau API.|
|**B2‑13**|**Fixed Deposit Application**|_I can open an FD selecting tenure and payout frequency._|‑ Maturity date auto‑calc. ‑ Interest rate fetched per tenure slab.|**BE** FD service; FE form.|
|**B2‑14**|**Mutual Fund KYC Verification**|_MF apps blocked if MF‑KYC is incomplete._|‑ External API call; error message with link to instructions.|**BE** Feign client; FE banner.|
|**B2‑15**|**Application Confirmation Email**|_An email confirmation with summary PDF should reach me._|‑ HTML email + PDF attached. ‑ SES or SMTP integration; retry queue.|**BE** Email service; template in Thymeleaf.|
|**B2‑16**|**Application Summary PDF Download**|_From the success screen I can download a PDF snapshot._|‑ PDF matches confirmation email; < 300 kB.|**BE** Generate on demand; FE button.|

---

### **Section B‑3 Service Requests Tab**

|ID|Title|User Story|Definition of Done|Sub‑Tasks|
|---|---|---|---|---|
|**B3‑1**|**Service Request Creation Form**|_As a customer I need an intuitive form to raise requests like address change._|‑ Categories & sub‑categories fetched from BE. ‑ Required fields validated. ‑ Ticket ID assigned.|**FE** `TicketForm`, dropdowns. **BE** `POST /tickets`.|
|**B3‑2**|**Category & Urgency Routing**|_Requests route automatically to correct queue based on category + urgency._|‑ Rule sets `assignedTeam`, `slaDeadline`. ‑ SLA displayed in UI.|**BE** Routing rule; FE badge.|
|**B3‑3**|**Attach Supporting Documents**|_I can add proofs to my ticket._|‑ Same validation rules as apps. ‑ Docs visible as thumbnails.|**FE** upload; **BE** S3 linkage.|
|**B3‑4**|**Live Chat Widget**|_I can chat with support within the ticket._|‑ WebSocket chat; typing indicator. ‑ Unread badge in tab header.|**FE** Chat UI; **BE** chat persistence.|
|**B3‑5**|**Ticket List with Filters**|_I can see all my tickets and filter by status/date._|‑ Virtual list, default sort by updatedAt desc.|**FE** list component; **BE** query params.|
|**B3‑6**|**Ticket Timeline View**|_I want a chronological timeline of status changes & chat._|‑ Vertical timeline component. ‑ Collapsible sections for attachments.|**FE** `Timeline` component; **BE** `/tickets/{id}/timeline`.|
|**B3‑7**|**Agent Reply & Status Update**|_Agents can reply and change status; customers see updates instantly._|‑ WS push to customer. ‑ Auth check for agent role.|**BE** update endpoints; FE show toast.|
|**B3‑8**|**SLA Countdown Timer**|_See live countdown until SLA breach._|‑ Timer changes colour (green→orange→red). ‑ Auto refresh time sync hourly.|**FE** timer hook; **BE** provides `slaDeadline`.|
|**B3‑9**|**Escalation Rule Integration**|_If SLA lapses ticket auto‑escalates._|‑ Background scheduler; status→ESCALATED; managers notified.|**BE** job; rule.|
|**B3‑10**|**Close Ticket & Feedback Survey**|_When resolved I can close and rate the resolution._|‑ 1–5 stars + optional comments. ‑ After submit, ticket locked read‑only.|**FE** modal; **BE** survey table.|
|**B3‑11**|**Duplicate Request Block**|_Cannot raise identical ticket within 24 h._|‑ 409 response with existing ticket link.|**BE** unique check; FE toast.|
|**B3‑12**|**Ticket Export to PDF**|_I need a PDF of ticket conversation for my records._|‑ PDF includes chat, attachments list, status history.|**BE** generator; FE trigger.|
|**B3‑13**|**Notification of Ticket Updates**|_Bell icon should reflect open ticket updates._|‑ Push increments count, click resets.|**FE** global notification store; **BE** event.|
|**B3‑14**|**Audit Trail for Ticket Changes**|_Auditors can download CSV of all changes._|‑ CSV zip delivered via email link.|**BE** batch job; FE request button.|

---

### **Section B‑4 Investment Dashboard Tab**

|ID|Title|User Story|Definition of Done|Sub‑Tasks|
|---|---|---|---|---|
|**B4‑1**|**Portfolio Overview Header**|_I want to see total invested amount, current value and XIRR at a glance._|‑ Header cards render < 400 ms. ‑ XIRR formula validated against spreadsheet sample.|**FE** header component; **BE** `/investments/summary`.|
|**B4‑2**|**Asset Allocation Pie Chart**|_A pie chart shows how my money is split across asset classes._|‑ Segments labelled, legend accessible. ‑ Slice explodes on hover.|**FE** `PieChart`; **BE** data agg.|
|**B4‑3**|**Gains/Losses Bar Chart**|_Bar chart of monthly gains/losses._|‑ Negative values coloured differently; tooltip shows pct.|**FE** chart; **BE** endpoint.|
|**B4‑4**|**Time Range Picker**|_Filter charts by predefined or custom date range._|‑ Picker component; charts refresh; URL query param.|**FE** date picker; **BE** range param support.|
|**B4‑5**|**SIP Schedule Table**|_I can view upcoming SIP debits._|‑ Export to ICS file.|**FE** table; **BE** `/sip/schedule`.|
|**B4‑6**|**Maturity Alerts Banner**|_Alerts for FDs/bonds maturing soon._|‑ Banner appears X≤ 30 days; dismiss persists.|**FE** banner; **BE** cron job.|
|**B4‑7**|**Consolidated Statement PDF**|_Download single PDF of all holdings._|‑ Signed, ≤ 5 MB, stored S3.|**BE** generator; FE trigger.|
|**B4‑8**|**Add External Investment Flow**|_I can manually add investments not held with the bank._|‑ Form for asset type, purchase price, date. ‑ Editable & deletable.|**FE** modal; **BE** CRUD endpoints; flag `external=true`.|
|**B4‑9**|**Real‑Time Price Feed Integration**|_See up‑to‑date NAV or market prices._|‑ Pulls price feed every 5 min; updates charts. ‑ Fallback to last known price on error.|**BE** price feed service; **FE** WS push.|
|**B4‑10**|**Recommendation Engine Nudges**|_Receive personalised suggestions (e.g., “Increase debt allocation”)._|‑ Engine returns list; cards carousel; dismissible.|**BE** rule engine; **FE** UI.|
|**B4‑11**|**Asset Detail Drawer**|_Clicking an asset opens a drawer with transaction list and graphs._|‑ Includes “Sell / Top‑up” buttons (stub).|**FE** drawer; **BE** `/investments/{id}/details`.|
|**B4‑12**|**Portfolio CSV Export**|_Export raw portfolio to CSV._|‑ Matches onscreen filters; < 1 MB.|**BE** writer; **FE** export button.|
|**B4‑13**|**Manual Refresh Button**|_I can force refresh of investment data._|‑ Button shows spinner; disables during call; last refreshed timestamp updates.|**FE** button; **BE** cache‑bust param.|

---

### **Section B‑5 Secure Messaging Tab**

|ID|Title|User Story|Definition of Done|Sub‑Tasks|
|---|---|---|---|---|
|**B5‑1**|**Thread List View**|_I want to see all message threads with unread counters._|‑ Virtualised list, sorted by last message desc.|**FE** `ThreadList`; **BE** `/messages/threads`.|
|**B5‑2**|**Search & Sort Threads**|_Search by name, account or keyword._|‑ Debounce 300 ms; highlight matches.|**FE** search input; **BE** ES query.|
|**B5‑3**|**Compose Message Editor**|_Rich‑text editor with basic formatting._|‑ Bold/italic, numbered list. ‑ Draft auto‑saves every 10 s.|**FE** Quill integration; local IndexedDB.|
|**B5‑4**|**File Attachment Upload**|_Attach up to 10 MB across files._|‑ Progress bar, MIME validation, thumbnail preview.|**FE** upload; **BE** S3 util.|
|**B5‑5**|**WebSocket Push for New Messages**|_New messages appear in real‑time._|‑ < 2 s latency; toast notification.|**BE** STOMP; **FE** hook.|
|**B5‑6**|**Mark Read / Unread**|_Toggle read status and update counters._|‑ Counter decrements instantly; server persists.|**FE** action; **BE** endpoint.|
|**B5‑7**|**Soft Delete Message**|_I can delete my message; it hides from me but retained for audit._|‑ BE flag `is_visible=false` per user.|**BE** API; **FE** fade‑out animation.|
|**B5‑8**|**Notification Center Counter**|_Global bell shows unread message count._|‑ Counter updates via WS; resets on view.|**FE** global store; **BE** event.|
|**B5‑9**|**Draft Persistence (Offline)**|_Draft survives page reload or offline state._|‑ IndexedDB store; syncs when online.|**FE** service worker integration.|
|**B5‑10**|**Attachment Encryption at Rest**|_All message attachments encrypted with KMS._|‑ Verify `x-amz-server-side-encryption : aws:kms`.|**BE** S3 client config; **QA** headers test.|
|**B5‑11**|**Conversation PDF Export**|_Export entire thread to PDF._|‑ Includes sender, timestamp, inline images.|**BE** HTML→PDF; **FE** button.|
|**B5‑12**|**Thread Tagging & Archiving**|_Tag threads (e.g., “Loan”, “Support”) and archive old ones._|‑ Tag multi‑select; archived hidden by default.|**FE** tagging UI; **BE** tags array; archive flag.|
|**B5‑13**|**Accessibility Compliance**|_Messaging area meets WCAG 2.1 AA._|‑ Screen‑reader labels; keyboard shortcuts (Ctrl+Enter send).|**FE** a11y fixes; **QA** Axe tests.|

---

### **Using This Epic Backlog**

_Load the stories directly into your tracker; every item is INVEST‑compliant (Independent, Negotiable, Valuable, Estimable, Small, Testable)._  
You can now plan sprint‑level swim‑lanes per tab, assign FE/BE pairs, and track progress with absolute clarity.

## **Epic C – Rules Engine & Business Validation

(Granular Backlog with Full Descriptions, DoD & Sub-Tasks)**

---

> **Legend for Sub-tasks**  
> • **FE** – Front-end (React + TypeScript) • **BE** – Back-end (Spring Boot & Drools) • **QA** – Quality Assurance & Integration

---

### **Section C-1 – Service Foundation & Bootstrap**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C1-1**|**Drools Service Bootstrap**|_As a platform architect, I need a standalone Drools-based microservice so that all rule evaluation is decoupled._|- Spring Boot service skeleton up and running. - `/health` endpoint returns `UP`. - Sample DRL rule “AlwaysPass.drl” loaded and evaluated correctly by a unit test.|**BE**: Create `rules-service` module; include Drools dependencies; health actuator. **BE**: Add sample DRL & JUnit test invoking `KieSession`. **QA**: Integration test for `/rules/validate`.|

---

### **Section C-2 – Rule Administration UI**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C2-1**|**Rule Admin CRUD UI**|_As an Admin I want a UI to create, read, update, and delete rule sets so that I can manage business logic without a deploy._|- Page lists existing rule sets (name, version, status). - Create/Edit opens Monaco YAML editor with schema validation. - Delete prompts confirmation. - Unit tests ≥ 90 %.|**FE**: Build `RuleList`, `RuleEditor` components using Monaco. **BE**: Expose `GET/POST/PUT/DELETE /rulesets`. **QA**: Cypress tests covering CRUD flows; contract tests.|
|**C2-2**|**Rule Version History Panel**|_As an Auditor I want to view version history of a rule set to track changes over time._|- History tab shows list of versions with timestamps & author. - “View diff” between any two versions opens side-by-side comparison.|**FE**: Add `HistoryTab` with diff viewer (e.g. `react-diff-viewer`). **BE**: `GET /rulesets/{id}/versions`. **QA**: Verify diffs accurate.|

---

### **Section C-3 – Rule Export, Import & Simulation**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C3-1**|**Bulk Rule Import/Export**|_As an Admin I need to import/export rule-sets in bulk so I can move rules between environments easily._|- Export button downloads ZIP of DRL+metadata. - Import dialog accepts ZIP, validates schema and IDs, rejects duplicates. - Errors shown inline.|**FE**: File upload dialog, progress bar. **BE**: `POST /rulesets/import`, `GET /rulesets/export`. **QA**: Upload malformed ZIP, missing files.|
|**C3-2**|**Rule Simulation Sandbox**|_As an Admin I want to test rules against sample data so I can verify logic before activation._|- Sandbox page allows JSON payload upload or manual entry. - “Run” invokes `/rules/simulate` and shows pass/fail results with rule messages. - Response time < 500 ms.|**FE**: JSON editor + “Run” button + results table. **BE**: `POST /rules/simulate`, invoke Drools and return list of `RuleResult`. **QA**: Test edge cases payloads.|

---

### **Section C-4 – Rule Lifecycle & Governance**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C4-1**|**Rule Change Approval Workflow**|_As a Compliance Officer I want a two-step approval before rules go live to enforce governance._|- New/updated rule-sets are created in `PENDING` state. - Approver sees list in UI with “Approve”/“Reject”. - State transitions (`PENDING`→`ACTIVE`/`REJECTED`) audited.|**FE**: Approval queue page; action buttons. **BE**: `PATCH /rulesets/{id}/status`; email notifications. **QA**: Ensure unauthorized cannot approve.|
|**C4-2**|**Rule Versioning & Rollback**|_As an Admin I want to rollback to any previous rule version to mitigate issues quickly._|- Version dropdown in Rule Detail UI. - Selecting older version prompts “Rollback?” confirmation. - Active pointer updates, notifications emitted.|**FE**: Dropdown + confirm modal. **BE**: `PATCH /rulesets/{id}/activate-version`. **QA**: Confirm rules engine uses correct version after rollback.|

---

### **Section C-5 – Core Business Rules Implementation**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C5-1**|**Loan Eligibility Rule**|_As a customer I should be rejected for a loan if creditScore < 650 or EMI / income > 50 %._|- DRL file `LoanEligibility.drl` implemented. - JUnit tests cover boundary values (649,650,651; income ratios). - Integrated in `/rules/validate`.|**BE**: Write DRL; add unit tests using Drools test harness. **QA**: Validate sample payloads.|
|**C5-2**|**Credit Card Eligibility Rule**|_As a customer only age ∈ [21,60] and salary > ₹30 000 may apply for a card._|- `CreditCardRule.drl` covers age & salary. - Tests for edge ages and salary thresholds.|**BE**: Implement DRL; tests. **QA**: UI shows correct inline error.|
|**C5-3**|**Investment Age Restriction Rule**|_As a customer > 65 I cannot invest in high-risk products._|- Rule file `InvestmentAge.drl`. - Tests: age=65 allow, age=66 block.|**BE**: DRL; tests.|
|**C5-4**|**KYC Completion Block Rule**|_As a customer I cannot apply if KYC status ≠ VERIFIED._|- `KycCompletion.drl` checks user.KYCStatus. - Inline “Complete your KYC” message returned.|**BE**: DRL; tests.|
|**C5-5**|**Duplicate Service Request Rule**|_As a customer I should be prevented from raising the same ticket within 24 h._|- Rule rejects if matching (customerId, type) exists in last 24 h. - Tests for exactly 23h59m vs 24h01m.|**BE**: DRL & DB unique check; tests.|
|**C5-6**|**Income-to-Loan Ratio Rule**|_As a customer my EMI must not exceed 40 % of net income._|- `IncomeLoanRatio.drl` implements ratio check. - Tests for 39.9 %, 40 %, 40.1 %.|**BE**: DRL; tests.|
|**C5-7**|**SLA Escalation Rule**|_As an agent a ticket older than 48 h should auto-escalate._|- Batch job runs every hour, evaluates `Ticket.openTime`. - Updates status to `ESCALATED`, emits event.|**BE**: Scheduler + rule; tests. **QA**: Simulate aging in test.|

---

### **Section C-6 – Observability & Performance**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C6-1**|**Rule Performance Metrics**|_As a DevOps engineer I want metrics on rule evaluation latency to monitor SLA adherence._|- Micrometer timers around Drools exec. - Exposed at `/actuator/metrics`. - Grafana dashboard template provided.|**BE**: Add timers; map metrics. **QA**: Verify timer values in metrics endpoint.|
|**C6-2**|**Circuit Breaker & Fallback**|_As a user I want a friendly message if the rules engine is unavailable._|- Integrate Resilience4j circuit breaker on `/rules/validate`. - Fallback returns HTTP 503 + user MSG “Validation temporarily unavailable”.|**BE**: Configure CB; fallback handler. **QA**: Simulate rule service down.|
|**C6-3**|**Caching of Frequent Validations**|_As a customer I want rule checks to be fast, avoiding repeated identical requests._|- Redis cache key on hash(inputs). - TTL = 30 min. - Cache hit ratio ≥ 80 % under load.|**BE**: Implement cache layer; tests. **QA**: Load test with repeat payloads.|
|**C6-4**|**High-Load Rule Scalability Test**|_As a performance engineer I need to ensure the rules-service handles 1 k RPS._|- Gatling script simulating concurrent validations. - 99 th percentile latency < 200 ms.|**QA**: Write & run Gatling tests; report.|

---

### **Section C-7 – Rule Discovery & Dependency Visualization**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**C7-1**|**Rule Dependency Graph UI**|_As an architect I want a visual DAG of rule dependencies so I can understand evaluation order._|- Graph renders nodes (rules) & edges. - Hover shows rule metadata. - Zoom & pan supported.|**FE**: Integrate Cytoscape or `react-flow`. **BE**: `GET /rules/dependencies` returning graph JSON. **QA**: Verify correct edges.|

---

### **How to Use This Epic Backlog**

- **Import** each story into your agile tracker; they are independent and TESTABLE.
    
- **Estimate** BE-only stories ~ 3-5 points, FE+BE stories ~ 5-8, infra/perf ~ 8-13.
    
- **Sprint Planning**: deliver Sections C-1 & C-5 first (core service + business rules), then UI & observability in parallel.
## **Epic D – Search & Document Services

(Granular Backlog with Full Descriptions, DoD & Sub-Tasks)**

---

> **Legend for Sub-tasks**  
> • **FE** – Front-end (React + TypeScript) • **BE** – Back-end (Spring Boot) • **QA** – Quality Assurance & Integration

---

### **Section D-1 – Indexing & Data Pipeline**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**D1-1**|**Batch Profile Indexing Job**|_As a platform engineer I need a nightly job to index all new/updated customer profiles into Elasticsearch so search queries stay up-to-date._|- Spring Batch job runs at 02:00 AM daily. - Reads from PostgreSQL `customers` table (only `updated_at` since last run). - Bulk indexes into ES `customer_index`. - Logs success/failure metrics to Grafana.|**BE**: Create Spring Batch job; implement reader, processor, writer. **QA**: Validate that new/updated profiles appear in ES; test injected failures.|
|**D1-2**|**Incremental Document Indexer**|_As a content manager I want new documents (KYC uploads, statement PDFs) automatically indexed so I can search them immediately._|- Kafka consumer listens on `document-uploaded` topic. - Extracts text via Tika, indexes to ES `document_index`. - Handles retries with DLQ on failure. - Monitoring alerts on DLQ > 100 messages.|**BE**: Implement Kafka listener + Tika extractor + ES client. **QA**: Upload sample docs; verify index; simulate parse error.|
|**D1-3**|**Reindex Utility Endpoint**|_As an admin I need an API to reindex a specific record or date range so I can fix indexing mistakes._|- `POST /admin/index/reindex` accepts `{type, id?, dateRange?}`. - Validates inputs, streams to batch job. - Returns jobId for status polling. - Job logs to audit.|**BE**: Add controller + service; hook into batch; job status table. **QA**: Trigger reindex of known record; confirm ES update.|

---

### **Section D-2 – Global Search UI & API**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**D2-1**|**Search Bar Component**|_As a customer I want a global search input in the header so I can quickly find accounts, tickets, or documents._|- Debounced (300 ms) input. - Shows latest 5 suggestions dropdown. - “Enter” executes full search view. - Mobile & desktop layouts.|**FE**: Build `SearchBar` with debounce & dropdown. **QA**: Keyboard nav & a11y checks; intercept API calls.|
|**D2-2**|**Search API Endpoint**|_As a developer I need a single `/search` API that dispatches queries to multiple indices so the frontend remains simple._|- `GET /search?q=&filters=` fan-out to ES indices. - Aggregates results into unified JSON with type tags (customer, ticket, doc). - P95 latency < 500 ms.|**BE**: Implement aggregator service; use async parallel ES calls. **QA**: Load test mixed queries; assert format.|
|**D2-3**|**Full Search Results Page**|_As a customer I want to see categorized search results (Accounts, Tickets, Documents) so I can navigate easily._|- Tabs or section headers for each type. - Result count badge per category. - “View more” link for > 10 hits.|**FE**: `SearchResults` page; pagination controls. **QA**: Responsive layout; empty-state messaging.|
|**D2-4**|**Search No-Results Suggestions**|_If my query returns no hits, I want helpful suggestions to improve search._|- Shows “Did you mean…” suggestions via ES `suggest` API. - FAQ links for common searches.|**FE**: Render suggestions; click re-search. **BE**: Add suggest call when hits=0.|
|**D2-5**|**Persistent Recent Searches**|_As a customer I want my last 10 search terms saved so I can quickly repeat them._|- Stores in localStorage and user profile via `POST /search/history`. - Displays dropdown of history on focus.|**FE**: Manage local + server store. **BE**: CRUD `/search/history`. **QA**: Sync behaviour offline.|

---

### **Section D-3 – Filters, Facets & Advanced**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**D3-1**|**Filter Panel UI**|_As a customer I want to filter search results by date range, type, status, and account so I can refine results._|- Collapsible panel with multi-select dropdowns and date picker. - “Apply” & “Clear All” buttons. - Debounce API call on change (500 ms).|**FE**: Build `SearchFilters` component. **BE**: Extend `/search` to accept filter params. **QA**: SQL injection fuzz tests.|
|**D3-2**|**Facet Counts & Drill-down**|_I want to see counts by category (e.g., 42 tickets, 17 docs) and click to drill-down._|- Displays facet counts alongside filters. - Clicking count adds filter & refreshes results.|**BE**: Add ES aggregations in search query. **FE**: Render counts; handle click.|
|**D3-3**|**Advanced Search Builder**|_As an auditor I need a UI to build complex boolean queries so I can find specific records._|- UI for AND/OR/NOT conditions, field selector dropdown. - Generates valid ES DSL JSON. - “Run” shows raw and formatted results.|**FE**: `AdvancedSearchBuilder` with tree UI. **BE**: Accept ES DSL via `POST /search/advanced`. **QA**: Validate DSL errors.|

---

### **Section D-4 – Document Text & Preview**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**D4-1**|**Full-Text Search Integration**|_As a customer I want to search inside uploaded documents so I can find relevant text in my PDFs._|- Document content extracted by Tika pipeline. - `/search` returns doc snippet with highlighted match. - Snippet ≤ 200 chars.|**BE**: Ensure Tika content stored in ES `document_index`. **QA**: Test on sample PDFs and scanned images.|
|**D4-2**|**OCR Pipeline for Scanned Docs**|_As a compliance officer I need scanned images OCR’d so they’re searchable._|- Tesseract or cloud OCR step triggered for JPEG/PNG. - Extracted text appended to ES index. - Failures moved to DLQ.|**BE**: Integrate OCR in consumer pipeline. **QA**: Scan images; verify searchability.|
|**D4-3**|**Document Preview Thumbnails**|_I want to see mini-previews of document pages in search results so I know which document to open._|- Thumbnails generated for first 3 pages as JPEGs, cached in S3. - FE shows carousel of ≤ 3 thumbnails. - Lazy-load images.|**BE**: Ghostscript thumbnail generator; S3 storage. **FE**: `ThumbnailCarousel`. **QA**: Verify image quality & load performance.|

---

### **Section D-5 – Secure Access & Export**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**D5-1**|**Signed URL for Document Download**|_As a user I need secure, time-limited links for document download so links cannot be abused._|- `GET /documents/{id}/download` returns pre-signed S3 URL valid 5 min. - Audit log entry on each request.|**BE**: Generate signed URL; persist audit row. **QA**: URL expiration test.|
|**D5-2**|**Bulk Document Export**|_I want to select multiple docs and download as a ZIP so I can batch-save files._|- FE allows multi-select list; “Export ZIP” triggers `POST /documents/export` with IDs. - BE streams ZIP; size limit 100 MB.|**FE**: Selection UI & download handler. **BE**: Zip streamer with temp S3 object. **QA**: Validate archive contents.|
|**D5-3**|**Audit Log CSV Export**|_As an auditor I need a CSV of all indexing & download events for compliance reviews._|- Filter by date range, user, action type. - CSV zipped; emailed or direct download.|**BE**: Batch export service; email link. **FE**: Request form. **QA**: Data accuracy vs DB logs.|

---

### **Section D-6 – Performance, Monitoring & Compliance**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**D6-1**|**Search SLA Monitoring**|_As an SRE I want alerts when search P95 latency exceeds 2 s so we can react to performance regressions._|- Grafana alert on ES query latency > 2 s for 5 m. - PagerDuty integration triggered.|**BE**: Instrument search API with OpenTelemetry. **QA**: Simulate latency; verify alert.|
|**D6-2**|**Cold Start Performance Test**|_As a performance engineer I need to measure ES cold cache latencies so we can tune caching._|- Gatling test hitting empty cache. - Report 95th percentile ≤ 300 ms.|**QA**: Write & run Gatling; analyze report.|
|**D6-3**|**Access Permissions Filter**|_As a system administrator I must ensure users only see search results they’re authorized to view._|- BE injects RBAC filter into ES query by user roles. - Tests: userA sees only own docs; admin sees all.|**BE**: Extend search service security filter. **QA**: Role-based search tests.|
|**D6-4**|**GDPR Data Removal Compliance**|_As a compliance officer I need deleted users’ documents auto-removed from search indices._|- On user deletion event, consumer deletes related docs from ES & S3 within 10 min. - Audit log of removal.|**BE**: Implement delete listener; QC job. **QA**: Delete test user; verify index cleanup.|
|**D6-5**|**Multi-Language Search Support**|_As a user I want to search in English and Hindi so I can use my preferred language._|- ES analyzer configured for English + Hindi. - FE language toggle on search. - Tests: Hindi terms match Hindi docs.|**BE**: Configure ICU & custom analyzers. **FE**: Language switch control. **QA**: Multilingual search scenarios.|

---

### **Using This Epic Backlog**

- **Import** each story into your tracker; all stories are INVEST-ready.
    
- **Estimate** BE-only ~ 3–5 pts, FE+BE ~ 5–8 pts, infra/perf ~ 8–13 pts.
    
- **Sprint Planning**: Start with D1 indexing core, then D2 UI & API, D4 docs pipeline, and wrap up D5/D6 non-functional in parallel.
## **Epic E – Non-Functional & Cross-Cutting Concerns

(Granular Backlog with Full Descriptions, DoD & Sub-Tasks)**

---

> **Legend for Sub-tasks**  
> • **FE** – Front-end (React + TypeScript) • **BE** – Back-end (Spring Boot, Infrastructure) • **QA** – Quality Assurance & Integration

---

### **Section E-1 – CI/CD & Release Automation**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E1-1**|**CI/CD Pipeline Setup**|_As a developer I want every PR to trigger a pipeline that builds, tests, and deploys to a preview environment so I get fast feedback._|- GitHub Actions workflow defined. - On PR: runs unit tests, lint, builds Docker images, pushes to registry. - On merge to main: deploy to `dev` namespace in K8s. - Notifications on fail/pass in Slack channel.|**BE**: Write `.github/workflows/ci.yml`; integrate Maven, npm build steps. **BE**: Add helm chart for preview namespace. **QA**: Validate pipeline passes on clean clone; test failure scenarios.|
|**E1-2**|**Release Notes Automation**|_As a release manager I want release notes generated from merged PR labels so I save manual effort._|- GitHub Action reads PR labels (feature, bug, chore). - Generates `CHANGELOG.md` snippet on tag push. - Snippet appended to release draft in GitHub.|**BE**: Configure GitHub Action YAML. **QA**: Merge sample PRs with labels; verify `CHANGELOG.md` updates correctly.|
|**E1-3**|**Dependency Upgrade Bot**|_As a team lead I want automated PRs for library updates so dependencies remain current._|- Renovate (or Dependabot) configured for backend (Maven) & frontend (npm). - Weekly PRs; semantic grouping. - PRs pass CI or auto-close if breaking.|**BE**: Add `renovate.json` config. **QA**: Verify weekly branch creation; test merging a minor/patch upgrade.|

---

### **Section E-2 – Observability & Monitoring**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E2-1**|**Observability Stack Deployment**|_As an SRE I want Grafana, Loki & Tempo deployed so I can visualize logs and traces._|- Helm charts installed for Grafana, Loki, Tempo in `monitoring` namespace. - Dashboards imported: service latency, error rates, throughput.|**BE**: Create Helm values files. **QA**: Verify dashboards populate with test traffic.|
|**E2-2**|**Tracing & Logging Integration**|_As a developer I want each service instrumented with OpenTelemetry so I can trace requests end-to-end._|- All Spring Boot and React apps include OTEL SDK. - HTTP spans created for key operations. - Logs enriched with trace and span IDs.|**BE**: Add OTEL auto-configuration. **FE**: Wrap fetch/axios with OTEL instrumentation. **QA**: Confirm traces in Tempo; correlate with logs in Grafana.|
|**E2-3**|**Cost Monitoring Dashboard**|_As a finance stakeholder I want AWS cost metrics in Grafana so I can track monthly spend._|- Lambda function queries Cost Explorer daily. - Pushes metrics to Prometheus via Pushgateway. - Grafana dashboard shows daily & monthly spend.|**BE**: Implement AWS SDK script + CronJob. **QA**: Validate cost data matches AWS console; alert on budget breach.|
|**E2-4**|**Chaos Engineering Experiments**|_As an SRE I want to inject latency and pod kills so I can test system resilience and alerting._|- LitmusChaos or Chaos Mesh installed. - Two experiments: 1) add 500 ms delay to `account-service`; 2) kill 1 pod of `rules-service`. - Alerts fire and recovery succeeds.|**BE**: Define ChaosExperiment CRDs. **QA**: Run experiments in staging; verify alerts and auto-heal.|

---

### **Section E-3 – Security & Compliance**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E3-1**|**Security Pen-Test Integration**|_As a security lead I want automated OWASP ZAP scans in CI so we catch common vulnerabilities early._|- ZAP stage in pipeline against deployed preview env. - Fails build on high severity. - Generates HTML report archived.|**BE**: Add ZAP CLI step in GH Actions. **QA**: Introduce known vulnerability; ensure ZAP fails build.|
|**E3-2**|**Static Code Analysis & Gate**|_As a developer I want SonarQube analysis in CI so code quality is enforced._|- Sonar scanner runs on each PR. - Quality gate: no new critical/blocker issues. - Badge in README updated.|**BE**: Configure `sonar-project.properties`. **QA**: Create dummy issue; verify gate blocks merge.|
|**E3-3**|**License Compliance Check**|_As a legal officer I want FOSSA scans so we catch forbidden licenses._|- FOSSA or equivalent step in CI. - Fails on MIT, Apache OK but GPL dependency flagged. - Report emailed to team.|**BE**: Integrate FOSSA CLI in pipeline. **QA**: Add test dependency with GPL; verify failure.|
|**E3-4**|**Privacy & Consent Banner**|_As a user I want to control cookies & trackers so my privacy preferences are respected._|- Banner on first visit, blocks GTM & analytics by default. - Stores consent in cookie/localStorage. - Respects ‘do not track’.|**FE**: Implement banner component; integrate iubenda or custom. **QA**: Verify trackers disabled until consent.|
|**E3-5**|**Terms & Conditions Versioning**|_As a compliance officer I need to track which T&C version each user accepted._|- T&C modal blocks until accept. - Version hash stored in `user_consent` table. - Users can view past versions.|**FE**: Modal & link to docs. **BE**: Consent table + API endpoints. **QA**: Change T&C; verify re-prompt.|
|**E3-6**|**Data Retention & Archiving**|_As a compliance officer I need to archive and then delete customer data after policy period._|- Soft-archive closed tickets > 2 years in PostgreSQL archive table & Glacier. - Purge job runs monthly. - Audit logs of removals.|**BE**: Batch job + AWS SDK. **QA**: Seed old data; run job; verify archive & deletion.|

---

### **Section E-4 – Infrastructure & Resilience**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E4-1**|**Infrastructure as Code (IaC)**|_As a DevOps engineer I want AWS & K8s resources defined in Terraform so environments are reproducible._|- Terraform modules for VPC, EKS, RDS, S3, Kafka. - `terraform plan/apply` idempotent. - State stored in S3+Lock in DynamoDB.|**BE**: Write Terraform code; configure backend. **QA**: Destroy & recreate staging; validate services.|
|**E4-2**|**Blue/Green Deployment**|_As a release manager I want zero-downtime deploys via traffic shifting so user impact is eliminated._|- Istio VirtualService configured for weight-based routing. - New version deployed in “green” subset; 100→0 shift. - Rollback option via `kubectl patch`.|**BE**: Write Istio manifests. **QA**: Deploy new version; monitor traffic shift; simulate failure; rollback.|
|**E4-3**|**Feature Flag Framework**|_As a product manager I want toggles in LaunchDarkly so we can release features gradually._|- SDK integrated in frontend & backend. - FLAG: `secure_messaging_beta`; default OFF. - Admin UI toggle affects traffic in < 30 s.|**BE**: Wire LD SDK; wrap feature code. **FE**: LD client init; HOC for flags. **QA**: Toggle on/off; verify behavior.|
|**E4-4**|**Disaster Recovery Plan & Drill**|_As an operations lead I need a documented recovery process and periodic drills so we meet RTO/RPO targets._|- Runbook in Confluence. - Quarterly drill: simulate region failure; restore DB from snapshot; rebuild cluster. - Drill report logged.|**BE**: Write runbook. **QA**: Execute drill in dev; measure RTO/RPO; report.|
|**E4-5**|**Automated Backup & Restore**|_As a DBA I want nightly backups of PostgreSQL and S3 so I can restore data if needed._|- RDS automated snapshots at 01:00. - S3 versioning + lifecycle to Glacier. - Lambda function tests restore weekly.|**BE**: Configure RDS & S3 policies; write Lambda restore script. **QA**: Trigger restore; compare data.|
|**E4-6**|**API Rate Limiting & Throttling**|_As a platform engineer I want Envoy rate limits so we protect downstream services from abuse._|- EnvoyFilter limits: 100 req/min per IP. - 429 responses with `Retry-After`. - Metrics in Prometheus.|**BE**: Write EnvoyFilter YAML. **QA**: Flood API; verify rate limit and header.|
|**E4-7**|**Service Mesh Mutual TLS (mTLS)**|_As a security engineer I want all internal service-to-service traffic encrypted so we meet compliance._|- Istio sidecars mTLS enabled cluster-wide. - Certificates rotated every 90 days automatically.|**BE**: Configure Istio `PeerAuthentication`. **QA**: Verify connections over TLS; attempt plain HTTP.|
|**E4-8**|**Correlation ID Propagation**|_As a support engineer I need `X-Correlation-ID` propagated across all services so I can trace requests end-to-end._|- All incoming HTTP calls extract or generate `X-Correlation-ID`. - Propagated in FE fetch headers & BE logs. - Present in Grafana dashboards.|**FE**: Axios interceptor. **BE**: Servlet filter + Feign interceptor. **QA**: Simulate request; confirm ID logged uniformly.|

---

### **Section E-5 – Performance & Scalability**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E5-1**|**Load & Performance Test Suite**|_As a performance engineer I need Gatling scripts covering key flows so we validate capacity under load._|- Scripts for login, search, rule validate, transaction listing. - CI nightly run with 1 k VU ramp. - Reports on P95 latency and error rate (< 1 %).|**QA**: Write Gatling scenarios; integrate into GH Actions nightly workflow.|
|**E5-2**|**End-to-End Latency Budgets**|_As an SRE I want defined latency SLOs so we measure against real user journeys._|- Define SLOs: search < 2 s P95; rule validation < 300 ms P95; account summary < 200 ms P95. - SLO dashboards in Grafana.|**QA**: Configure Grafana SLO dashboard; test against synthetic traffic.|

---

### **Section E-6 – Developer Experience & Documentation**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E6-1**|**Developer Documentation Portal**|_As a new engineer I want an MKDocs site with ADRs and API docs so I can onboard quickly._|- MkDocs site built from `/docs` folder. - CI job deploys to `docs.company.com`. - Search index of docs available.|**BE**: Add `mkdocs.yml` and CI publish step. **QA**: Validate links, search, PDF export.|
|**E6-2**|**On-boarding Scripts & Sample Data**|_As a junior engineer I want a one-command script to seed local env so I can start dev fast._|- `make seed` seeds PostgreSQL, ES, Kafka with demo data. - Creates 3 users (admin, customer, agent) + sample accounts.|**BE**: Write SQL/JSON seed scripts; extend `docker-compose` for local. **QA**: Fresh clone, run `make seed`, verify data.|
|**E6-3**|**Test Data Management**|_As a QA engineer I want anonymized production-like data for testing so I can run realistic scenarios._|- Mask names, IDs in production dump. - Script `data-anonymizer.py` generates safe dataset. - Stored in versioned S3.|**BE**: Write anonymizer script; document usage. **QA**: Run on real sample; confirm no PII remains.|

---

### **Section E-7 – Accessibility & Internationalization**

|ID|Title|User Story|Definition of Done|Sub-Tasks|
|---|---|---|---|---|
|**E7-1**|**Accessibility Audit & Remediation**|_As an accessibility specialist I want WCAG 2.1 AA compliance so users with disabilities can use the portal._|- Axe-core audit across all pages. - Fixes for contrast, ARIA labels, keyboard nav. - CI check ensures no new violations.|**FE**: Address audit findings. **QA**: Run Axe-CLI in CI; certificate when >= 95% compliance.|
|**E7-2**|**Internationalization (i18n)**|_As an end user I want the portal in English & Hindi so I can use my preferred language._|- All strings externalized with `react-i18next`. - Language toggle persists in user profile. - Hindi translations validated.|**FE**: Integrate `i18next`; extract keys. **QA**: Translate sample pages; verify missing keys.|
|**E7-3**|**Mobile Responsiveness**|_As a mobile user I want the portal usable on phones & tablets so I can manage accounts on the go._|- Breakpoints at 320, 768, 1024 px. - Critical pages achieve Lighthouse mobile score ≥ 90.|**FE**: CSS media queries, refactor layouts. **QA**: Run Lighthouse; test on device emulators.|
|**E7-4**|**Browser Compatibility Matrix**|_As a QA engineer I need support for Chrome, Edge, Safari 14+, Firefox so all users get consistent UX._|- CI matrix runs Cypress tests on each browser. - Fixes for polyfills & CSS as needed.|**QA**: Configure GitHub Actions browser matrix; address failures per browser.|

---

### **Using This Epic Backlog**

- **Import** stories directly into your tracker; all items are INVEST-ready.
    
- **Estimate**: BE-only ~ 3–5 pts; FE+BE ~ 5–8 pts; infra/perf ~ 8–13 pts.
    
- **Sprint Planning**: Tackle E-1 & E-4 first (CI/CD, IaC, resilience), then E-2 & E-3 (monitoring, security), and wrap up with E-5–E-7 across sprints to improve DX and UX.