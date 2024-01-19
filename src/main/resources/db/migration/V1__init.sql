create table if not exists roles(
    id         uuid not null  primary key,
    created_at timestamp,
    updated_at timestamp,
    name       varchar(255)
);

create table if not exists users(
    id         uuid not null primary key,
    created_at timestamp,
    updated_at timestamp,
    email      varchar(255) constraint uk6dotkott2kjsp8vw4d0m25fb7    unique,
    name       varchar(255),
    password   varchar(255),
    username   varchar(255) constraint ukr43af9ap4edm43mmtq01oddj6 unique
);

create table if not exists user_roles(
    user_id uuid not null constraint fkhfh9dx7w3ubf1co1vdev94g3f references users,
    role_id uuid not null constraint fkh8ciramu9cc9q3qcqiv4ue8a6 references roles,
    primary key (user_id, role_id)
);

create table if not exists scheduler(
    id            uuid not null primary key,
    user_id       uuid constraint fk_scheduler_user_id references users,
    cron          varchar(255),
    name          varchar(255),
    description   varchar(255),
    group_name    varchar(255),
    class_name    varchar(255),
    status        varchar(255)
        constraint scheduler_status_check
            check ((status)::text = ANY
                   ((ARRAY ['UNKNOWN'::character varying, 'ENABLED'::character varying, 'DISABLED'::character varying])::text[])),
    status_reason varchar(255),
    created_at    timestamp(6),
    updated_at    timestamp(6)
);

create table if not exists scheduler_result(
    scheduler_id uuid not null constraint fk_scheduler_scheduler_result_id references scheduler,
    result       text
);
