--liquibase formatted sql
--changeset n.akimov:update_workplace_material_rate_pk

ALTER TABLE workplace_material_rate
    ADD COLUMN id UUID;

ALTER TABLE workplace_material_rate
    ADD COLUMN min_area_in_meters DECIMAL DEFAULT 0,
    ADD COLUMN max_area_in_meters DECIMAL DEFAULT 0;

UPDATE workplace_material_rate
SET id = gen_random_uuid()
WHERE id IS NULL;

UPDATE workplace_material_rate
SET min_area_in_meters = 0
WHERE min_area_in_meters IS NULL;

UPDATE workplace_material_rate
SET max_area_in_meters = 0
WHERE max_area_in_meters IS NULL;

ALTER TABLE workplace_material_rate
DROP CONSTRAINT workplace_material_rate_pkey;

ALTER TABLE workplace_material_rate
    ALTER COLUMN id SET NOT NULL;

ALTER TABLE workplace_material_rate
    ADD PRIMARY KEY (id);

COMMENT ON COLUMN workplace_material_rate.id IS 'Уникальный идентификатор записи';
COMMENT ON COLUMN workplace_material_rate.min_area_in_meters IS 'Минимальный размер включительно для применения коэфицента';
COMMENT ON COLUMN workplace_material_rate.max_area_in_meters IS 'Максимальный размер включительно для применения коэфицента';