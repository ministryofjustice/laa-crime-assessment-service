--liquibase formatted sql
--changeset nigelpain:11-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN case_management_unit_id SET NOT NULL;
ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN is_current;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN receipt_date TYPE DATE;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_date TYPE DATE;
--rollback
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN case_management_unit_id DROP NOT NULL;
ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN is_current BOOLEAN;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN receipt_date TYPE TIMESTAMP;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_date TYPE TIMESTAMP;