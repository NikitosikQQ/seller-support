--liquibase formatted sql
--changeset n.akimov:create_article_promo_info

CREATE TABLE IF NOT EXISTS article_promo_info
(
    id                  UUID    PRIMARY KEY   NOT NULL,
    name                VARCHAR UNIQUE        NOT NULL,
    material            VARCHAR               NOT NULL,
    quantity_per_sku    INTEGER               NOT NULL
);

COMMENT ON TABLE article_promo_info IS 'Информация по номерам акции из артикула';

COMMENT ON COLUMN article_promo_info.id IS 'Технический id магазина';
COMMENT ON COLUMN article_promo_info.name IS 'Аббревиатура акции, пример - 1Ф';
COMMENT ON COLUMN article_promo_info.material IS 'Материал, принадлежащий к акции';
COMMENT ON COLUMN article_promo_info.quantity_per_sku IS 'Реальное количество товара за единицу товара из маркетплейса';

INSERT INTO article_promo_info (id, name, material, quantity_per_sku)
VALUES
    (gen_random_uuid(), '1А', 'ЛДСП', 1),
    (gen_random_uuid(), '2А', 'ЛДСП', 2),
    (gen_random_uuid(), '1Ф', 'Фасад с присадкой', 1),
    (gen_random_uuid(), '3А', 'ЛДСП', 1),
    (gen_random_uuid(), '4А', 'ЛДСП', 2),
    (gen_random_uuid(), '1МДФ', 'МДФ', 1),
    (gen_random_uuid(), '2МДФ', 'МДФ', 1),
    (gen_random_uuid(), '3МДФ', 'МДФ', 1),
    (gen_random_uuid(), '1ХДФ', 'ДВП/ХДФ', 3),
    (gen_random_uuid(), '1Б', 'Полистирол', 1),
    (gen_random_uuid(), '1ЛМ', 'ЛМДФ', 1),
    (gen_random_uuid(), '2ЛМ', 'ЛМДФ', 1),
    (gen_random_uuid(), '1ДСП', 'ДСП', 1),
    (gen_random_uuid(), '3Б', 'Полистирол полосы', 3),
    (gen_random_uuid(), '5Б', 'Полистирол полосы', 5),
    (gen_random_uuid(), '10Б', 'Полистирол полосы', 10),
    (gen_random_uuid(), '1П', 'Настенная 100', 4),
    (gen_random_uuid(), '2П', 'Настенная 150', 4),
    (gen_random_uuid(), '1СП', 'Реечная Линии, Ромб, Рейки', 4),
    (gen_random_uuid(), '2СП', 'Реечная Волна', 4),
    (gen_random_uuid(), '3СП', 'Реечная Рейки 700 мм', 4),
    (gen_random_uuid(), '4СП', 'Реечная Рейки 700 мм', 1),
    (gen_random_uuid(), '5СП', 'Реечная Ромб, Рейки', 1);