--liquibase formatted sql
--changeset n.akimov:create_index_external_order_number

CREATE INDEX IF NOT EXISTS idx_external_order_number ON orders (external_order_number);