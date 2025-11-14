--liquibase formatted sql

--changeset jumatov:1
ALTER TABLE users
DROP COLUMN role;
