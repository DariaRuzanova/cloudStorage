CREATE SCHEMA IF NOT EXISTS cloud_DB;
create table if not exists cloud_DB.users
(
    id serial primary key,
    login varchar(50) not null unique,
    password varchar(100) not null
);


create table  if not exists cloud_DB.files
(
    id serial primary key,
    filename varchar(50) unique,
    type varchar(10) not null ,
    fileContent bytea not null,
    date timestamp,
    size bigint not null,
    user_id serial,
    CONSTRAINT fk_users_files FOREIGN KEY (user_id)
        REFERENCES cloud_DB.users (id) MATCH SIMPLE
);