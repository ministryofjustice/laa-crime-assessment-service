--liquibase formatted sql
--changeset jhunt:06-alter-ioj-appeal-event-table.sql
CREATE INDEX idx_appeal_id ON ioj_appeal.ioj_appeal_event(appeal_id);

ALTER TABLE ioj_appeal.ioj_appeal_event DROP COLUMN event_type;