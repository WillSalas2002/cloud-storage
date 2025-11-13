--liquibase formatted sql

--changeset jumatov:1
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(128) NOT NULL,
    password VARCHAR(128) NOT NULL,
    role VARCHAR(128) NOT NULL,
    CONSTRAINT uc_user UNIQUE (id)
);
