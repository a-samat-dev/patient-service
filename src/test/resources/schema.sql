drop table if exists patients;
create table if not exists patients
(
    id                   uuid primary key,
    user_id              uuid         not null,
    first_name           varchar(155) not null,
    last_name            varchar(155),
    birth_date           date         not null,
    phone_number         varchar(20),
    family_connection_id smallint,
    iin                  varchar(15),
    created_at           timestamp with time zone,
    updated_at           timestamp with time zone,
    created_by           varchar(155) not null,
    updated_by           varchar(155) not null,
    deleted_at           timestamp with time zone
);