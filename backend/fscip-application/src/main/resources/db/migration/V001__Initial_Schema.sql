-- =====================================================
-- FSCIP Database Schema Initialization Script
-- Sprint 0: Core Entities for MVP Implementation
-- Compatible with PostgreSQL 12+
-- =====================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- CUSTOM TYPES AND ENUMS
-- =====================================================

-- User status enum
DO $$ BEGIN
    CREATE TYPE USER_STATUS AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'DELETED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- Account type enum
DO $$ BEGIN
    CREATE TYPE ACCOUNT_TYPE AS ENUM ('SAVINGS', 'CHECKING', 'LOAN', 'INVESTMENT', 'CREDIT_CARD');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- Application status enum
DO $$ BEGIN
    CREATE TYPE APPL_STATUS AS ENUM ('SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'REJECTED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- Document context enum
DO $$ BEGIN
    CREATE TYPE CTX AS ENUM ('APPLICATION', 'TICKET', 'MESSAGE', 'PROFILE', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

-- =====================================================
-- CORE TABLES - AUTH & USERS
-- =====================================================

-- Users table
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(320) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    mobile VARCHAR(15) UNIQUE,
    status USER_STATUS DEFAULT 'PENDING',
    two_factor_enabled BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE IF NOT EXISTS roles (
    role_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- User roles junction table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(role_id),
    PRIMARY KEY (user_id, role_id)
);

-- OTP codes table
CREATE TABLE IF NOT EXISTS otp_codes (
    otp_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    otp CHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Password reset tokens table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    token UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Login history table
CREATE TABLE IF NOT EXISTS login_history (
    login_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- =====================================================
-- ACCOUNTS & TRANSACTIONS
-- =====================================================

-- Accounts table
CREATE TABLE IF NOT EXISTS accounts (
    account_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    type ACCOUNT_TYPE NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    balance NUMERIC(15,2) DEFAULT 0.00,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    txn_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL REFERENCES accounts(account_id) ON DELETE CASCADE,
    txn_date DATE NOT NULL,
    description VARCHAR(500),
    debit_amount NUMERIC(15,2) DEFAULT 0.00,
    credit_amount NUMERIC(15,2) DEFAULT 0.00,
    balance_after NUMERIC(15,2),
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- DOCUMENT MANAGEMENT
-- =====================================================

-- Documents table
CREATE TABLE IF NOT EXISTS documents (
    document_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    context_type CTX DEFAULT 'OTHER',
    context_id UUID NULL,
    file_name TEXT,
    s3_key TEXT NOT NULL,
    mime_type VARCHAR(100),
    file_size INTEGER,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- APPLICATIONS & PRODUCT APPLICATIONS
-- =====================================================

-- Applications table
CREATE TABLE IF NOT EXISTS applications (
    application_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    reference_no VARCHAR(30) UNIQUE NOT NULL,
    status APPL_STATUS DEFAULT 'SUBMITTED',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL,
    cancelled_reason TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Application documents junction table
CREATE TABLE IF NOT EXISTS application_documents (
    id SERIAL PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(application_id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(document_id) ON DELETE CASCADE,
    UNIQUE(application_id, document_id)
);

-- =====================================================
-- ALERTS & NOTIFICATIONS
-- =====================================================

-- Alerts table (for upcoming payments and notifications)
CREATE TABLE IF NOT EXISTS alerts (
    alert_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    account_id UUID REFERENCES accounts(account_id) ON DELETE SET NULL,
    due_date DATE NOT NULL,
    amount NUMERIC(15,2),
    description VARCHAR(255),
    dismissed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dismissed_at TIMESTAMP NULL
);

-- =====================================================
-- SEARCH & AUDIT
-- =====================================================

-- Search history table
CREATE TABLE IF NOT EXISTS search_history (
    history_id SERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    query_text TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit log table
CREATE TABLE IF NOT EXISTS audit_log (
    audit_id SERIAL PRIMARY KEY,
    actor_id UUID REFERENCES users(user_id) ON DELETE SET NULL,
    event_type VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    event_data JSONB,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- User authentication indexes
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_mobile ON users(mobile);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- OTP and token indexes
CREATE INDEX IF NOT EXISTS idx_otp_codes_user_id ON otp_codes(user_id);
CREATE INDEX IF NOT EXISTS idx_otp_codes_expires_at ON otp_codes(expires_at);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Account and transaction indexes
CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id);
CREATE INDEX IF NOT EXISTS idx_accounts_type ON accounts(type);
CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_transactions_txn_date ON transactions(txn_date);

-- Document indexes
CREATE INDEX IF NOT EXISTS idx_documents_user_id ON documents(user_id);
CREATE INDEX IF NOT EXISTS idx_documents_context_type ON documents(context_type);
CREATE INDEX IF NOT EXISTS idx_documents_s3_key ON documents(s3_key);

-- Application indexes
CREATE INDEX IF NOT EXISTS idx_applications_user_id ON applications(user_id);
CREATE INDEX IF NOT EXISTS idx_applications_reference_no ON applications(reference_no);
CREATE INDEX IF NOT EXISTS idx_applications_status ON applications(status);

-- Audit and search indexes
CREATE INDEX IF NOT EXISTS idx_audit_log_actor_id ON audit_log(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_log_event_type ON audit_log(event_type);
CREATE INDEX IF NOT EXISTS idx_search_history_user_id ON search_history(user_id);

-- =====================================================
-- CONSTRAINTS AND TRIGGERS
-- =====================================================

-- Ensure balance calculations are consistent
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Add update triggers for tables with updated_at columns
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_applications_updated_at ON applications;
CREATE TRIGGER update_applications_updated_at
    BEFORE UPDATE ON applications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Constraint to ensure only one primary account per user per currency
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_primary_account_per_user_currency 
ON accounts(user_id, currency) 
WHERE is_primary = TRUE;

-- =====================================================
-- INITIAL DATA SETUP
-- =====================================================

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('ADMIN', 'System administrator with full access'),
    ('CUSTOMER', 'Regular customer with limited access'),
    ('SUPPORT_AGENT', 'Customer support agent'),
    ('RELATIONSHIP_MANAGER', 'Relationship manager for customers'),
    ('AUDITOR', 'Auditor with read-only access for compliance')
ON CONFLICT (name) DO NOTHING;