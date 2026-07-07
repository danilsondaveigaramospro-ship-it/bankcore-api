create table bank_transactions (
    id uuid primary key default gen_random_uuid(),
    type varchar(30) not null check (type in ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT', 'REVERSAL')),
    status varchar(30) not null check (status in ('PENDING', 'COMPLETED', 'FAILED', 'REJECTED')),
    source_account_id uuid references bank_accounts(id),
    target_account_id uuid references bank_accounts(id),
    amount numeric(19, 2) not null check (amount > 0),
    currency varchar(3) not null check (currency in ('CHF', 'EUR', 'USD')),
    description varchar(280) not null,
    reference varchar(80) not null unique,
    failure_reason varchar(500),
    created_at timestamptz not null default now(),
    completed_at timestamptz,
    initiated_by_user_id uuid references users(id)
);
