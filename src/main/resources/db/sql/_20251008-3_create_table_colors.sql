--liquibase formatted sql
--changeset n.akimov:create_tables_colors

CREATE TABLE IF NOT EXISTS colors
(
    id                      UUID        PRIMARY KEY NOT NULL,
    number                  INTEGER     UNIQUE      NOT NULL,
    name                    VARCHAR                 NOT NULL
);

COMMENT ON TABLE colors IS 'Справочник цветов';

COMMENT ON COLUMN colors.id IS 'Уникальный идентификатор цвета (UUID)';
COMMENT ON COLUMN colors.number IS 'Номер цвета';
COMMENT ON COLUMN colors.name IS 'Наименование цвета';

INSERT INTO colors (id, number, name) VALUES
      (gen_random_uuid(), 1,  'Белый'),
      (gen_random_uuid(), 2,  'Венге'),
      (gen_random_uuid(), 3,  'Серый'),
      (gen_random_uuid(), 4,  'Графит'),
      (gen_random_uuid(), 5,  'Слоновая Кость'),
      (gen_random_uuid(), 6,  'Вишня'),
      (gen_random_uuid(), 7,  'Дуб Сонома'),
      (gen_random_uuid(), 8,  'Ясень Шимо'),
      (gen_random_uuid(), 9,  'Дуб Вотан'),
      (gen_random_uuid(), 10, 'Бук'),
      (gen_random_uuid(), 11, 'Орех'),
      (gen_random_uuid(), 12, 'Бетон'),
      (gen_random_uuid(), 13, 'Шампань'),
      (gen_random_uuid(), 15, 'Джексон'),
      (gen_random_uuid(), 16, 'Ясень Шимо Темный'),
      (gen_random_uuid(), 17, 'Дуб Корбридж'),
      (gen_random_uuid(), 18, 'Дуб Юта'),
      (gen_random_uuid(), 19, 'Юта'),
      (gen_random_uuid(), 20, 'Дуб Делано Светлый'),
      (gen_random_uuid(), 21, 'Дуб Делано Темный'),
      (gen_random_uuid(), 23, 'Дуб Молочный'),
      (gen_random_uuid(), 25, 'Дуб Апрельский'),
      (gen_random_uuid(), 26, 'Капучино'),
      (gen_random_uuid(), 27, 'Мокко'),
      (gen_random_uuid(), 28, 'Кашемир');

