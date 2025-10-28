--liquibase formatted sql
--changeset jhunt:06-alter-ioj-appeal-event-table.sql
CREATE INDEX idx_appeal_id ON ioj_appeal.ioj_appeal_event(appeal_id);

CREATE TYPE event_type_enum AS ENUM ('CREATED', 'UPDATED', 'STATUS_CHANGED');
ALTER TABLE ioj_appeal.ioj_appeal_event ALTER COLUMN event_type TYPE event_type_enum USING event_type::event_type_enum;