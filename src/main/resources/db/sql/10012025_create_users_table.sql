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

INSERT INTO users (id, username, password)
    VALUES
        ('a7b06927-c49a-4493-bab1-d6712f275bd6', 'admin', '$2a$10$caC3Ym3AyRBmIfEd3u2aHeAmbm0YyiAaoRgCgezbNnG9RcbSibJJ6');