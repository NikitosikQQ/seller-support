--liquibase formatted sql
--changeset n.akimov:create_tables_for_orders

CREATE TABLE IF NOT EXISTS orders
(
    id                      UUID               PRIMARY KEY,
    number                  VARCHAR UNIQUE     NOT NULL,
    status                  VARCHAR            NOT NULL,
    pallet_number           INTEGER            NOT NULL,
    article                 VARCHAR            NOT NULL,
    length                  INTEGER            NOT NULL,
    width                   INTEGER            NOT NULL,
    thickness               INTEGER            NOT NULL,
    color                   VARCHAR            NOT NULL,
    color_number            INTEGER            NOT NULL,
    material_name           VARCHAR            NOT NULL,
    quantity                INTEGER            NOT NULL,
    marketplace             VARCHAR            NOT NULL,
    shop_name               VARCHAR            NOT NULL,
    in_process_at           TIMESTAMP          NOT NULL,
    total_price             DECIMAL            NOT NULL,
    area_in_meters          DECIMAL            NOT NULL,
    price_per_square_meter  INTEGER            NOT NULL,
    comment                 VARCHAR,
    promo_name              VARCHAR     NOT NULL,
    created_at              TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_number ON orders (number);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders (status);
CREATE INDEX IF NOT EXISTS idx_orders_in_process_at ON orders (created_at);
CREATE INDEX IF NOT EXISTS idx_orders_length_width ON orders (length, width);

COMMENT ON TABLE orders IS 'Таблица заказов';

COMMENT ON COLUMN orders.id IS 'Уникальный идентификатор заказа (UUID)';
COMMENT ON COLUMN orders.number IS 'Номер заказа';
COMMENT ON COLUMN orders.status IS 'Текущий статус заказа';
COMMENT ON COLUMN orders.pallet_number IS 'Номер паллеты';
COMMENT ON COLUMN orders.article IS 'Артикул товара';
COMMENT ON COLUMN orders.length IS 'Длина изделия (мм)';
COMMENT ON COLUMN orders.width IS 'Ширина изделия (мм)';
COMMENT ON COLUMN orders.thickness IS 'Толщина изделия (мм)';
COMMENT ON COLUMN orders.color IS 'Цвет изделия';
COMMENT ON COLUMN orders.color_number IS 'Номер цвета изделия';
COMMENT ON COLUMN orders.quantity IS 'Количество единиц товара';
COMMENT ON COLUMN orders.marketplace IS 'Площадка (маркетплейс)';
COMMENT ON COLUMN orders.shop_name  IS 'Название магазина';
COMMENT ON COLUMN orders.in_process_at IS 'Дата и время начала обработки';
COMMENT ON COLUMN orders.total_price IS 'Общая стоимость заказа';
COMMENT ON COLUMN orders.area_in_meters IS 'Площадь заказа в квадратных метрах';
COMMENT ON COLUMN orders.price_per_square_meter IS 'Цена за квадратный метр';
COMMENT ON COLUMN orders.comment IS 'Комментарий к заказу';
COMMENT ON COLUMN orders.promo_name IS 'Наименование акции/цены';
COMMENT ON COLUMN orders.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN orders.updated_at IS 'Дата и время последнего обновления';



CREATE TABLE IF NOT EXISTS orders_changes_history
(
    id                  UUID               PRIMARY KEY,
    order_number        VARCHAR            NOT NULL,
    status              VARCHAR            NOT NULL,
    author              VARCHAR            NOT NULL,
    workplace           VARCHAR,
    created_at          TIMESTAMP          NOT NULL DEFAULT NOW(),
    order_id            UUID               NOT NULL,

    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_orders_changes_history_order_number
    ON orders_changes_history (order_number);
CREATE INDEX IF NOT EXISTS idx_orders_changes_history_author
    ON orders_changes_history (author);

-- Комментарии
COMMENT ON TABLE orders_changes_history IS 'История изменений заказов';

COMMENT ON COLUMN orders_changes_history.id IS 'Идентификатор записи истории';
COMMENT ON COLUMN orders_changes_history.order_number IS 'Номер заказа, к которому относится изменение';
COMMENT ON COLUMN orders_changes_history.status IS 'Статус заказа после изменения';
COMMENT ON COLUMN orders_changes_history.author IS 'Автор изменений';
COMMENT ON COLUMN orders_changes_history.workplace IS 'Рабочее место, с которого произведено изменение';
COMMENT ON COLUMN orders_changes_history.created_at IS 'Дата и время фиксации изменения';