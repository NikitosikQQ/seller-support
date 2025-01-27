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
    ('66472f2f-66c9-4dac-8d49-2a392ff0e10b', 'ADMIN'),
    ('74a88bf3-4624-4bc3-9e10-0c84302b8ca0', 'MANAGER'),
    ('729f4baf-c01e-4b9b-a560-08074f20db04', 'USER');