--liquibase formatted sql
--changeset n.akimov:create_roles_table

CREATE TABLE IF NOT EXISTS roles
(
    id    UUID    PRIMARY KEY   NOT NULL,
    name  VARCHAR UNIQUE        NOT NULL
);

COMMENT ON TABLE roles IS 'Роли пользователей';

COMMENT ON COLUMN roles.id IS 'id роли';
COMMENT ON COLUMN roles.name IS 'Наименование роли';

INSERT INTO roles (id, name)
VALUES
    (gen_random_uuid(), 'ADMIN'),
    (gen_random_uuid(), 'MANAGER');