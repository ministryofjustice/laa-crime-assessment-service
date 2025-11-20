--liquibase formatted sql
--changeset jhunt:08-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal RENAME COLUMN is_latest_appeal_assessment TO is_current;

ALTER TABLE ioj_appeal.ioj_appeal RENAME COLUMN appeal_reason TO reason;
CREATE TYPE ioj_appeal.reason_enum AS ENUM ('NEW', 'PRI', 'JR');    -- Previous Record Incorrect, Judicial Review
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN reason TYPE ioj_appeal.reason_enum 
    USING reason::ioj_appeal.reason_enum;

ALTER TABLE ioj_appeal.ioj_appeal RENAME COLUMN appeal_assessor TO assessor;
CREATE TYPE ioj_appeal.assessor_enum AS ENUM ('CASEWORKER', 'JUDGE');
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN assessor TYPE ioj_appeal.assessor_enum 
    USING assessor::ioj_appeal.assessor_enum;

ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN appeal_decision;
ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN is_passed BOOLEAN;

CREATE TYPE ioj_appeal.decision_reason_enum AS ENUM ('LOSS_OF_LIBERTY', 'SUSPENDED_SENTENCE', 
    'LOSS_OF_LIVELIHOOD', 'DAMAGE_TO_REPUTATION', 'QUESTION_OF_LAW', 'UNDERSTAND_PROCEEDINGS', 
    'WITNESS_TRACE', 'SKILL_EXAM', 'INTERESTS_PERSON', 'OTHER');
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_reason TYPE ioj_appeal.decision_reason_enum 
    USING decision_reason::ioj_appeal.decision_reason_enum;

ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN appeal_status;