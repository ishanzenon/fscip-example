# FSCIP – Business Requirement Document

---

## 1. Executive Summary

This document outlines the business requirements for the development of the **Financial Services Customer Interaction Portal (FSCIP)**, a web-based application for banking and financial institutions to manage customer profiles, provide self-service access to financial products, track service requests, and enhance compliance and transparency.

The portal will support user management, distinct functional tabs (e.g., account services, product applications, investment tracking), business rule-based workflow validation, upload/download capabilities for forms and documents, and global search.

---

## 2. Business Objectives

* Improve customer engagement and reduce dependency on physical branch visits.
* Streamline financial product access and request fulfillment.
* Enforce service and product eligibility rules.
* Increase transparency of customer-financial institution interactions.

---

## 3. Functional Requirements

### A. User Management

* **User Roles**: Admin, Customer, Relationship Manager, Support Agent, Auditor.
* **User Actions**:
    * Account registration and profile setup
    * Identity verification (KYC upload, OTP verification)
    * Role assignment and activation (Admin)
    * Password reset, two-factor authentication
    * View and download account history (Customer)
    * Deactivation/reactivation of users (Admin)
    * View access logs and audit history

### B. Feature Tabs

* **Account Overview Tab**
    * View account balances, transaction history, and credit/debit trends.
    * Download mini-statements and tax summaries.
    * See alerts for upcoming EMIs or due payments.
* **Product Applications Tab**
    * Apply for loans, credit cards, fixed deposits, or mutual funds.
    * Fill eligibility questionnaire and upload documents.
    * Get real-time feedback based on rule evaluation.
    * Track application status and next steps.
* **Service Requests Tab**
    * Raise and track tickets like address changes or limit enhancements.
    * Categorize service requests by urgency and department.
    * Attach supporting documents or chat with a support agent.
* **Investment Dashboard Tab**
    * Overview of financial portfolio (SIPs, FDs, bonds).
    * Track gains/losses over selected time periods.
    * Access consolidated statements and maturity alerts.
* **Secure Messaging Tab**
    * Communicate with relationship managers or support agents.
    * Send/receive secured messages and attachments.
    * Notification center for system alerts, approvals, or changes.

### C. Business Functionality: Product Eligibility & Service Rules Engine

* **Key business rules**:
    * Loan eligibility based on credit score and income.
    * Age-based investment product access.
    * Block application if KYC is incomplete.
    * Auto-rejection of duplicate service requests within 24 hours.
    * Rule-based document checklist per product.
    * SLA monitoring (e.g., auto-escalation after 48 hrs).
    * Income-to-loan ratio checks.
    * Validation of linked account status before approval.
* **The rules engine must**:
    * Allow admins to add/edit rules.
    * Be auditable with change logs.
    * Run efficiently on high transaction volumes.
    * Generate real-time rule validation feedback to users.

### D. Upload/Download Data

* **Upload**:
    * Upload income proofs, KYC, transaction statements (PDF, JPEG, XLS).
    * File size and format validation.
    * Provide error messages and a re-upload option.
    * Automatically tag uploads to the application context.
* **Download**:
    * Generate account reports, loan sanction letters, and tax forms.
    * Export transaction history by date and account type.
    * Download submitted documents and application snapshots.

### E. Global Search Feature

* **Search across**:
    * Customer name, account number, ticket ID, product name.
    * Full-text search in uploaded documents and messages.
* **Filters**: Time range, ticket status, product category.
* Predictive search suggestions and recent search history.

### F. Login/Logout Feature

* Customer and Admin login with OTP or password.
* Optional SSO for employees.
* Session timeout and activity log.
* Secure logout redirection and audit tracking.
* “Last login” information shown on the dashboard.

---

## 4. Use Case Scenarios

### Use Case 1: Customer Applies for a Credit Card
1.  Registers and logs in.
2.  Completes KYC upload.
3.  Navigates to Product Applications.
4.  Selects Credit Card and fills out the eligibility form.
5.  Uploads payslip and ID proof.
6.  The application passes rules check and is routed to the Relationship Manager (RM).

### Use Case 2: Support Agent Resolves Service Request
1.  Logs in as a Support Agent.
2.  Reviews an open request: "Incorrect address on statement."
3.  Verifies the document upload.
4.  Applies the update and closes the ticket with a message.

### Use Case 3: Auditor Reviews Rule Logs
1.  Logs in as an Auditor.
2.  Opens the Rules Engine section.
3.  Filters logs for "Loan Eligibility."
4.  Downloads the change history for compliance review.

---

## 5. Acceptance Criteria

* All user roles can perform defined actions according to their permissions.
* Product application forms are validated by the rules engine in real-time.
* Global search returns results in under 2 seconds.
* The upload feature allows a minimum of 5MB per file and a maximum of 5 files per action.