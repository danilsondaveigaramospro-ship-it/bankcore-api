insert into users (id, email, password_hash, role, status, created_at, updated_at, last_login_at)
values
('00000000-0000-0000-0000-000000000005', 'claire@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '11 days', now() - interval '11 days', now() - interval '2 days'),
('00000000-0000-0000-0000-000000000006', 'diego@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '10 days', now() - interval '10 days', now() - interval '1 day'),
('00000000-0000-0000-0000-000000000007', 'emma@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '9 days', now() - interval '9 days', now() - interval '3 hours'),
('00000000-0000-0000-0000-000000000008', 'noah@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '8 days', now() - interval '8 days', null),
('00000000-0000-0000-0000-000000000009', 'sofia@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '7 days', now() - interval '7 days', now() - interval '5 hours'),
('00000000-0000-0000-0000-000000000010', 'locked.customer@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'LOCKED', now() - interval '6 days', now() - interval '1 day', null),
('00000000-0000-0000-0000-000000000011', 'reviewer@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_BANK_EMPLOYEE', 'ACTIVE', now() - interval '5 days', now() - interval '5 days', now() - interval '1 day'),
('00000000-0000-0000-0000-000000000012', 'disabled.employee@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_BANK_EMPLOYEE', 'DISABLED', now() - interval '25 days', now() - interval '2 days', null);

insert into customer_profiles (id, user_id, first_name, last_name, date_of_birth, phone_number, address_line1, address_line2, postal_code, city, country, kyc_status, created_at, updated_at)
values
('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000005', 'Claire', 'Dubois', '1979-01-19', '+41765550103', 'Avenue de Rumine 42', null, '1005', 'Lausanne', 'Switzerland', 'VERIFIED', now() - interval '11 days', now() - interval '11 days'),
('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000006', 'Diego', 'Rossi', '1988-06-24', '+41765550104', 'Via Nassa 18', 'Scala B', '6900', 'Lugano', 'Switzerland', 'VERIFIED', now() - interval '10 days', now() - interval '10 days'),
('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000007', 'Emma', 'Schneider', '1995-11-08', '+41765550105', 'Marktgasse 5', null, '3011', 'Bern', 'Switzerland', 'VERIFIED', now() - interval '9 days', now() - interval '9 days'),
('10000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000008', 'Noah', 'Meier', '1998-03-30', '+41765550106', 'Kirchplatz 3', null, '4051', 'Basel', 'Switzerland', 'PENDING', now() - interval '8 days', now() - interval '8 days'),
('10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000009', 'Sofia', 'Garcia', '1983-12-14', '+41765550107', 'Rue de Bourg 21', null, '1003', 'Lausanne', 'Switzerland', 'VERIFIED', now() - interval '7 days', now() - interval '7 days'),
('10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000010', 'Liam', 'Weber', '1990-02-02', '+41765550108', 'Seestrasse 77', null, '8002', 'Zurich', 'Switzerland', 'REJECTED', now() - interval '6 days', now() - interval '1 day');

insert into bank_accounts (id, customer_id, iban, account_number, currency, balance, status, account_type, daily_transfer_limit, created_at, updated_at, closed_at)
values
('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000003', 'CH8800762011623852960', '1000000004', 'CHF', 108400.00, 'ACTIVE', 'BUSINESS', 50000.00, now() - interval '10 days', now() - interval '2 hours', null),
('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000003', 'CH4100762011623852961', '1000000005', 'USD', 18500.00, 'ACTIVE', 'SAVINGS', 12000.00, now() - interval '10 days', now() - interval '1 day', null),
('20000000-0000-0000-0000-000000000006', '10000000-0000-0000-0000-000000000004', 'CH1200762011623852962', '1000000006', 'EUR', 12600.00, 'ACTIVE', 'CHECKING', 6000.00, now() - interval '9 days', now() - interval '3 hours', null),
('20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000004', 'CH3000762011623852963', '1000000007', 'CHF', 1500.00, 'FROZEN', 'CHECKING', 3000.00, now() - interval '9 days', now() - interval '1 day', null),
('20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000005', 'CH0200762011623852964', '1000000008', 'CHF', 1050.00, 'ACTIVE', 'SAVINGS', 2500.00, now() - interval '8 days', now() - interval '6 hours', null),
('20000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000005', 'CH7500762011623852965', '1000000009', 'USD', 0.00, 'CLOSED', 'CHECKING', 1000.00, now() - interval '8 days', now() - interval '2 days', now() - interval '2 days'),
('20000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000007', 'CH9900762011623852966', '1000000010', 'EUR', 37000.00, 'ACTIVE', 'BUSINESS', 30000.00, now() - interval '6 days', now() - interval '30 minutes', null);

insert into bank_transactions (id, type, status, source_account_id, target_account_id, amount, currency, description, reference, failure_reason, created_at, completed_at, initiated_by_user_id)
values
('30000000-0000-0000-0000-000000000004', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000004', 125000.00, 'CHF', 'Business onboarding deposit', 'DEMO-DEP-0004', null, now() - interval '10 days', now() - interval '10 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000005', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000004', null, 1600.00, 'CHF', 'Supplier cash withdrawal', 'DEMO-WDR-0001', null, now() - interval '9 days', now() - interval '9 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000006', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000005', 20000.00, 'USD', 'Savings opening deposit', 'DEMO-DEP-0005', null, now() - interval '9 days', now() - interval '9 days', '00000000-0000-0000-0000-000000000011'),
('30000000-0000-0000-0000-000000000007', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000005', null, 1500.00, 'USD', 'Travel cash withdrawal', 'DEMO-WDR-0002', null, now() - interval '7 days', now() - interval '7 days', '00000000-0000-0000-0000-000000000011'),
('30000000-0000-0000-0000-000000000008', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000006', 9000.00, 'EUR', 'Salary deposit', 'DEMO-DEP-0006', null, now() - interval '8 days', now() - interval '8 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000009', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000006', null, 1400.00, 'EUR', 'Rent withdrawal', 'DEMO-WDR-0003', null, now() - interval '6 days', now() - interval '6 days', '00000000-0000-0000-0000-000000000006'),
('30000000-0000-0000-0000-000000000010', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000007', 1500.00, 'CHF', 'Manual deposit before freeze', 'DEMO-DEP-0007', null, now() - interval '6 days', now() - interval '6 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000011', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000008', 4000.00, 'CHF', 'Savings deposit', 'DEMO-DEP-0008', null, now() - interval '7 days', now() - interval '7 days', '00000000-0000-0000-0000-000000000011'),
('30000000-0000-0000-0000-000000000012', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000008', null, 750.00, 'CHF', 'Card withdrawal', 'DEMO-WDR-0004', null, now() - interval '3 days', now() - interval '3 days', '00000000-0000-0000-0000-000000000007'),
('30000000-0000-0000-0000-000000000013', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000009', 1000.00, 'USD', 'Closed account initial deposit', 'DEMO-DEP-0009', null, now() - interval '8 days', now() - interval '8 days', '00000000-0000-0000-0000-000000000011'),
('30000000-0000-0000-0000-000000000014', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000009', null, 1000.00, 'USD', 'Balance withdrawal before closure', 'DEMO-WDR-0005', null, now() - interval '2 days', now() - interval '2 days', '00000000-0000-0000-0000-000000000011'),
('30000000-0000-0000-0000-000000000015', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000010', 50000.00, 'EUR', 'Business treasury deposit', 'DEMO-DEP-0010', null, now() - interval '6 days', now() - interval '6 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000016', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000010', null, 8000.00, 'EUR', 'Large supplier payout', 'DEMO-WDR-0006', null, now() - interval '1 day', now() - interval '1 day', '00000000-0000-0000-0000-000000000009'),
('30000000-0000-0000-0000-000000000017', 'TRANSFER_OUT', 'COMPLETED', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', 1200.00, 'CHF', 'Alice to Bob rent split', 'DEMO-TRF-0001-OUT', null, now() - interval '5 days', now() - interval '5 days', '00000000-0000-0000-0000-000000000003'),
('30000000-0000-0000-0000-000000000018', 'TRANSFER_IN', 'COMPLETED', '20000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000003', 1200.00, 'CHF', 'Alice to Bob rent split', 'DEMO-TRF-0001-IN', null, now() - interval '5 days', now() - interval '5 days', '00000000-0000-0000-0000-000000000003'),
('30000000-0000-0000-0000-000000000019', 'TRANSFER_OUT', 'COMPLETED', '20000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 15000.00, 'CHF', 'Large business transfer to Alice', 'DEMO-TRF-0002-OUT', null, now() - interval '1 day', now() - interval '1 day', '00000000-0000-0000-0000-000000000005'),
('30000000-0000-0000-0000-000000000020', 'TRANSFER_IN', 'COMPLETED', '20000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 15000.00, 'CHF', 'Large business transfer to Alice', 'DEMO-TRF-0002-IN', null, now() - interval '1 day', now() - interval '1 day', '00000000-0000-0000-0000-000000000005'),
('30000000-0000-0000-0000-000000000021', 'TRANSFER_OUT', 'COMPLETED', '20000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000006', 5000.00, 'EUR', 'Sofia to Diego invoice settlement', 'DEMO-TRF-0003-OUT', null, now() - interval '3 hours', now() - interval '3 hours', '00000000-0000-0000-0000-000000000009'),
('30000000-0000-0000-0000-000000000022', 'TRANSFER_IN', 'COMPLETED', '20000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000006', 5000.00, 'EUR', 'Sofia to Diego invoice settlement', 'DEMO-TRF-0003-IN', null, now() - interval '3 hours', now() - interval '3 hours', '00000000-0000-0000-0000-000000000009'),
('30000000-0000-0000-0000-000000000023', 'TRANSFER_OUT', 'FAILED', '20000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000003', 300.00, 'CHF', 'Failed transfer from frozen account', 'DEMO-FAIL-0001', 'Account is frozen', now() - interval '11 minutes', null, '00000000-0000-0000-0000-000000000006'),
('30000000-0000-0000-0000-000000000024', 'WITHDRAWAL', 'FAILED', '20000000-0000-0000-0000-000000000007', null, 200.00, 'CHF', 'Failed withdrawal from frozen account', 'DEMO-FAIL-0002', 'Account is frozen', now() - interval '9 minutes', null, '00000000-0000-0000-0000-000000000006'),
('30000000-0000-0000-0000-000000000025', 'TRANSFER_OUT', 'REJECTED', '20000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000003', 100.00, 'CHF', 'Rejected transfer review case', 'DEMO-FAIL-0003', 'Compliance review rejected transfer', now() - interval '7 minutes', null, '00000000-0000-0000-0000-000000000006'),
('30000000-0000-0000-0000-000000000026', 'WITHDRAWAL', 'FAILED', '20000000-0000-0000-0000-000000000007', null, 75.00, 'CHF', 'Failed ATM withdrawal', 'DEMO-FAIL-0004', 'Account is frozen', now() - interval '5 minutes', null, '00000000-0000-0000-0000-000000000006'),
('30000000-0000-0000-0000-000000000027', 'WITHDRAWAL', 'COMPLETED', '20000000-0000-0000-0000-000000000008', null, 2200.00, 'CHF', 'Unusual savings withdrawal', 'DEMO-WDR-0007', null, now() - interval '30 minutes', now() - interval '30 minutes', '00000000-0000-0000-0000-000000000007');

insert into suspicious_activity_alerts (id, account_id, transaction_id, alert_type, severity, message, status, created_at, reviewed_at, reviewed_by)
values
('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000019', 'LARGE_TRANSFER', 'HIGH', 'Demo alert: CHF 15000 transfer exceeds the configured large-transfer threshold.', 'OPEN', now() - interval '1 day', null, null),
('40000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000007', null, 'FAILED_ATTEMPTS', 'MEDIUM', 'Demo alert: repeated failed operations on frozen account within 15 minutes.', 'OPEN', now() - interval '4 minutes', null, null),
('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000027', 'UNUSUAL_WITHDRAWAL', 'MEDIUM', 'Demo alert: withdrawal exceeds 50 percent of available balance before withdrawal.', 'REVIEWED', now() - interval '25 minutes', now() - interval '10 minutes', '00000000-0000-0000-0000-000000000011'),
('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000010', null, 'MANY_TRANSFERS', 'LOW', 'Demo alert: high outgoing transfer cadence for business account.', 'DISMISSED', now() - interval '2 hours', now() - interval '1 hour', '00000000-0000-0000-0000-000000000002');

insert into audit_logs (id, actor_user_id, action, entity_type, entity_id, ip_address, user_agent, metadata, created_at)
values
('50000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'CUSTOMER_CREATED', 'CustomerProfile', '10000000-0000-0000-0000-000000000003', '127.0.0.1', 'flyway', '{"email":"claire@bankcore.local"}', now() - interval '11 days'),
('50000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000011', 'ACCOUNT_CREATED', 'BankAccount', '20000000-0000-0000-0000-000000000004', '127.0.0.1', 'flyway', '{"currency":"CHF","accountType":"BUSINESS"}', now() - interval '10 days'),
('50000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002', 'ACCOUNT_DEPOSIT', 'BankAccount', '20000000-0000-0000-0000-000000000004', '127.0.0.1', 'flyway', '{"transactionId":"30000000-0000-0000-0000-000000000004"}', now() - interval '10 days'),
('50000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000011', 'ACCOUNT_FROZEN', 'BankAccount', '20000000-0000-0000-0000-000000000007', '127.0.0.1', 'flyway', '{"reason":"Demo compliance hold"}', now() - interval '1 day'),
('50000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000011', 'ACCOUNT_CLOSED', 'BankAccount', '20000000-0000-0000-0000-000000000009', '127.0.0.1', 'flyway', '{"reason":"Demo account lifecycle"}', now() - interval '2 days'),
('50000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000005', 'INTERNAL_TRANSFER', 'BankTransaction', '30000000-0000-0000-0000-000000000019', '127.0.0.1', 'flyway', '{"incomingTransactionId":"30000000-0000-0000-0000-000000000020"}', now() - interval '1 day'),
('50000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000011', 'ALERT_REVIEWED', 'SuspiciousActivityAlert', '40000000-0000-0000-0000-000000000004', '127.0.0.1', 'flyway', '{"note":"Reviewed during demo seed"}', now() - interval '10 minutes'),
('50000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000002', 'ALERT_DISMISSED', 'SuspiciousActivityAlert', '40000000-0000-0000-0000-000000000005', '127.0.0.1', 'flyway', '{"note":"Dismissed as known business pattern"}', now() - interval '1 hour'),
('50000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001', 'USER_DISABLED', 'User', '00000000-0000-0000-0000-000000000012', '127.0.0.1', 'flyway', '{"reason":"Demo disabled employee"}', now() - interval '2 days');

update bank_accounts
set balance = 38800.00,
    updated_at = now() - interval '1 day'
where id = '20000000-0000-0000-0000-000000000001';

update bank_accounts
set balance = 9200.00,
    updated_at = now() - interval '5 days'
where id = '20000000-0000-0000-0000-000000000003';
