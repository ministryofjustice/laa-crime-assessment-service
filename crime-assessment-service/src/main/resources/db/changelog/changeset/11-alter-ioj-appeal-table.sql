--liquibase formatted sql
--changeset nigelpain:11a-alter-ioj-appeal-table.sql
ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN is_current;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN is_current BOOLEAN;

ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN modified_by;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN modified_by TEXT;

ALTER TABLE ioj_appeal.ioj_appeal DROP COLUMN modified_at;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ADD COLUMN modified_at TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN case_management_unit_id SET NOT NULL;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN case_management_unit_id DROP NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN receipt_date TYPE DATE;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN receipt_date TYPE TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_date TYPE DATE;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_date TYPE TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN reason TYPE TEXT;
ALTER TABLE ioj_appeal.ioj_appeal ADD CONSTRAINT chk_reason
    CHECK (reason IN ('NEW', 'PRI', 'JR'));
--rollback ALTER TABLE ioj_appeal.ioj_appeal DROP CONSTRAINT chk_reason;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN reason TYPE ioj_appeal.reason_enum USING reason::ioj_appeal.reason_enum;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN assessor TYPE TEXT;
ALTER TABLE ioj_appeal.ioj_appeal ADD CONSTRAINT chk_assessor
    CHECK (assessor IN ('CASEWORKER', 'JUDGE'));
--rollback ALTER TABLE ioj_appeal.ioj_appeal DROP CONSTRAINT chk_assessor;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN assessor TYPE ioj_appeal.assessor_enum USING assessor::ioj_appeal.assessor_enum;

ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_reason TYPE TEXT;
ALTER TABLE ioj_appeal.ioj_appeal ADD CONSTRAINT chk_decision_reason
    CHECK (decision_reason IN ('LOSS_OF_LIBERTY', 'SUSPENDED_SENTENCE', 'LOSS_OF_LIVELIHOOD',
                               'DAMAGE_TO_REPUTATION', 'QUESTION_OF_LAW', 'UNDERSTAND_PROCEEDINGS',
                               'WITNESS_TRACE', 'SKILL_EXAM', 'INTERESTS_PERSON', 'OTHER'));
--rollback ALTER TABLE ioj_appeal.ioj_appeal DROP CONSTRAINT chk_decision_reason;
--rollback ALTER TABLE ioj_appeal.ioj_appeal ALTER COLUMN decision_reason TYPE ioj_appeal.decision_reason_enum USING decision_reason::ioj_appeal.decision_reason_enum;

--changeset nigelpain:11b-alter-ioj-appeal-table.sql dbms:postgresql
DROP CAST IF EXISTS (character varying AS ioj_appeal.reason_enum);
DROP TYPE IF EXISTS ioj_appeal.reason_enum;
--rollback dbms:postgresql CREATE TYPE ioj_appeal.reason_enum AS ENUM ('NEW', 'PRI', 'JR');
--rollback dbms:postgresql CREATE CAST (character varying AS ioj_appeal.reason_enum) with inout as assignment;

DROP CAST IF EXISTS (character varying AS ioj_appeal.assessor_enum);
DROP TYPE IF EXISTS ioj_appeal.assessor_enum;
--rollback dbms:postgresql CREATE TYPE ioj_appeal.assessor_enum AS ENUM ('CASEWORKER', 'JUDGE');
--rollback dbms:postgresql CREATE CAST (character varying AS ioj_appeal.assessor_enum) with inout as assignment;

DROP CAST IF EXISTS (character varying AS ioj_appeal.decision_reason_enum);
DROP TYPE IF EXISTS ioj_appeal.decision_reason_enum;
--rollback dbms:postgresql CREATE TYPE ioj_appeal.decision_reason_enum AS ENUM ('LOSS_OF_LIBERTY', 'SUSPENDED_SENTENCE', 'LOSS_OF_LIVELIHOOD', 'DAMAGE_TO_REPUTATION', 'QUESTION_OF_LAW', 'UNDERSTAND_PROCEEDINGS','WITNESS_TRACE', 'SKILL_EXAM', 'INTERESTS_PERSON', 'OTHER');
--rollback dbms:postgresql CREATE CAST (character varying AS ioj_appeal.decision_reason_enum) with inout as assignment;