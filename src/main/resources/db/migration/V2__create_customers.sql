create table customer_profiles (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null unique references users(id),
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    date_of_birth date not null,
    phone_number varchar(30),
    address_line1 varchar(255) not null,
    address_line2 varchar(255),
    postal_code varchar(30) not null,
    city varchar(100) not null,
    country varchar(100) not null,
    kyc_status varchar(20) not null check (kyc_status in ('PENDING', 'VERIFIED', 'REJECTED')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);
