--liquibase formatted sql
--changeset n.akimov:create_comment_table

CREATE TABLE IF NOT EXISTS comment
(
    id              UUID    PRIMARY KEY   NOT NULL,
    value           VARCHAR               NOT NULL,
    conditions      JSONB,
    created_at      timestamp             NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE comment IS 'Информация по комментариям для отправлений из магазинов';

COMMENT ON COLUMN comment.id IS 'Технический id ';
COMMENT ON COLUMN comment.value IS 'Значение комментария(что будет в отчете)';
COMMENT ON COLUMN comment.conditions IS 'Условия, при котором добавляется комментарий к артикулу';
COMMENT ON COLUMN comment.created_at IS 'Дата создания комментария';