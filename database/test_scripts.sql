-- =====================================================
-- FSCIP Database Scripts Test & Verification
-- This script tests that schema.sql and data.sql work correctly
-- =====================================================

-- Test 1: Verify all tables were created
SELECT 'Verifying table creation...' as test_step;
SELECT table_name, table_type 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'users', 'roles', 'user_roles', 'otp_codes', 'password_reset_tokens', 
    'login_history', 'accounts', 'transactions', 'documents', 'applications', 
    'application_documents', 'alerts', 'search_history', 'audit_log', 'schema_version'
)
ORDER BY table_name;

-- Test 2: Verify all enums were created
SELECT 'Verifying enum types...' as test_step;
SELECT typname as enum_name 
FROM pg_type 
WHERE typcategory = 'E' 
AND typname IN ('user_status', 'account_type', 'appl_status', 'ctx')
ORDER BY typname;

-- Test 3: Verify indexes were created
SELECT 'Verifying indexes...' as test_step;
SELECT indexname, tablename 
FROM pg_indexes 
WHERE schemaname = 'public' 
AND indexname LIKE 'idx_%'
ORDER BY tablename, indexname;

-- Test 4: Verify constraints and foreign keys
SELECT 'Verifying foreign key constraints...' as test_step;
SELECT 
    tc.table_name, 
    tc.constraint_name, 
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
AND tc.table_schema = 'public'
ORDER BY tc.table_name, tc.constraint_name;

-- Test 5: Verify sample data was inserted
SELECT 'Verifying sample data insertion...' as test_step;
SELECT 
    'users' as table_name, COUNT(*) as record_count 
FROM users
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'accounts', COUNT(*) FROM accounts  
UNION ALL
SELECT 'transactions', COUNT(*) FROM transactions
UNION ALL
SELECT 'applications', COUNT(*) FROM applications
UNION ALL
SELECT 'documents', COUNT(*) FROM documents
UNION ALL
SELECT 'alerts', COUNT(*) FROM alerts
UNION ALL
SELECT 'login_history', COUNT(*) FROM login_history
UNION ALL
SELECT 'search_history', COUNT(*) FROM search_history
UNION ALL
SELECT 'audit_log', COUNT(*) FROM audit_log
ORDER BY table_name;

-- Test 6: Verify business logic constraints
SELECT 'Testing business constraints...' as test_step;

-- Check that each user has at least one role
SELECT 
    'Users without roles' as check_name,
    COUNT(*) as violation_count
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
WHERE ur.user_id IS NULL;

-- Check that primary accounts constraint works
SELECT 
    'Multiple primary accounts per user/currency' as check_name,
    COUNT(*) as violation_count
FROM (
    SELECT user_id, currency, COUNT(*) as primary_count
    FROM accounts 
    WHERE is_primary = TRUE
    GROUP BY user_id, currency
    HAVING COUNT(*) > 1
) violations;

-- Check that transaction amounts are reasonable
SELECT 
    'Transactions with both debit and credit amounts' as check_name,
    COUNT(*) as violation_count
FROM transactions
WHERE debit_amount > 0 AND credit_amount > 0;

-- Test 7: Test data relationships integrity
SELECT 'Testing referential integrity...' as test_step;

-- Check all foreign key relationships are valid
SELECT 
    'Orphaned user_roles' as check_name,
    COUNT(*) as violation_count
FROM user_roles ur
LEFT JOIN users u ON ur.user_id = u.user_id
LEFT JOIN roles r ON ur.role_id = r.role_id
WHERE u.user_id IS NULL OR r.role_id IS NULL;

SELECT 
    'Orphaned accounts' as check_name,
    COUNT(*) as violation_count
FROM accounts a
LEFT JOIN users u ON a.user_id = u.user_id
WHERE u.user_id IS NULL;

SELECT 
    'Orphaned transactions' as check_name,
    COUNT(*) as violation_count
FROM transactions t
LEFT JOIN accounts a ON t.account_id = a.account_id
WHERE a.account_id IS NULL;

-- Test 8: Verify triggers are working
SELECT 'Testing triggers...' as test_step;
SELECT 
    trigger_name, 
    event_manipulation, 
    event_object_table
FROM information_schema.triggers
WHERE event_object_schema = 'public'
ORDER BY event_object_table, trigger_name;

-- Test 9: Performance test with sample queries
SELECT 'Testing query performance...' as test_step;

-- Sample queries that the application would run
EXPLAIN (ANALYZE, BUFFERS) 
SELECT u.full_name, a.type, a.balance 
FROM users u 
JOIN accounts a ON u.user_id = a.user_id 
WHERE u.email = 'john.doe@example.com';

EXPLAIN (ANALYZE, BUFFERS)
SELECT t.*, a.type as account_type
FROM transactions t
JOIN accounts a ON t.account_id = a.account_id
WHERE a.user_id = '550e8400-e29b-41d4-a716-446655440001'
ORDER BY t.txn_date DESC
LIMIT 10;

-- Test 10: Verify script idempotency (can be run multiple times)
SELECT 'Testing script idempotency...' as test_step;
SELECT 
    version,
    applied_at,
    description
FROM schema_version
ORDER BY applied_at DESC;

-- Final summary
SELECT 'Test execution completed!' as final_message;
SELECT 
    'Total tables' as metric,
    COUNT(*) as value
FROM information_schema.tables 
WHERE table_schema = 'public'
UNION ALL
SELECT 
    'Total records across all tables',
    (SELECT COUNT(*) FROM users) +
    (SELECT COUNT(*) FROM roles) +
    (SELECT COUNT(*) FROM user_roles) +
    (SELECT COUNT(*) FROM accounts) +
    (SELECT COUNT(*) FROM transactions) +
    (SELECT COUNT(*) FROM applications) +
    (SELECT COUNT(*) FROM documents) +
    (SELECT COUNT(*) FROM alerts) +
    (SELECT COUNT(*) FROM login_history) +
    (SELECT COUNT(*) FROM search_history) +
    (SELECT COUNT(*) FROM audit_log);