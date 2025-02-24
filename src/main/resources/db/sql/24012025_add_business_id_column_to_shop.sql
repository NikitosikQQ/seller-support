--liquibase formatted sql
--changeset n.akimov:add_business_id_column_to_shop

ALTER TABLE shops
    ADD COLUMN IF NOT EXISTS business_id VARCHAR;
