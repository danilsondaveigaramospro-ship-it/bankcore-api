create extension if not exists pgcrypto;

create table users (
    id uuid primary key default gen_random_uuid(),
    email varchar(255) not null unique,
    password_hash varchar(100) not null,
    role varchar(40) not null check (role in ('ROLE_CUSTOMER', 'ROLE_BANK_EMPLOYEE', 'ROLE_ADMIN')),
    status varchar(20) not null check (status in ('ACTIVE', 'DISABLED', 'LOCKED')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    last_login_at timestamptz
);
