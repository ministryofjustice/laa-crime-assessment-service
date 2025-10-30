--liquibase formatted sql
--changeset jhunt:08-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal RENAME COLUMN is_latest_appeal_assessment TO is_current;

CREATE TYPE appeal_reason_enum AS ENUM ('NEW', 'PRI', 'JR');    -- Previous Record Incorrect, Judicial Review
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN appeal_reason TYPE appeal_reason_enum 
    USING appeal_reason::appeal_reason_enum;

CREATE TYPE appeal_assessor_enum AS ENUM ('CASEWORKER', 'JUDGE');
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN appeal_assessor TYPE appeal_assessor_enum 
    USING appeal_assessor::appeal_assessor_enum;

CREATE TYPE appeal_decision_enum AS ENUM ('PASS', 'FAIL');
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN appeal_decision TYPE appeal_decision_enum 
    USING appeal_decision::appeal_decision_enum;

CREATE TYPE decision_reason_enum AS ENUM ('LOSS_OF_LIBERTY', 'SUSPENDED_SENTENCE', 
    'LOSS_OF_LIVELIHOOD', 'DAMAGE_TO_REPUTATION', 'QUESTION_OF_LAW', 'UNDERSTAND_PROCEEDINGS', 
    'WITNESS_TRACE', 'SKILL_EXAM', 'INTERESTS_PERSON', 'OTHER');
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_reason TYPE decision_reason_enum 
    USING decision_reason::decision_reason_enum;