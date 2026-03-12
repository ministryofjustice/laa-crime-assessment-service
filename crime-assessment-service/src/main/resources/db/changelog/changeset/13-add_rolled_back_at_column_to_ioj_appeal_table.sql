--liquibase formatted sql
--changeset mhowell:13-add_rolled_back_at_column_to_ioj_appeal_table.sql dbms: postgres
ALTER TABLE ioj_appeal.ioj_appeal
    ADD COLUMN rolled_back_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX IF NOT EXISTS idx_ioj_appeal_active
    ON ioj_appeal.ioj_appeal (appeal_id)
    WHERE rolled_back_at IS NULL;