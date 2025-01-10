--liquibase formatted sql
--changeset n.akimov:create_test_table

CREATE TABLE IF NOT EXISTS users
(
    id          UUID    PRIMARY KEY   NOT NULL,
    login       VARCHAR UNIQUE        NOT NULL,
    password    VARCHAR               NOT NULL
);

COMMENT ON TABLE users IS 'Пользователи';

COMMENT ON COLUMN users.id IS 'id пользователя';
COMMENT ON COLUMN users.login IS 'Логин пользователя';
COMMENT ON COLUMN users.password IS 'Пароль пользователя';