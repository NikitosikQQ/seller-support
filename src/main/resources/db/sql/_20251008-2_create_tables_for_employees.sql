--liquibase formatted sql
--changeset n.akimov:create_tables_for_employees

CREATE TABLE IF NOT EXISTS employees_processed_capacity
(
    id                 UUID        PRIMARY KEY,
    username           VARCHAR     NOT NULL,
    capacity           DECIMAL     NOT NULL DEFAULT 0,
    workplace          VARCHAR     NOT NULL,
    earned_amount      DECIMAL     NOT NULL DEFAULT 0,
    processed_at       DATE        NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_employees_username ON employees_processed_capacity (username);
CREATE INDEX IF NOT EXISTS idx_employees_username ON employees_processed_capacity (processed_at, workplace);

COMMENT ON TABLE employees_processed_capacity IS 'Таблица с обработанным объёмом сотрудников';

COMMENT ON COLUMN employees_processed_capacity.id IS 'Идентификатор записи';
COMMENT ON COLUMN employees_processed_capacity.username IS 'Логин работника';
COMMENT ON COLUMN employees_processed_capacity.capacity IS 'Обработанный объём (в условных единицах)';
COMMENT ON COLUMN employees_processed_capacity.earned_amount IS 'Заработано рублей';
COMMENT ON COLUMN employees_processed_capacity.workplace IS 'Рабочее место';
COMMENT ON COLUMN employees_processed_capacity.processed_at IS 'Дата обработки заказов';



CREATE TABLE IF NOT EXISTS employees_activity_history
(
    id               UUID          PRIMARY KEY,
    username         VARCHAR        NOT NULL,
    workplace        VARCHAR        NOT NULL,
    capacity         DECIMAL     NOT NULL DEFAULT 0,
    amount           DECIMAL        NOT NULL,
    operation_type   VARCHAR        NOT NULL,
    processed_at     DATE           NOT NULL,
    created_at       TIMESTAMP      NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_employees_activity_username_created_at
    ON employees_activity_history (username, processed_at, created_at);

COMMENT ON TABLE employees_activity_history IS 'История изменений по сотрудникам (объем, заработок, операции)';

COMMENT ON COLUMN employees_activity_history.id IS 'Идентификатор записи';
COMMENT ON COLUMN employees_activity_history.username IS 'Имя пользователя (сотрудника)';
COMMENT ON COLUMN employees_activity_history.workplace IS 'Рабочее место, где выполнялась операция';
COMMENT ON COLUMN employees_activity_history.amount IS 'Сумма операции';
COMMENT ON COLUMN employees_activity_history.capacity IS 'Выполненный за операцию объем';
COMMENT ON COLUMN employees_activity_history.operation_type IS 'Тип операции: прибавка или убавка';
COMMENT ON COLUMN employees_activity_history.processed_at IS 'Дата и время обработки заказа';
COMMENT ON COLUMN employees_activity_history.created_at IS 'Дата и время изменения';


CREATE TABLE IF NOT EXISTS workplace_rates
(
    id               UUID                  PRIMARY KEY,
    workplace        VARCHAR   UNIQUE      NOT NULL,
    rate             DECIMAL               NOT NULL
);

COMMENT ON TABLE workplace_rates IS 'Ставка заработка на рабочем месте';

COMMENT ON COLUMN workplace_rates.id IS 'Идентификатор записи';
COMMENT ON COLUMN workplace_rates.workplace IS 'Наименование рабочего места';
COMMENT ON COLUMN workplace_rates.rate IS 'Ставка';

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO workplace_rates (id, workplace, rate) VALUES
          (gen_random_uuid(), 'PILA1', 0),
          (gen_random_uuid(), 'PILA2', 0),
          (gen_random_uuid(), 'PILA_MASTER', 0),
          (gen_random_uuid(), 'CHPU', 0),
          (gen_random_uuid(), 'KROMSHIK', 0),
          (gen_random_uuid(), 'UPAKOVSHIK', 0),
          (gen_random_uuid(), 'UPAKOVSHIK_MEBEL', 0)