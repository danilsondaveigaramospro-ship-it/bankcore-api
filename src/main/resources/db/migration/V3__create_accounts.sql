create table bank_accounts (
    id uuid primary key default gen_random_uuid(),
    customer_id uuid not null references customer_profiles(id),
    iban varchar(34) not null unique,
    account_number varchar(30) not null unique,
    currency varchar(3) not null check (currency in ('CHF', 'EUR', 'USD')),
    balance numeric(19, 2) not null default 0.00 check (balance >= 0),
    status varchar(20) not null check (status in ('ACTIVE', 'FROZEN', 'CLOSED')),
    account_type varchar(20) not null check (account_type in ('CHECKING', 'SAVINGS', 'BUSINESS')),
    daily_transfer_limit numeric(19, 2) not null check (daily_transfer_limit >= 0),
    version bigint not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    closed_at timestamptz
);
