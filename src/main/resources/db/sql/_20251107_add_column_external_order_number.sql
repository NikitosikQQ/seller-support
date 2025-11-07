--liquibase formatted sql
--changeset n.akimov:add_column_external_order_number

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS external_order_number VARCHAR;

UPDATE orders
SET external_order_number = number