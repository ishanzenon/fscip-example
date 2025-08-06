-- =====================================================
-- FSCIP Sample Data Initialization Script
-- Sprint 0: Sample data for cold start testing
-- Compatible with PostgreSQL 12+
-- =====================================================

-- =====================================================
-- ROLES AND PERMISSIONS
-- =====================================================

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
    ('CUSTOMER', 'Standard customer with access to personal banking features'),
    ('SUPPORT_AGENT', 'Customer support agent with ticket management access'),
    ('RELATIONSHIP_MANAGER', 'Relationship manager with enhanced customer view'),
    ('AUDITOR', 'Read-only access for audit and compliance'),
    ('ADMIN', 'System administrator with full access')
ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- SAMPLE USERS
-- =====================================================

-- Insert sample customers
-- Note: Password hashes are for 'password123' - should be properly hashed in production
INSERT INTO users (user_id, email, password_hash, full_name, mobile, status, two_factor_enabled) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'john.doe@example.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'John Doe', '+1234567890', 'ACTIVE', TRUE),
    ('550e8400-e29b-41d4-a716-446655440002', 'jane.smith@example.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'Jane Smith', '+1234567891', 'ACTIVE', TRUE),
    ('550e8400-e29b-41d4-a716-446655440003', 'bob.wilson@example.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'Bob Wilson', '+1234567892', 'PENDING', TRUE),
    ('550e8400-e29b-41d4-a716-446655440004', 'alice.brown@example.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'Alice Brown', '+1234567893', 'ACTIVE', FALSE),
    ('550e8400-e29b-41d4-a716-446655440005', 'support@fscip.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'Support Agent', '+1234567894', 'ACTIVE', TRUE),
    ('550e8400-e29b-41d4-a716-446655440006', 'rm@fscip.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'Relationship Manager', '+1234567895', 'ACTIVE', TRUE),
    ('550e8400-e29b-41d4-a716-446655440007', 'admin@fscip.com', '$2a$10$DowJonesIndexFakeHashForTesting123456789', 'System Admin', '+1234567896', 'ACTIVE', TRUE)
ON CONFLICT (user_id) DO NOTHING;

-- =====================================================
-- USER ROLE ASSIGNMENTS
-- =====================================================

INSERT INTO user_roles (user_id, role_id) VALUES 
    -- Customers
    ('550e8400-e29b-41d4-a716-446655440001', (SELECT role_id FROM roles WHERE name = 'CUSTOMER')),
    ('550e8400-e29b-41d4-a716-446655440002', (SELECT role_id FROM roles WHERE name = 'CUSTOMER')),
    ('550e8400-e29b-41d4-a716-446655440003', (SELECT role_id FROM roles WHERE name = 'CUSTOMER')),
    ('550e8400-e29b-41d4-a716-446655440004', (SELECT role_id FROM roles WHERE name = 'CUSTOMER')),
    
    -- Staff members
    ('550e8400-e29b-41d4-a716-446655440005', (SELECT role_id FROM roles WHERE name = 'SUPPORT_AGENT')),
    ('550e8400-e29b-41d4-a716-446655440006', (SELECT role_id FROM roles WHERE name = 'RELATIONSHIP_MANAGER')),
    ('550e8400-e29b-41d4-a716-446655440007', (SELECT role_id FROM roles WHERE name = 'ADMIN'))
ON CONFLICT (user_id, role_id) DO NOTHING;

-- =====================================================
-- SAMPLE ACCOUNTS
-- =====================================================

INSERT INTO accounts (account_id, user_id, type, currency, balance, is_primary) VALUES 
    -- John Doe's accounts
    ('660e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'CHECKING', 'USD', 5250.75, TRUE),
    ('660e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'SAVINGS', 'USD', 15000.00, FALSE),
    ('660e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 'CREDIT_CARD', 'USD', -850.25, FALSE),
    
    -- Jane Smith's accounts
    ('660e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 'CHECKING', 'USD', 3200.50, TRUE),
    ('660e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', 'INVESTMENT', 'USD', 25000.00, FALSE),
    
    -- Bob Wilson's account (pending user)
    ('660e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440003', 'CHECKING', 'USD', 1000.00, TRUE),
    
    -- Alice Brown's accounts
    ('660e8400-e29b-41d4-a716-446655440007', '550e8400-e29b-41d4-a716-446655440004', 'CHECKING', 'USD', 8750.25, TRUE),
    ('660e8400-e29b-41d4-a716-446655440008', '550e8400-e29b-41d4-a716-446655440004', 'LOAN', 'USD', -45000.00, FALSE)
ON CONFLICT (account_id) DO NOTHING;

-- =====================================================
-- SAMPLE TRANSACTIONS
-- =====================================================

INSERT INTO transactions (txn_id, account_id, txn_date, description, debit_amount, credit_amount, balance_after, metadata) VALUES 
    -- John Doe's checking account transactions
    ('770e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440001', '2024-01-15', 'Direct Deposit - Salary', 0.00, 3500.00, 5250.75, '{"source": "payroll", "employer": "TechCorp Inc"}'),
    ('770e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440001', '2024-01-14', 'ATM Withdrawal', 200.00, 0.00, 1750.75, '{"atm_id": "ATM001", "location": "Main St Branch"}'),
    ('770e8400-e29b-41d4-a716-446655440003', '660e8400-e29b-41d4-a716-446655440001', '2024-01-13', 'Online Purchase - Amazon', 125.50, 0.00, 1950.75, '{"merchant": "Amazon", "category": "online_shopping"}'),
    ('770e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440001', '2024-01-12', 'Transfer to Savings', 500.00, 0.00, 2076.25, '{"transfer_type": "internal", "target_account": "savings"}'),
    
    -- John Doe's savings account transactions
    ('770e8400-e29b-41d4-a716-446655440005', '660e8400-e29b-41d4-a716-446655440002', '2024-01-12', 'Transfer from Checking', 0.00, 500.00, 15000.00, '{"transfer_type": "internal", "source_account": "checking"}'),
    ('770e8400-e29b-41d4-a716-446655440006', '660e8400-e29b-41d4-a716-446655440002', '2024-01-01', 'Interest Credit', 0.00, 45.25, 14500.00, '{"interest_rate": 0.025, "period": "monthly"}'),
    
    -- Jane Smith's checking account transactions
    ('770e8400-e29b-41d4-a716-446655440007', '660e8400-e29b-41d4-a716-446655440004', '2024-01-16', 'Mobile Deposit', 0.00, 1200.00, 3200.50, '{"deposit_type": "mobile", "check_number": "1234"}'),
    ('770e8400-e29b-41d4-a716-446655440008', '660e8400-e29b-41d4-a716-446655440004', '2024-01-15', 'Grocery Store', 85.75, 0.00, 2000.50, '{"merchant": "SuperMart", "category": "groceries"}'),
    
    -- Alice Brown's checking account transactions
    ('770e8400-e29b-41d4-a716-446655440009', '660e8400-e29b-41d4-a716-446655440007', '2024-01-16', 'Salary Direct Deposit', 0.00, 4500.00, 8750.25, '{"source": "payroll", "employer": "Finance Corp"}'),
    ('770e8400-e29b-41d4-a716-446655440010', '660e8400-e29b-41d4-a716-446655440007', '2024-01-14', 'Rent Payment', 1500.00, 0.00, 4250.25, '{"payment_type": "rent", "property": "Downtown Apt"}')
ON CONFLICT (txn_id) DO NOTHING;

-- =====================================================
-- SAMPLE APPLICATIONS
-- =====================================================

INSERT INTO applications (application_id, user_id, reference_no, status, submitted_at, cancelled_reason) VALUES 
    ('880e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'APP-2024-001', 'APPROVED', '2024-01-10 09:00:00', NULL),
    ('880e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440002', 'APP-2024-002', 'UNDER_REVIEW', '2024-01-15 14:30:00', NULL),
    ('880e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 'APP-2024-003', 'SUBMITTED', '2024-01-16 10:15:00', NULL),
    ('880e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'APP-2024-004', 'REJECTED', '2024-01-05 16:45:00', NULL)
ON CONFLICT (application_id) DO NOTHING;

-- =====================================================
-- SAMPLE DOCUMENTS
-- =====================================================

INSERT INTO documents (document_id, user_id, context_type, context_id, file_name, s3_key, mime_type, file_size) VALUES 
    ('990e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'APPLICATION', '880e8400-e29b-41d4-a716-446655440001', 'drivers_license.pdf', 'documents/2024/01/john_doe_dl_001.pdf', 'application/pdf', 156789),
    ('990e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 'APPLICATION', '880e8400-e29b-41d4-a716-446655440001', 'bank_statement.pdf', 'documents/2024/01/john_doe_stmt_001.pdf', 'application/pdf', 234567),
    ('990e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440002', 'APPLICATION', '880e8400-e29b-41d4-a716-446655440002', 'passport.pdf', 'documents/2024/01/jane_smith_pp_001.pdf', 'application/pdf', 145632),
    ('990e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'PROFILE', NULL, 'profile_photo.jpg', 'documents/2024/01/alice_brown_photo_001.jpg', 'image/jpeg', 89456)
ON CONFLICT (document_id) DO NOTHING;

-- Link documents to applications
INSERT INTO application_documents (application_id, document_id) VALUES 
    ('880e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440001'),
    ('880e8400-e29b-41d4-a716-446655440001', '990e8400-e29b-41d4-a716-446655440002'),
    ('880e8400-e29b-41d4-a716-446655440002', '990e8400-e29b-41d4-a716-446655440003')
ON CONFLICT (application_id, document_id) DO NOTHING;

-- =====================================================
-- SAMPLE ALERTS
-- =====================================================

INSERT INTO alerts (user_id, account_id, due_date, amount, description, dismissed) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', '660e8400-e29b-41d4-a716-446655440003', '2024-02-01', 850.25, 'Credit Card Payment Due', FALSE),
    ('550e8400-e29b-41d4-a716-446655440002', '660e8400-e29b-41d4-a716-446655440005', '2024-01-25', 2500.00, 'Investment Maturity Alert', FALSE),
    ('550e8400-e29b-41d4-a716-446655440004', '660e8400-e29b-41d4-a716-446655440008', '2024-02-15', 1250.00, 'Loan Payment Due', FALSE),
    ('550e8400-e29b-41d4-a716-446655440001', NULL, '2024-01-20', NULL, 'Annual Fee Waiver Expiring Soon', TRUE)
ON CONFLICT DO NOTHING;

-- =====================================================
-- SAMPLE LOGIN HISTORY
-- =====================================================

INSERT INTO login_history (user_id, login_at, ip_address, user_agent) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', '2024-01-16 08:30:00', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
    ('550e8400-e29b-41d4-a716-446655440001', '2024-01-15 09:15:00', '192.168.1.100', 'Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)'),
    ('550e8400-e29b-41d4-a716-446655440002', '2024-01-16 10:45:00', '10.0.0.25', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'),
    ('550e8400-e29b-41d4-a716-446655440004', '2024-01-16 14:20:00', '172.16.0.50', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'),
    ('550e8400-e29b-41d4-a716-446655440005', '2024-01-16 07:00:00', '192.168.10.5', 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36')
ON CONFLICT DO NOTHING;

-- =====================================================
-- SAMPLE SEARCH HISTORY
-- =====================================================

INSERT INTO search_history (user_id, query_text, executed_at) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'credit card balance', '2024-01-16 10:30:00'),
    ('550e8400-e29b-41d4-a716-446655440001', 'recent transactions', '2024-01-16 10:32:00'),
    ('550e8400-e29b-41d4-a716-446655440002', 'investment portfolio', '2024-01-16 11:15:00'),
    ('550e8400-e29b-41d4-a716-446655440002', 'loan application status', '2024-01-16 11:20:00'),
    ('550e8400-e29b-41d4-a716-446655440004', 'payment history', '2024-01-16 15:45:00')
ON CONFLICT DO NOTHING;

-- =====================================================
-- SAMPLE AUDIT LOG ENTRIES
-- =====================================================

INSERT INTO audit_log (actor_id, event_type, resource_type, resource_id, event_data, timestamp) VALUES 
    ('550e8400-e29b-41d4-a716-446655440001', 'LOGIN', 'USER', '550e8400-e29b-41d4-a716-446655440001', '{"ip_address": "192.168.1.100", "success": true}', '2024-01-16 08:30:00'),
    ('550e8400-e29b-41d4-a716-446655440001', 'TRANSACTION_VIEW', 'ACCOUNT', '660e8400-e29b-41d4-a716-446655440001', '{"action": "view_transactions", "page": 1}', '2024-01-16 08:35:00'),
    ('550e8400-e29b-41d4-a716-446655440002', 'APPLICATION_SUBMIT', 'APPLICATION', '880e8400-e29b-41d4-a716-446655440002', '{"reference_no": "APP-2024-002", "type": "loan_application"}', '2024-01-15 14:30:00'),
    ('550e8400-e29b-41d4-a716-446655440005', 'APPLICATION_REVIEW', 'APPLICATION', '880e8400-e29b-41d4-a716-446655440002', '{"reviewer": "support_agent", "status": "under_review"}', '2024-01-16 09:00:00'),
    ('550e8400-e29b-41d4-a716-446655440007', 'USER_STATUS_CHANGE', 'USER', '550e8400-e29b-41d4-a716-446655440003', '{"old_status": "PENDING", "new_status": "ACTIVE", "reason": "identity_verified"}', '2024-01-16 10:00:00')
ON CONFLICT DO NOTHING;

-- =====================================================
-- DATA INTEGRITY VALIDATION
-- =====================================================

-- Update last_login for users who have login history
UPDATE users SET last_login = (
    SELECT MAX(login_at) FROM login_history WHERE login_history.user_id = users.user_id
) WHERE user_id IN (SELECT DISTINCT user_id FROM login_history);

-- Validate that all primary accounts have correct balance calculations
-- This would be more complex in real implementation with proper transaction summation

-- =====================================================
-- SAMPLE DATA SUMMARY
-- =====================================================

-- Display summary of inserted data for verification
DO $$
DECLARE
    user_count INTEGER;
    account_count INTEGER;
    transaction_count INTEGER;
    application_count INTEGER;
    document_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users;
    SELECT COUNT(*) INTO account_count FROM accounts;
    SELECT COUNT(*) INTO transaction_count FROM transactions;
    SELECT COUNT(*) INTO application_count FROM applications;
    SELECT COUNT(*) INTO document_count FROM documents;
    
    RAISE NOTICE 'Sample data inserted successfully:';
    RAISE NOTICE '- Users: %', user_count;
    RAISE NOTICE '- Accounts: %', account_count;
    RAISE NOTICE '- Transactions: %', transaction_count;
    RAISE NOTICE '- Applications: %', application_count;
    RAISE NOTICE '- Documents: %', document_count;
END
$$;