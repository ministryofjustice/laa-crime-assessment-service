--liquibase formatted sql
--changeset mhowell:13_add_rolled_back_at_column_to_ioj_appeal_table.sql
ALTER TABLE ioj_appeal.ioj_appeal
    ADD COLUMN rolled_back_at TIMESTAMP WITH TIME ZONE;

--changeset mhowell:13-create_active_index dbms:postgresql
CREATE INDEX idx_ioj_appeal_active
    ON ioj_appeal.ioj_appeal (appeal_id)
    WHERE rolled_back_at IS NULL;