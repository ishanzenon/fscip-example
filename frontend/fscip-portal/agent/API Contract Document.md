Below is a comprehensive, unambiguous API contract for **all** the endpoints across Epics A–E. Your team can pick up any of these and begin implementation immediately.

---

## Epic A – Identity & Access Management

### 1. **Register New User**

**POST** `/auth/register`  
**Auth:** None  
**Headers:**

- `Content-Type: application/json`  
    **Request Body (JSON):**
    

```json
{
  "email": "user@example.com",         // required, valid email
  "password": "Str0ngP@ss!",          // required, min 8 chars, upper/lower/digit/special
  "fullName": "Jane Doe",             // required
  "mobile": "+911234567890"           // required, E.164 format
}
```

**Responses:**

- **201 Created**
    
    ```json
    {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "PENDING",
      "message": "OTP sent to email and mobile"
    }
    ```
    
- **400 Bad Request** – missing/invalid fields
    
- **409 Conflict** – email or mobile already registered
    

---

### 2. **Verify OTP**

**POST** `/auth/otp/verify`  
**Auth:** None  
**Headers:**

- `Content-Type: application/json`  
    **Request Body:**
    

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",  // required
  "otp": "123456"                                   // required, 6 digits
}
```

**Responses:**

- **200 OK**
    
    ```json
    {
      "userId": "550e8400-e29b-41d4-a716-446655440000",
      "status": "ACTIVE",
      "message": "User activated"
    }
    ```
    
- **400 Bad Request** – missing/invalid OTP
    
- **401 Unauthorized** – OTP expired or attempts exceeded
    

---

### 3. **Request Password Reset**

**POST** `/auth/password/reset-request`  
**Auth:** None  
**Headers:** `Content-Type: application/json`  
**Request Body:**

```json
{ "email": "user@example.com" }           // required
```

**Responses:**

- **200 OK**
    
    ```json
    { "message": "Reset link sent to email" }
    ```
    
- **404 Not Found** – no account with that email
    

---

### 4. **Confirm Password Reset**

**POST** `/auth/password/reset-confirm`  
**Auth:** None  
**Headers:** `Content-Type: application/json`  
**Request Body:**

```json
{
  "token": "reset-token-from-email",      // required
  "newPassword": "N3wStr0ngP@ss!"         // required, same complexity as register
}
```

**Responses:**

- **200 OK**
    
    ```json
    { "message": "Password reset successful" }
    ```
    
- **400 Bad Request** – invalid/expired token or weak password
    

---

### 5. **Toggle Two-Factor Authentication**

**PATCH** `/users/{userId}/2fa`  
**Auth:** Bearer JWT (must be same user or Admin)  
**Path Params:**

- `userId` (UUID)  
    **Headers:** `Content-Type: application/json`  
    **Request Body:**
    

```json
{ "enabled": true }      // required boolean
```

**Responses:**

- **200 OK**
    
    ```json
    { "userId":"…", "twoFactorEnabled": true }
    ```
    
- **403 Forbidden** – unauthorized toggle
    
- **404 Not Found** – userId not found
    

---

### 6. **Login**

**POST** `/auth/login`  
**Auth:** None  
**Headers:** `Content-Type: application/json`  
**Request Body:**

```json
{
  "username": "user@example.com",   // or mobile
  "password": "Str0ngP@ss!"
}
```

**Responses:**

- **200 OK**
    
    ```json
    {
      "accessToken": "eyJhb…", 
      "refreshToken": "dGhp…", 
      "expiresIn": 3600
    }
    ```
    
- **401 Unauthorized** – invalid credentials
    
- **423 Locked** – account locked (failed logins)
    

---

### 7. **Logout**

**POST** `/auth/logout`  
**Auth:** Bearer JWT  
**Headers:** none  
**Responses:**

- **204 No Content**
    

---

### 8. **Get My Profile**

**GET** `/users/me`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK**
    
    ```json
    {
      "userId":"…",
      "email":"user@example.com",
      "fullName":"Jane Doe",
      "mobile":"+911234567890",
      "roles":["Customer"],
      "status":"ACTIVE",
      "twoFactorEnabled": true,
      "lastLogin":"2025-07-21T14:32:00Z"
    }
    ```
    

---

### 9. **Update Profile**

**PATCH** `/users/me`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Request Body (any subset):**

```json
{
  "fullName": "Jane A. Doe",
  "address": {
    "line1": "123 Main St",
    "city": "Mumbai",
    "state": "MH",
    "postalCode":"400001",
    "country":"IN"
  }
}
```

**Responses:**

- **200 OK** – returns updated profile
    

---

### 10. **Change Email Request**

**POST** `/users/me/email/change-request`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "newEmail": "new@example.com" }
```

**Responses:**

- **200 OK** – OTP sent to newEmail
    
- **409 Conflict** – email already in use
    

---

### 11. **Confirm Email Change**

**POST** `/users/me/email/confirm`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "otp": "654321" }
```

**Responses:**

- **200 OK** – email updated
    

---

### 12. **Change Mobile Request**

**POST** `/users/me/mobile/change-request`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "newMobile": "+919876543210" }
```

**Responses:**

- **200 OK** – OTP sent to newMobile
    
- **409 Conflict** – mobile in use
    

---

### 13. **Confirm Mobile Change**

**POST** `/users/me/mobile/confirm`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "otp": "123456" }
```

**Responses:**

- **200 OK** – mobile updated
    

---

### 14. **Upload KYC Document**

**POST** `/kyc`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: multipart/form-data`  
**Form Data:**

- `type`: `"PAN"|"AADHAAR"|"PASSPORT"`
    
- `file`: binary (PDF/JPEG/PNG ≤ 5 MB)  
    **Responses:**
    
- **201 Created**
    
    ```json
    {
      "documentId":"…",
      "status":"PENDING_KYC"
    }
    ```
    
- **400 Bad Request** – invalid type/size
    

---

### 15. **Admin: Assign Roles**

**PUT** `/admin/users/{userId}/roles`  
**Auth:** Bearer JWT (Admin only)  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "roles": ["Customer","SupportAgent"] }
```

**Responses:**

- **200 OK** – updated roles
    
- **403 Forbidden** – not an Admin
    

---

### 16. **Admin: Activate/Deactivate User**

**PATCH** `/admin/users/{userId}/status`  
**Auth:** Admin  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "status": "ACTIVE"|"SUSPENDED" }
```

**Responses:**

- **204 No Content**
    

---

### 17. **View Audit Logs**

**GET** `/audit`  
**Auth:** Bearer JWT (Admin/Auditor)  
**Query Params:**

- `actorId` (UUID, optional)
    
- `eventType` (string, optional)
    
- `startDate`, `endDate` (ISO 8601, optional)
    
- `page`, `size` (pagination)  
    **Responses:**
    
- **200 OK**
    
    ```json
    {
      "total": 1234,
      "page": 0,
      "size": 50,
      "logs": [
        { "timestamp":"…", "actorId":"…", "event":"USER_REGISTER", "details":{…} },
        …
      ]
    }
    ```
    

---

### 18. **Export Audit Logs**

**POST** `/audit/export`  
**Auth:** Admin  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "startDate":"2025-07-01","endDate":"2025-07-21","format":"CSV" }
```

**Responses:**

- **202 Accepted**
    
    ```json
    { "jobId":"export-job-1234" }
    ```
    
- **404 Not Found** – no logs in range
    

---

### 19. **Consent to Terms & Privacy**

**POST** `/consents`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{ "type":"TOS"|"PRIVACY","version":"v1.2.0" }
```

**Responses:**

- **201 Created**
    

---

### 20. **GDPR: Request Account Deletion**

**POST** `/users/me/delete-request`  
**Auth:** Bearer JWT  
**Responses:**

- **202 Accepted** – “Your request is pending admin approval”
    

---

## Epic B – Feature Tabs & Customer Journeys

### **Account Overview**

#### 21. **Get Account Summaries**

**GET** `/accounts/summary`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK**
    
    ```json
    [
      {
        "accountId":"…",
        "type":"SAVINGS",
        "maskedNumber":"XXXXXX1234",
        "currency":"INR",
        "balance":12500.75
      },…
    ]
    ```
    

#### 22. **Get Transactions**

**GET** `/transactions`  
**Auth:** Bearer JWT  
**Query Params:**

- `accountId` (UUID, required)
    
- `page`, `size`, `sort` (e.g. `date,desc`)
    
- `minAmount`, `maxAmount` (optional)
    
- `keyword` (string, optional)
    
- `startDate`,`endDate` (ISO 8601, optional)  
    **Responses:**
    
- **200 OK**
    
    ```json
    {
      "total": 234,
      "page": 0,
      "size": 50,
      "transactions": [
        {
          "id":"…","date":"2025-07-20","description":"Coffee Shop",
          "debit":200.00,"credit":0.00,"balance":12300.75
        },…
      ]
    }
    ```
    

#### 23. **Export Transactions**

**POST** `/transactions/export`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{
  "accountId":"…",
  "startDate":"2025-07-01",
  "endDate":"2025-07-21",
  "format":"CSV"|"PDF"
}
```

**Responses:**

- **200 OK**
    
    ```json
    { "downloadUrl":"https://…signed-url…?expires=…” }
    ```
    

#### 24. **Get Balance Trend**

**GET** `/accounts/{accountId}/trend`  
**Auth:** Bearer JWT  
**Path Params:** `accountId` (UUID)  
**Query Params:**

- `range`: `7d|30d|90d` or `startDate,endDate`  
    **Responses:**
    
- **200 OK**
    
    ```json
    [
      { "date":"2025-07-01","balance":10000.00 },
      …
    ]
    ```
    

#### 25. **Get Upcoming Payment Alerts**

**GET** `/alerts?type=payment`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK**
    
    ```json
    [
      { "alertId":"…","dueDate":"2025-07-25","amount":5000,"description":"Home Loan EMI" },
      …
    ]
    ```
    

#### 26. **Mini-Statement PDF**

**GET** `/accounts/{accountId}/mini-statement`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – binary PDF stream, `Content-Type: application/pdf`
    

#### 27. **Tax Summary PDF**

**GET** `/reports/tax-summary?year=2024`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – binary PDF
    

---

### **Product Applications**

#### 28. **List Products**

**GET** `/products?category=LOANS`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK**
    
    ```json
    [
      { "id":"…","name":"Home Loan","teaserRate":7.5,"category":"LOANS" },
      …
    ]
    ```
    

#### 29. **Get Questionnaire Schema**

**GET** `/products/{productId}/questionnaire-schema`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – JSON-Schema document
    

#### 30. **Submit Application**

**POST** `/applications`  
**Auth:** Bearer JWT  
**Headers:** `Content-Type: application/json`  
**Body:**

```json
{
  "productId":"…",
  "answers": { /* key/value per schema */ },
  "documents": [
    { "type":"PAYSLIP","documentId":"…"},
    …
  ]
}
```

**Responses:**

- **201 Created**
    
    ```json
    {
      "applicationId":"…",
      "referenceNo":"APP-20250721-0001",
      "status":"SUBMITTED"
    }
    ```
    
- **409 Conflict** – duplicate within 24h
    

#### 31. **Get Application Status**

**GET** `/applications/{applicationId}`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK**
    
    ```json
    {
      "applicationId":"…","status":"UNDER_REVIEW",
      "history":[
        {"status":"SUBMITTED","timestamp":"…"},
        …
      ]
    }
    ```
    

#### 32. **Cancel Application**

**PATCH** `/applications/{applicationId}/cancel`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – status changes to `CANCELLED`
    

#### 33. **Download Application Summary**

**GET** `/applications/{applicationId}/summary-pdf`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – PDF stream
    

---

### **Service Requests**

#### 34. **Create Ticket**

**POST** `/tickets`  
**Auth:** Bearer JWT  
**Body:**

```json
{
  "category":"ADDRESS_CHANGE",
  "urgency":"LOW"|"MEDIUM"|"HIGH",
  "subject":"Incorrect address on statement",
  "description":"My street name is misspelt",
  "documents":[{"type":"UTILITY_BILL","documentId":"…"}]
}
```

**Responses:**

- **201 Created**
    
    ```json
    { "ticketId":"…","assignedTeam":"Support","slaDeadline":"2025-07-23T12:00:00Z" }
    ```
    

#### 35. **List My Tickets**

**GET** `/tickets?status=OPEN&page=0&size=20`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – paginated list
    

#### 36. **Get Ticket Timeline**

**GET** `/tickets/{ticketId}/timeline`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – chronological array of events & messages
    

#### 37. **Post Ticket Message**

**POST** `/tickets/{ticketId}/messages`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "message":"Please update my address","attachments":[…] }
```

**Responses:**

- **201 Created**
    

#### 38. **Close Ticket & Feedback**

**POST** `/tickets/{ticketId}/close`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "rating":4, "comments":"Thank you!" }
```

**Responses:**

- **200 OK** – ticket status= CLOSED
    

#### 39. **Export Ticket as PDF**

**GET** `/tickets/{ticketId}/export?format=PDF`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – PDF stream
    

---

### **Investment Dashboard**

#### 40. **Get Investment Summary**

**GET** `/investments/summary`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK**
    
    ```json
    {
      "totalInvested":500000,
      "currentValue":550000,
      "xirr":12.5
    }
    ```
    

#### 41. **Get Investment History**

**GET** `/investments/history?startDate=2025-01-01&endDate=2025-07-21`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – array of date/value pairs
    

#### 42. **Get SIP Schedule**

**GET** `/sip/schedule`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – list of upcoming SIP debits
    

#### 43. **Download Consolidated Statement**

**GET** `/investments/statement?format=PDF`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – PDF
    

#### 44. **Add External Investment**

**POST** `/investments/external`  
**Auth:** Bearer JWT  
**Body:**

```json
{
  "type":"STOCK"|"BOND",
  "purchaseDate":"2025-06-01",
  "amount":10000,
  "notes":"Private holding"
}
```

**Responses:**

- **201 Created**
    

---

### **Secure Messaging**

#### 45. **List Threads**

**GET** `/messages/threads?page=0&size=20`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – paginated threads with unread counts
    

#### 46. **Get Thread Messages**

**GET** `/messages/threads/{threadId}/messages?page=0&size=50`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – list of messages
    

#### 47. **Send Message**

**POST** `/messages/threads/{threadId}/messages`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "content":"Hello RM","attachments":[…] }
```

**Responses:**

- **201 Created**
    

#### 48. **Mark Message Read/Unread**

**PATCH** `/messages/{messageId}/read`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "read": true }
```

**Responses:**

- **200 OK**
    

#### 49. **Delete Message (Soft)**

**DELETE** `/messages/{messageId}`  
**Auth:** Bearer JWT  
**Responses:**

- **204 No Content**
    

#### 50. **Export Thread as PDF**

**GET** `/messages/threads/{threadId}/export?format=PDF`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – PDF stream
    

---

## Epic C – Rules Engine & Business Validation

#### 51. **List Rule Sets**

**GET** `/rulesets`  
**Auth:** Bearer JWT (Admin/Auditor)  
**Responses:**

- **200 OK** – array of `{ id, name, version, status }`
    

#### 52. **Create Rule Set**

**POST** `/rulesets`  
**Auth:** Admin  
**Body:**

```json
{ "name":"LoanEligibility","drl":"…DRL content…","status":"PENDING" }
```

**Responses:**

- **201 Created** – returns new `id`
    

#### 53. **Get Rule Set**

**GET** `/rulesets/{id}`  
**Auth:** Admin/Auditor  
**Responses:**

- **200 OK** – full rule set object
    

#### 54. **Update Rule Set**

**PUT** `/rulesets/{id}`  
**Auth:** Admin  
**Body:** same as create  
**Responses:**

- **200 OK**
    

#### 55. **Delete Rule Set**

**DELETE** `/rulesets/{id}`  
**Auth:** Admin  
**Responses:**

- **204 No Content**
    

#### 56. **Get Version History**

**GET** `/rulesets/{id}/versions`  
**Auth:** Auditor  
**Responses:**

- **200 OK** – array of versions with timestamps
    

#### 57. **Activate Version**

**PATCH** `/rulesets/{id}/versions/{version}/activate`  
**Auth:** Admin  
**Responses:**

- **200 OK**
    

#### 58. **Import Rule Sets**

**POST** `/rulesets/import`  
**Auth:** Admin  
**Headers:** `Content-Type: multipart/form-data` (ZIP)  
**Responses:**

- **200 OK** – import report
    

#### 59. **Export Rule Sets**

**GET** `/rulesets/export`  
**Auth:** Admin  
**Responses:**

- **200 OK** – ZIP download
    

#### 60. **Validate Rules**

**POST** `/rules/validate`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "productId":"…","payload":{…} }
```

**Responses:**

- **200 OK**
    
    ```json
    { "passed":false, "errors":[ {"rule":"LoanEligibility","message":"Income too low"} ] }
    ```
    

#### 61. **Simulate Rules**

**POST** `/rules/simulate`  
**Auth:** Bearer JWT  
**Body:** same as validate  
**Responses:**

- **200 OK** – raw decision data
    

#### 62. **Get Dependency Graph**

**GET** `/rules/dependencies`  
**Auth:** Bearer JWT (Admin)  
**Responses:**

- **200 OK** – graph JSON
    

---

## Epic D – Search & Document Services

#### 63. **Global Search**

**GET** `/search?q=term&filters=…`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** –
    
    ```json
    {
      "customers":[…],
      "tickets":[…],
      "documents":[…]
    }
    ```
    

#### 64. **Advanced Search**

**POST** `/search/advanced`  
**Auth:** Bearer JWT  
**Body:** ES DSL JSON  
**Responses:**

- **200 OK** – raw ES hits
    

#### 65. **Search Suggestions**

**GET** `/search/suggestions?q=ter`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – `[ "term1","term2",… ]`
    

#### 66. **Store Recent Search**

**POST** `/search/history`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "query":"mortgage status" }
```

**Responses:**

- **201 Created**
    

#### 67. **Get Recent Searches**

**GET** `/search/history`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – last 10 queries
    

#### 68. **Reindex Data**

**POST** `/admin/index/reindex`  
**Auth:** Admin  
**Body:**

```json
{ "type":"customer"|"document","id":"…","startDate":"2025-07-01","endDate":"2025-07-21" }
```

**Responses:**

- **202 Accepted** – `{ "jobId":"…" }`
    

#### 69. **Download Document**

**GET** `/documents/{id}/download`  
**Auth:** Bearer JWT  
**Responses:**

- **200 OK** – 5-min signed URL JSON:
    
    ```json
    { "url":"https://…?X-Amz-Expires=300" }
    ```
    

#### 70. **Bulk Document Export**

**POST** `/documents/export`  
**Auth:** Bearer JWT  
**Body:**

```json
{ "ids":["…","…"],"format":"ZIP" }
```

**Responses:**

- **200 OK** – signed ZIP URL
    

---

## Epic E – Non-Functional & Cross-Cutting

#### 71. **Health Check**

**GET** `/actuator/health`  
**Auth:** None  
**Responses:**

- **200 OK** – `{ "status":"UP" }`
    

#### 72. **Metrics (Prometheus)**

**GET** `/actuator/prometheus`  
**Auth:** None

#### 73. **JSON Metrics**

**GET** `/actuator/metrics`  
**Auth:** None

---

**Error Response Format (applies to all JSON endpoints):**

```json
{
  "timestamp":"2025-07-22T10:00:00Z",
  "status":400,
  "error":"Bad Request",
  "message":"Detailed error message",
  "path":"/path/that/failed"
}
```

---

> **Notes:**
> 
> - All `POST`/`PATCH`/`DELETE` require CSRF protection if not stateless JWT.
>     
> - All date/time fields are ISO 8601 UTC strings.
>     
> - All list endpoints are paginated with `page` (0-based) & `size`.
>     
> - Authentication via Bearer JWT in `Authorization` header.
>     
> - Role-based access enforced via Spring Security on each endpoint.
>     

This contract covers every function from Epics A–E with no ambiguity: your frontend and backend engineers can now scaffold controllers, services, DTOs, and clients straight away.