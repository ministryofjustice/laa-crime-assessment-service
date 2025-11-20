--liquibase formatted sql
--changeset jhunt:04-seed-test-ioj-appeal-data context:test
INSERT INTO ioj_appeal.ioj_appeal (appeal_id, rep_id, receipt_date, appeal_reason, appeal_assessor,
                                   appeal_decision, decision_reason, notes, decision_date, appeal_status,
                                   legacy_appeal_id, case_management_unit_id, created_by, created_at,
                                   modified_by, modified_at)
VALUES ('4e57a50b-33a7-48ae-a9e5-5364e19e73aa', 111, '2025-12-02 08:10:00', 'NEW', 
        'CASEWORKER', 'PASS', 'LOSS_OF_LIBERTY', 'Some notes here', '2025-12-10 12:00:00', 
        'COMPLETE', 1, 1, 'test_user', '2025-11-26 15:00:00', 'test_user', '2025-11-26 15:00:00'),
       ('0822419e-4617-48ef-93ac-0898050e58e2', 222, '2025-12-01 10:15:00', 'JR',
        'JUDGE', 'FAIL', 'SUSPENDED_SENTENCE', 'Some notes here', '2025-12-08 11:00:00',
        'IN PROGRESS', 2, 2, 'test_user', '2025-11-20 09:00:00', 'test_user', '2025-11-21 14:45:00');