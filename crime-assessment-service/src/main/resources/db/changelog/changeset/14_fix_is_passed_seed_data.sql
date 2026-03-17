--liquibase formatted sql
--changeset mhowell:14_fix_is_passed_seed_data.sql context:test

UPDATE ioj_appeal.ioj_appeal SET is_passed = true WHERE appeal_id = '4e57a50b-33a7-48ae-a9e5-5364e19e73aa';
UPDATE ioj_appeal.ioj_appeal SET is_passed = false WHERE appeal_id = '0822419e-4617-48ef-93ac-0898050e58e2';