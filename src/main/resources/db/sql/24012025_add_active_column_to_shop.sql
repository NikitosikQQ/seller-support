--liquibase formatted sql
--changeset n.akimov:add_active_column_to_shop

ALTER TABLE shops
    ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;

UPDATE shops
SET active = TRUE;
