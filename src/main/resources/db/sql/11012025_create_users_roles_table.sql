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