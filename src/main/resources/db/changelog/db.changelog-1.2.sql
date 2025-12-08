--liquibase formatted sql

--changeset jumatov:1
ALTER TABLE users
    DROP CONSTRAINT uc_user;


CREATE UNIQUE INDEX uc_username ON users (username);
