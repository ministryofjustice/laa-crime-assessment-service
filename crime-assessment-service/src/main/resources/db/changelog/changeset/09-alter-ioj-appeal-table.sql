--liquibase formatted sql
--changeset jhunt:09-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal RENAME COLUMN legacy_case_id TO legacy_application_id;   -- This references the rep_id/maat_id in the legacy system (which is used inconsistently).