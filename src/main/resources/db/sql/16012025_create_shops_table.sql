--liquibase formatted sql
--changeset n.akimov:create_shops_table

CREATE TABLE IF NOT EXISTS shops
(
    id              UUID    PRIMARY KEY   NOT NULL,
    name            VARCHAR               NOT NULL,
    marketplace     VARCHAR               NOT NULL,
    pallet_number   INTEGER               NOT NULL,
    api_key         VARCHAR               NOT NULL,
    client_id       VARCHAR

);

COMMENT ON TABLE shops IS 'Информация по магазинам маркетплейса';

COMMENT ON COLUMN shops.id IS 'Технический id магазина';
COMMENT ON COLUMN shops.name IS 'Наименование магазина';
COMMENT ON COLUMN shops.marketplace IS 'Тип маркетплейса';
COMMENT ON COLUMN shops.pallet_number IS 'Номер паллета, привязанного к магазину';
COMMENT ON COLUMN shops.api_key IS 'Зашифрованный апи ключ магазина для интеграции';
COMMENT ON COLUMN shops.client_id IS 'Зашифрованный id клиента из маркетплейса для интеграции';