--liquibase formatted sql
--changeset n.akimov:create_table_workplace_material_rate

CREATE TABLE IF NOT EXISTS workplace_material_rate (
    workplace   VARCHAR NOT NULL,
    material_name VARCHAR NOT NULL REFERENCES material(name) ON DELETE CASCADE,
    coefficient DECIMAL NOT NULL DEFAULT 1.0,
    PRIMARY KEY (workplace, material_name)
);

COMMENT ON TABLE workplace_material_rate IS 'Связка между рабочими местами и материалами для указания коэффицента ставки сотрудникам';

COMMENT ON COLUMN workplace_material_rate.workplace IS 'Рабочее место';
COMMENT ON COLUMN workplace_material_rate.material_name IS 'Наименование материала';
COMMENT ON COLUMN workplace_material_rate.coefficient IS 'Коэффицент ставки';