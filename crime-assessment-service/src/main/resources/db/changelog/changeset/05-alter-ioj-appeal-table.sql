--liquibase formatted sql
--changeset jhunt:05-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal RENAME COLUMN rep_id TO legacy_case_id;   -- This references the rep_id/maat_id in the legacy system (which is used inconsistently).

ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN is_latest_appeal_assessment BOOLEAN;