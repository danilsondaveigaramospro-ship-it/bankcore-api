create table suspicious_activity_alerts (
    id uuid primary key default gen_random_uuid(),
    account_id uuid not null references bank_accounts(id),
    transaction_id uuid references bank_transactions(id),
    alert_type varchar(40) not null check (alert_type in ('LARGE_TRANSFER', 'MANY_TRANSFERS', 'FAILED_ATTEMPTS', 'UNUSUAL_WITHDRAWAL')),
    severity varchar(20) not null check (severity in ('LOW', 'MEDIUM', 'HIGH')),
    message varchar(500) not null,
    status varchar(20) not null check (status in ('OPEN', 'REVIEWED', 'DISMISSED')),
    created_at timestamptz not null default now(),
    reviewed_at timestamptz,
    reviewed_by uuid references users(id)
);
