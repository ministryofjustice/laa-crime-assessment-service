--liquibase formatted sql
--changeset jhunt:07-seed-test-ioj-appeal-data context:test
UPDATE ioj_appeal.ioj_appeal SET is_latest_appeal_assessment = true WHERE appeal_id = '4e57a50b-33a7-48ae-a9e5-5364e19e73aa';
UPDATE ioj_appeal.ioj_appeal SET is_latest_appeal_assessment = true WHERE appeal_id = '0822419e-4617-48ef-93ac-0898050e58e2';