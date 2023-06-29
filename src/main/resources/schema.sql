CREATE SCHEMA IF NOT EXISTS cloud_DB;
create table if not exists cloud_DB.users
(
    id serial primary key,
    login varchar(50) not null unique,
    password varchar(100) not null
);
