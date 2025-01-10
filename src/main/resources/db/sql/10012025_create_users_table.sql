--liquibase formatted sql
--changeset n.akimov:create_users_table

CREATE TABLE IF NOT EXISTS users
(
    id          UUID    PRIMARY KEY   NOT NULL,
    username    VARCHAR UNIQUE        NOT NULL,
    password    VARCHAR               NOT NULL
);

COMMENT ON TABLE users IS 'Пользователи';

COMMENT ON COLUMN users.id IS 'id пользователя';
COMMENT ON COLUMN users.username IS 'Логин пользователя';
COMMENT ON COLUMN users.password IS 'Пароль пользователя';