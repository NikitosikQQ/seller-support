--liquibase formatted sql
--changeset n.akimov:create_article_promo_info_and_material_tables

CREATE TABLE IF NOT EXISTS material
(
    id                      UUID    PRIMARY KEY   NOT NULL,
    name                    VARCHAR UNIQUE        NOT NULL,
    separator_name          VARCHAR,
    sorting_posting_by      VARCHAR
);

COMMENT ON TABLE material IS 'Информация по номерам акции из артикула';

COMMENT ON COLUMN material.id IS 'Технический id материала';
COMMENT ON COLUMN material.name IS 'Уникальное наименование материала, используется в таблице общей отчетности по продажам материала за день';
COMMENT ON COLUMN material.separator_name IS 'Наименование строки в отчете, которая отделяет этот материал от других';
COMMENT ON COLUMN material.sorting_posting_by IS 'По какому параметру артикула проводить сортировку материала';

INSERT INTO material (id, name, separator_name, sorting_posting_by)
VALUES
    ('550e8400-e29b-41d4-a716-446655440000', 'ЛДСП', null, null),
    ('550e8400-e29b-41d4-a716-446655440001', 'ЛДСП 25мм', 'ЛДСП 25мм', 'COLOR_NUMBER'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Рейки', 'Стеновые панели', 'COLOR_NAME'),
    ('550e8400-e29b-41d4-a716-446655440003', 'ХДФ', 'ХДФ и ДВП', 'COLOR_NUMBER'),
    ('550e8400-e29b-41d4-a716-446655440004', 'Пластик', 'Пластик', 'COLOR_NUMBER'),
    ('550e8400-e29b-41d4-a716-446655440005', 'ДСП', 'ДСП', 'COLOR_NUMBER'),
    ('550e8400-e29b-41d4-a716-446655440006', 'Полки настенные', 'Полки настенные', 'COLOR_NUMBER'),
    ('550e8400-e29b-41d4-a716-446655440007', 'МДФ', 'МДФ', 'PROMO_NAME'),
    ('550e8400-e29b-41d4-a716-446655440008', 'ЛМДФ', 'ЛМДФ', 'COLOR_NUMBER'),
    ('550e8400-e29b-41d4-a716-446655440009', 'Фасад с присадкой', null, null);

CREATE TABLE IF NOT EXISTS article_promo_info
(
    id                      UUID    PRIMARY KEY   NOT NULL,
    name                    VARCHAR UNIQUE        NOT NULL,
    type                    VARCHAR               NOT NULL,
    quantity_per_sku        INTEGER               NOT NULL,
    material_id             UUID                  NOT NULL
);

COMMENT ON TABLE article_promo_info IS 'Информация по номерам акции из артикула';

COMMENT ON COLUMN article_promo_info.id IS 'Технический id магазина';
COMMENT ON COLUMN article_promo_info.name IS 'Аббревиатура акции, пример - 1Ф';
COMMENT ON COLUMN article_promo_info.type IS 'Материал, принадлежащий к акции';
COMMENT ON COLUMN article_promo_info.material_id IS 'ID материала';
COMMENT ON COLUMN article_promo_info.quantity_per_sku IS 'Реальное количество товара за единицу товара из маркетплейса';

INSERT INTO article_promo_info (id, name, type, material_id, quantity_per_sku)
VALUES
    (gen_random_uuid(), '1А', 'ЛДСП', '550e8400-e29b-41d4-a716-446655440000', 1),
    (gen_random_uuid(), '2А', 'ЛДСП', '550e8400-e29b-41d4-a716-446655440000', 2),
    (gen_random_uuid(), '1Ф', 'Фасад с присадкой', '550e8400-e29b-41d4-a716-446655440009', 1),
    (gen_random_uuid(), '3А', 'ЛДСП', '550e8400-e29b-41d4-a716-446655440001', 1),
    (gen_random_uuid(), '4А', 'ЛДСП', '550e8400-e29b-41d4-a716-446655440001', 2),
    (gen_random_uuid(), '1МДФ', 'МДФ', '550e8400-e29b-41d4-a716-446655440007',  1),
    (gen_random_uuid(), '2МДФ', 'МДФ', '550e8400-e29b-41d4-a716-446655440007', 1),
    (gen_random_uuid(), '3МДФ', 'МДФ', '550e8400-e29b-41d4-a716-446655440007', 1),
    (gen_random_uuid(), '1ХДФ', 'ДВП/ХДФ', '550e8400-e29b-41d4-a716-446655440003', 3),
    (gen_random_uuid(), '1Б', 'Полистирол', '550e8400-e29b-41d4-a716-446655440004', 1),
    (gen_random_uuid(), '1ЛМ', 'ЛМДФ', '550e8400-e29b-41d4-a716-446655440008', 1),
    (gen_random_uuid(), '2ЛМ', 'ЛМДФ', '550e8400-e29b-41d4-a716-446655440008', 1),
    (gen_random_uuid(), '1ДСП', 'ДСП', '550e8400-e29b-41d4-a716-446655440005', 1),
    (gen_random_uuid(), '3Б', 'Полистирол полосы', '550e8400-e29b-41d4-a716-446655440004', 3),
    (gen_random_uuid(), '5Б', 'Полистирол полосы', '550e8400-e29b-41d4-a716-446655440004', 5),
    (gen_random_uuid(), '10Б', 'Полистирол полосы', '550e8400-e29b-41d4-a716-446655440004', 10),
    (gen_random_uuid(), '1П', 'Настенная 100', '550e8400-e29b-41d4-a716-446655440006', 4),
    (gen_random_uuid(), '2П', 'Настенная 150', '550e8400-e29b-41d4-a716-446655440006', 4),
    (gen_random_uuid(), '1СП', 'Реечная Линии, Ромб', '550e8400-e29b-41d4-a716-446655440002', 4),
    (gen_random_uuid(), '2СП', 'Реечная Волна', '550e8400-e29b-41d4-a716-446655440002', 4),
    (gen_random_uuid(), '3СП', 'Реечная Рейки 700 мм', '550e8400-e29b-41d4-a716-446655440002', 4),
    (gen_random_uuid(), '4СП', 'Реечная Рейки 700 мм', '550e8400-e29b-41d4-a716-446655440002', 1),
    (gen_random_uuid(), '5СП', 'Реечная Ромб, Рейки', '550e8400-e29b-41d4-a716-446655440002', 1);