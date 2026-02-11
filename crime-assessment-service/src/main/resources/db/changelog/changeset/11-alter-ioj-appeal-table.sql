--liquibase formatted sql

--changeset nigelpain:11a-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN is_current;

ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN modified_by;

ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN modified_at;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN case_management_unit_id SET NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN receipt_date TYPE DATE;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_date TYPE DATE;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN reason TYPE TEXT;
ALTER TABLE ioj_appeal.ioj_appeal ADD CONSTRAINT chk_reason
    CHECK (reason IN ('NEW', 'PRI', 'JR'));

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN assessor TYPE TEXT;
ALTER TABLE ioj_appeal.ioj_appeal ADD CONSTRAINT chk_assessor
    CHECK (assessor IN ('CASEWORKER', 'JUDGE'));

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_reason TYPE TEXT;
ALTER TABLE ioj_appeal.ioj_appeal ADD CONSTRAINT chk_decision_reason
    CHECK (decision_reason IN ('LOSS_OF_LIBERTY', 'SUSPENDED_SENTENCE', 'LOSS_OF_LIVELIHOOD',
                               'DAMAGE_TO_REPUTATION', 'QUESTION_OF_LAW', 'UNDERSTAND_PROCEEDINGS',
                               'WITNESS_TRACE', 'SKILL_EXAM', 'INTERESTS_PERSON', 'OTHER'));

--changeset nigelpain:11b-alter-ioj-appeal-table.sql dbms:postgresql
DROP CAST IF EXISTS (character varying AS ioj_appeal.reason_enum);
DROP TYPE IF EXISTS ioj_appeal.reason_enum;

DROP CAST IF EXISTS (character varying AS ioj_appeal.assessor_enum);
DROP TYPE IF EXISTS ioj_appeal.assessor_enum;

DROP CAST IF EXISTS (character varying AS ioj_appeal.decision_reason_enum);
DROP TYPE IF EXISTS ioj_appeal.decision_reason_enum;

--rollback dbms:postgresql
CREATE TYPE ioj_appeal.reason_enum AS ENUM ('NEW', 'PRI', 'JR');
CREATE CAST (character varying AS ioj_appeal.reason_enum) with inout as assignment;

CREATE TYPE ioj_appeal.assessor_enum AS ENUM ('CASEWORKER', 'JUDGE');
CREATE CAST (character varying AS ioj_appeal.assessor_enum) with inout as assignment;

CREATE TYPE ioj_appeal.decision_reason_enum AS ENUM ('LOSS_OF_LIBERTY', 'SUSPENDED_SENTENCE',
    'LOSS_OF_LIVELIHOOD', 'DAMAGE_TO_REPUTATION', 'QUESTION_OF_LAW', 'UNDERSTAND_PROCEEDINGS',
    'WITNESS_TRACE', 'SKILL_EXAM', 'INTERESTS_PERSON', 'OTHER');
CREATE CAST (character varying AS ioj_appeal.decision_reason_enum) with inout as assignment;

--rollback
ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN is_current BOOLEAN;

ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN modified_by TEXT;

ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN modified_at TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN case_management_unit_id DROP NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN receipt_date TYPE TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_date TYPE TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal DROP CONSTRAINT chk_reason;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN reason TYPE ioj_appeal.reason_enum
    USING reason::ioj_appeal.reason_enum;

ALTER TABLE ioj_appeal.ioj_appeal DROP CONSTRAINT chk_assessor;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN assessor TYPE ioj_appeal.assessor_enum
    USING assessor::ioj_appeal.assessor_enum;

ALTER TABLE ioj_appeal.ioj_appeal DROP CONSTRAINT chk_decision_reason;
ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_reason TYPE ioj_appeal.decision_reason_enum
    USING decision_reason::ioj_appeal.decision_reason_enum;
