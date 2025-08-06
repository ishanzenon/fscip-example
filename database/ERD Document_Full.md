
## 🗺️ 1. Schema Overview & ERD

```text
[users]──<user_roles>──[roles]
   │
   ├──<otp_codes>
   ├──<password_reset_tokens>
   ├──<contact_change_requests>
   ├──<login_history>
   ├──<consents>
   ├──<kyc_documents>
   ├──<accounts>──<transactions>
   ├──<alerts>
   ├──<applications>──<application_documents>
   │                     └──<documents>
   ├──<tickets>──────<ticket_messages>──<message_attachments>──<documents>
   │    │
   │    └──<ticket_feedback>
   ├──<investments>──<sip_schedules>
   │       ├──<investment_history>
   │       └──<maturity_alerts>
   ├──<external_investments>
   ├──<threads>──<thread_participants>
   │     └──<messages>──<message_attachments>
   ├──<notifications>
   ├──<rulesets>──<rule_versions>
   │      └──<rule_evaluations>
   ├──<search_history>
   └──<audit_log>
```

---

### 1.1 Auth & Users

#### **users**

- **user_id** UUID PRIMARY KEY
    
- email VARCHAR(320) UNIQUE NOT NULL
    
- password_hash TEXT NOT NULL
    
- full_name VARCHAR(200) NOT NULL
    
- mobile VARCHAR(15) UNIQUE
    
- status USER_STATUS(‘PENDING’,‘ACTIVE’,‘SUSPENDED’,‘DELETED’)
    
- two_factor_enabled BOOLEAN DEFAULT TRUE
    
- last_login TIMESTAMP NULL
    
- created_at TIMESTAMP DEFAULT now()
    
- updated_at TIMESTAMP DEFAULT now()
    

#### **roles**

- **role_id** SERIAL PRIMARY KEY
    
- name VARCHAR(50) UNIQUE NOT NULL
    
- description TEXT
    

#### **user_roles**

- **user_id** UUID REFERENCES users(user_id) ON DELETE CASCADE
    
- **role_id** INT REFERENCES roles(role_id)
    
- PRIMARY KEY(user_id, role_id)
    

#### **otp_codes**

- **otp_id** SERIAL PK
    
- user_id UUID FK→users
    
- otp CHAR(6) NOT NULL
    
- expires_at TIMESTAMP NOT NULL
    
- attempts INT DEFAULT 0
    
- created_at TIMESTAMP DEFAULT now()
    

#### **password_reset_tokens**

- **token_id** SERIAL PK
    
- user_id UUID FK→users
    
- token UUID UNIQUE NOT NULL
    
- expires_at TIMESTAMP NOT NULL
    
- used BOOLEAN DEFAULT FALSE
    
- created_at TIMESTAMP DEFAULT now()
    

#### **contact_change_requests**

- **request_id** SERIAL PK
    
- user_id UUID FK→users
    
- type CHANGE_TYPE(‘EMAIL’,‘MOBILE’)
    
- new_value VARCHAR(320) NOT NULL
    
- otp CHAR(6) NOT NULL
    
- expires_at TIMESTAMP NOT NULL
    
- attempts INT DEFAULT 0
    
- created_at TIMESTAMP DEFAULT now()
    

#### **login_history**

- **login_id** SERIAL PK
    
- user_id UUID FK→users
    
- login_at TIMESTAMP DEFAULT now()
    
- ip_address INET
    
- user_agent TEXT
    

#### **consents**

- **consent_id** SERIAL PK
    
- user_id UUID FK→users
    
- type CONSENT_TYPE(‘TOS’,‘PRIVACY’)
    
- version VARCHAR(20) NOT NULL
    
- consented_at TIMESTAMP DEFAULT now()
    

---

### 1.2 Document & KYC

#### **documents**

- **document_id** UUID PK
    
- user_id UUID FK→users
    
- context_type CTX(‘APPLICATION’,‘TICKET’,‘MESSAGE’,‘PROFILE’,‘OTHER’)
    
- context_id UUID NULLABLE
    
- file_name TEXT
    
- s3_key TEXT NOT NULL
    
- mime_type VARCHAR(100)
    
- file_size INT
    
- uploaded_at TIMESTAMP DEFAULT now()
    

#### **kyc_documents**

- **kyc_id** SERIAL PK
    
- user_id UUID FK→users
    
- document_id UUID FK→documents
    
- type KYC_TYPE(‘PAN’,‘AADHAAR’,‘PASSPORT’)
    
- status KYC_STATUS(‘PENDING’,‘VERIFIED’,‘REJECTED’)
    
- uploaded_at TIMESTAMP DEFAULT now()
    
- reviewed_at TIMESTAMP NULL
    
- external_status VARCHAR(50)
    
- external_data JSONB
    

---

### 1.3 Accounts & Transactions

#### **accounts**

- **account_id** UUID PK
    
- user_id UUID FK→users
    
- type ACCOUNT_TYPE(‘SAVINGS’,‘CHECKING’,‘LOAN’,…)
    
- currency CHAR(3) NOT NULL
    
- balance NUMERIC(15,2) DEFAULT 0.00
    
- is_primary BOOLEAN DEFAULT FALSE
    
- created_at TIMESTAMP DEFAULT now()
    

#### **transactions**

- **txn_id** UUID PK
    
- account_id UUID FK→accounts
    
- txn_date DATE NOT NULL
    
- description VARCHAR(500)
    
- debit_amount NUMERIC(15,2) DEFAULT 0.00
    
- credit_amount NUMERIC(15,2) DEFAULT 0.00
    
- balance_after NUMERIC(15,2)
    
- metadata JSONB
    
- created_at TIMESTAMP DEFAULT now()
    

#### **alerts** (Upcoming payments)

- **alert_id** SERIAL PK
    
- user_id UUID FK→users
    
- account_id UUID FK→accounts
    
- due_date DATE NOT NULL
    
- amount NUMERIC(15,2)
    
- description VARCHAR(255)
    
- dismissed BOOLEAN DEFAULT FALSE
    
- created_at TIMESTAMP DEFAULT now()
    
- dismissed_at TIMESTAMP NULL
    

---

### 1.4 Product Applications

#### **products**

- **product_id** UUID PK
    
- name VARCHAR(100) NOT NULL
    
- category PRODUCT_CAT(‘LOAN’,‘CARD’,‘DEPOSIT’,‘MF’)
    
- teaser_rate NUMERIC(5,2)
    
- metadata JSONB
    
- created_at TIMESTAMP DEFAULT now()
    

#### **questionnaire_schemas**

- **schema_id** SERIAL PK
    
- product_id UUID FK→products
    
- schema_json JSONB NOT NULL
    
- version INT DEFAULT 1
    
- created_at TIMESTAMP DEFAULT now()
    

#### **applications**

- **application_id** UUID PK
    
- user_id UUID FK→users
    
- product_id UUID FK→products
    
- reference_no VARCHAR(30) UNIQUE NOT NULL
    
- status APPL_STATUS(‘SUBMITTED’,‘UNDER_REVIEW’,‘APPROVED’,‘REJECTED’,‘CANCELLED’)
    
- submitted_at TIMESTAMP DEFAULT now()
    
- cancelled_at TIMESTAMP NULL
    
- cancelled_reason TEXT
    
- updated_at TIMESTAMP DEFAULT now()
    

#### **application_documents**

- **id** SERIAL PK
    
- application_id UUID FK→applications
    
- document_id UUID FK→documents
    

#### **application_history**

- **hist_id** SERIAL PK
    
- application_id UUID FK→applications
    
- status APPL_STATUS
    
- changed_at TIMESTAMP DEFAULT now()
    

---

### 1.5 Service Requests

#### **tickets**

- **ticket_id** UUID PK
    
- user_id UUID FK→users
    
- category TICKET_CAT(…)
    
- urgency URGENCY(‘LOW’,‘MEDIUM’,‘HIGH’)
    
- subject VARCHAR(200)
    
- description TEXT
    
- assigned_team VARCHAR(50)
    
- sla_deadline TIMESTAMP
    
- status TICKET_STATUS(‘OPEN’,‘IN_PROGRESS’,‘ESCALATED’,‘CLOSED’)
    
- created_at TIMESTAMP DEFAULT now()
    
- updated_at TIMESTAMP DEFAULT now()
    

#### **ticket_messages**

- **msg_id** UUID PK
    
- ticket_id UUID FK→tickets
    
- sender_id UUID FK→users
    
- message TEXT
    
- created_at TIMESTAMP DEFAULT now()
    

#### **message_attachments**

- **id** SERIAL PK
    
- msg_id UUID FK→ticket_messages
    
- document_id UUID FK→documents
    

#### **ticket_feedback**

- **feedback_id** SERIAL PK
    
- ticket_id UUID FK→tickets
    
- rating SMALLINT CHECK (rating BETWEEN 1 AND 5)
    
- comments TEXT
    
- created_at TIMESTAMP DEFAULT now()
    

---

### 1.6 Investment Dashboard

#### **investments**

- **investment_id** UUID PK
    
- user_id UUID FK→users
    
- type INV_TYPE(‘SIP’,‘FD’,‘BOND’,‘EXTERNAL’)
    
- amount NUMERIC(15,2)
    
- purchase_date DATE
    
- current_value NUMERIC(15,2)
    
- xirr NUMERIC(5,2)
    
- external BOOLEAN DEFAULT FALSE
    
- notes TEXT
    
- created_at TIMESTAMP DEFAULT now()
    

#### **sip_schedules**

- **schedule_id** SERIAL PK
    
- investment_id UUID FK→investments
    
- schedule_date DATE
    
- amount NUMERIC(15,2)
    

#### **investment_history**

- **hist_id** SERIAL PK
    
- investment_id UUID FK→investments
    
- as_of_date DATE
    
- value NUMERIC(15,2)
    

#### **maturity_alerts**

- **alert_id** SERIAL PK
    
- investment_id UUID FK→investments
    
- maturity_date DATE
    
- dismissed BOOLEAN DEFAULT FALSE
    
- created_at TIMESTAMP DEFAULT now()
    
- dismissed_at TIMESTAMP NULL
    

---

### 1.7 Secure Messaging

#### **threads**

- **thread_id** UUID PK
    
- subject VARCHAR(200)
    
- created_by UUID FK→users
    
- created_at TIMESTAMP DEFAULT now()
    
- updated_at TIMESTAMP DEFAULT now()
    

#### **thread_participants**

- **thread_id** UUID FK→threads
    
- **user_id** UUID FK→users
    
- PRIMARY KEY(thread_id, user_id)
    

#### **messages**

- **message_id** UUID PK
    
- thread_id UUID FK→threads
    
- sender_id UUID FK→users
    
- content TEXT
    
- is_deleted BOOLEAN DEFAULT FALSE
    
- created_at TIMESTAMP DEFAULT now()
    

#### **message_attachments**

- **id** SERIAL PK
    
- message_id UUID FK→messages
    
- document_id UUID FK→documents
    

#### **notifications**

- **notif_id** SERIAL PK
    
- user_id UUID FK→users
    
- type VARCHAR(50)
    
- payload JSONB
    
- is_read BOOLEAN DEFAULT FALSE
    
- created_at TIMESTAMP DEFAULT now()
    

---

### 1.8 Rules Engine

#### **rulesets**

- **ruleset_id** UUID PK
    
- name VARCHAR(100) UNIQUE
    
- status RULE_STATUS(‘PENDING’,‘ACTIVE’,‘REJECTED’)
    
- created_at TIMESTAMP DEFAULT now()
    
- updated_at TIMESTAMP DEFAULT now()
    

#### **rule_versions**

- **version_id** SERIAL PK
    
- ruleset_id UUID FK→rulesets
    
- version_no INT
    
- drl_content TEXT
    
- created_by UUID FK→users
    
- created_at TIMESTAMP DEFAULT now()
    

#### **rule_evaluations**

- **eval_id** SERIAL PK
    
- ruleset_id UUID FK→rulesets
    
- payload JSONB
    
- result JSONB
    
- evaluated_at TIMESTAMP DEFAULT now()
    

#### **rule_change_log**

- **log_id** SERIAL PK
    
- ruleset_id UUID FK→rulesets
    
- version_id INT FK→rule_versions
    
- changed_by UUID FK→users
    
- change_type VARCHAR(50)
    
- timestamp TIMESTAMP DEFAULT now()
    

---

### 1.9 Search & Audit

#### **search_history**

- **history_id** SERIAL PK
    
- user_id UUID FK→users
    
- query_text TEXT
    
- executed_at TIMESTAMP DEFAULT now()
    

#### **audit_log**

- **audit_id** SERIAL PK
    
- actor_id UUID FK→users
    
- event_type VARCHAR(100)
    
- resource_type VARCHAR(50)
    
- resource_id UUID
    
- event_data JSONB
    
- timestamp TIMESTAMP DEFAULT now()
    

---
