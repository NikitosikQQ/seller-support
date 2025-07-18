--liquibase formatted sql
--changeset n.akimov:add_chpu_columns_to_material_and_article

ALTER TABLE material
    ADD COLUMN IF NOT EXISTS use_in_chpu_template BOOLEAN DEFAULT FALSE;

ALTER TABLE material
    ADD COLUMN IF NOT EXISTS chpu_material_name VARCHAR;

ALTER TABLE material
    ADD COLUMN IF NOT EXISTS chpu_article_number VARCHAR;

ALTER TABLE article_promo_info
    ADD COLUMN IF NOT EXISTS chpu_material_id UUID;


UPDATE material
SET use_in_chpu_template = false
WHERE use_in_chpu_template IS NULL;

