--liquibase formatted sql
--changeset n.akimov:add_new_columns

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS workplaces JSONB DEFAULT '[]'::jsonb;

UPDATE users
SET workplaces = '[]'::jsonb
WHERE users.workplaces is NULL;

INSERT INTO roles (id, name)
VALUES
    ('93739aaa-5ccf-42bc-a8c4-f8f951ab94e9', 'EMPLOYEE') ON CONFLICT (id) DO NOTHING;

ALTER TABLE material
    ADD COLUMN IF NOT EXISTS is_only_packaging BOOLEAN DEFAULT FALSE;

UPDATE material
SET is_only_packaging = false
WHERE is_only_packaging is null;
