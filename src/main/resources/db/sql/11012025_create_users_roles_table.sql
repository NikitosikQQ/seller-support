--liquibase formatted sql
--changeset n.akimov:create_users_roles_table

CREATE TABLE IF NOT EXISTS users_roles (
   user_id UUID NOT NULL,
   role_id UUID NOT NULL,
   PRIMARY KEY (user_id, role_id),
   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
   FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

COMMENT ON TABLE users_roles IS 'Связь пользователей с их ролями';

INSERT INTO users_roles (user_id, role_id)
    VALUES
        ('a7b06927-c49a-4493-bab1-d6712f275bd6', '66472f2f-66c9-4dac-8d49-2a392ff0e10b');