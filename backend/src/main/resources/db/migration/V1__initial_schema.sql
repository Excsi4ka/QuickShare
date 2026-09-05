create table users (
    id uuid not null,
    username varchar(255) not null,
    email varchar(255) not null,
    password varchar(255),
    constraint pk_users primary key (id),
    constraint uk_users_email unique (email)
);

create table file_metas (
    id uuid not null,
    user_id uuid not null,
    name varchar(255) not null,
    has_password boolean not null,
    password varchar(255),
    size bigint not null,
    expire_at timestamp(6) with time zone,
    download_limit integer,
    download_count integer not null,
    uploading_status varchar(255) not null,
    uploaded_at timestamp(6) with time zone,
    constraint pk_file_metas primary key (id),
    constraint fk_file_metas_user foreign key (user_id) references users (id)
);

create table refresh_tokens (
    id uuid not null,
    user_id uuid not null,
    created_at timestamp(6) with time zone not null,
    expires_at timestamp(6) with time zone not null,
    constraint pk_refresh_tokens primary key (id),
    constraint fk_refresh_tokens_user foreign key (user_id) references users (id)
);

create index idx_file_metas_user_id on file_metas (user_id);
create index idx_refresh_tokens_user_id on refresh_tokens (user_id);
