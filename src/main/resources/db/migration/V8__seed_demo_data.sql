insert into users (id, email, password_hash, role, status, created_at, updated_at)
values
('00000000-0000-0000-0000-000000000001', 'admin@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_ADMIN', 'ACTIVE', now() - interval '20 days', now() - interval '20 days'),
('00000000-0000-0000-0000-000000000002', 'employee@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_BANK_EMPLOYEE', 'ACTIVE', now() - interval '18 days', now() - interval '18 days'),
('00000000-0000-0000-0000-000000000003', 'alice@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '15 days', now() - interval '15 days'),
('00000000-0000-0000-0000-000000000004', 'bob@bankcore.local', '$2y$10$wFWtJsLuxro5gumaiwSYx.FzcUYfBqu8k0bpdo1NdATSQ2Bvn3.6q', 'ROLE_CUSTOMER', 'ACTIVE', now() - interval '13 days', now() - interval '13 days');

insert into customer_profiles (id, user_id, first_name, last_name, date_of_birth, phone_number, address_line1, postal_code, city, country, kyc_status, created_at, updated_at)
values
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003', 'Alice', 'Martin', '1991-04-12', '+41791234567', 'Rue du Lac 10', '1207', 'Geneva', 'Switzerland', 'VERIFIED', now() - interval '15 days', now() - interval '15 days'),
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000004', 'Bob', 'Keller', '1986-09-03', '+41798765432', 'Bahnhofstrasse 8', '8001', 'Zurich', 'Switzerland', 'VERIFIED', now() - interval '13 days', now() - interval '13 days');

insert into bank_accounts (id, customer_id, iban, account_number, currency, balance, status, account_type, daily_transfer_limit, created_at, updated_at)
values
('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'CH9300762011623852957', '1000000001', 'CHF', 25000.00, 'ACTIVE', 'CHECKING', 15000.00, now() - interval '14 days', now() - interval '1 day'),
('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'CH5600762011623852958', '1000000002', 'EUR', 5000.00, 'ACTIVE', 'SAVINGS', 8000.00, now() - interval '14 days', now() - interval '2 days'),
('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000002', 'CH6800762011623852959', '1000000003', 'CHF', 8000.00, 'ACTIVE', 'CHECKING', 10000.00, now() - interval '12 days', now() - interval '1 day');

insert into bank_transactions (id, type, status, source_account_id, target_account_id, amount, currency, description, reference, created_at, completed_at, initiated_by_user_id)
values
('30000000-0000-0000-0000-000000000001', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000001', 25000.00, 'CHF', 'Initial demo deposit', 'DEMO-DEP-0001', now() - interval '13 days', now() - interval '13 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000002', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000002', 5000.00, 'EUR', 'Initial demo deposit', 'DEMO-DEP-0002', now() - interval '13 days', now() - interval '13 days', '00000000-0000-0000-0000-000000000002'),
('30000000-0000-0000-0000-000000000003', 'DEPOSIT', 'COMPLETED', null, '20000000-0000-0000-0000-000000000003', 8000.00, 'CHF', 'Initial demo deposit', 'DEMO-DEP-0003', now() - interval '11 days', now() - interval '11 days', '00000000-0000-0000-0000-000000000002');

insert into suspicious_activity_alerts (id, account_id, transaction_id, alert_type, severity, message, status, created_at)
values
('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', null, 'LARGE_TRANSFER', 'MEDIUM', 'Demo alert: large transfer requires review.', 'OPEN', now() - interval '2 days');

insert into audit_logs (id, actor_user_id, action, entity_type, entity_id, ip_address, user_agent, metadata, created_at)
values
('50000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'SEED_DEMO_DATA', 'SYSTEM', 'demo', '127.0.0.1', 'flyway', '{"note":"Demo banking data seeded"}', now() - interval '12 days');
