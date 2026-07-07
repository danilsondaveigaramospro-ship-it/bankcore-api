create table audit_logs (
    id uuid primary key default gen_random_uuid(),
    actor_user_id uuid references users(id),
    action varchar(120) not null,
    entity_type varchar(120) not null,
    entity_id varchar(120),
    ip_address varchar(80),
    user_agent varchar(500),
    metadata text,
    created_at timestamptz not null default now()
);
