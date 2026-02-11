--liquibase formatted sql
--changeset mhowell:11-alter-ioj-appeal-event-table dbms:h2

ALTER TABLE ioj_appeal.ioj_appeal_event
    RENAME COLUMN payload TO audit_payload;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ALTER COLUMN appeal_id DROP NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ALTER COLUMN audit_payload DROP NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ADD COLUMN trace_id VARCHAR;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ADD COLUMN event_type VARCHAR NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ADD COLUMN legacy_appeal_id BIGINT;

-- H2 doesn't do TIMESTAMPTZ; use:
ALTER TABLE ioj_appeal.ioj_appeal_event
    ALTER COLUMN triggered_at SET DATA TYPE TIMESTAMP WITH TIME ZONE;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ALTER COLUMN triggered_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ADD CONSTRAINT chk_ioj_appeal_event_type
        CHECK (event_type IN ('CREATE', 'ROLLBACK', 'FIND') AND event_type <> '');

-- IF NOT EXISTS is not supported in some H2 versions; safest is just CREATE INDEX:
CREATE INDEX ioj_appeal_event_appeal_id_triggered_at_idx
    ON ioj_appeal.ioj_appeal_event (appeal_id, triggered_at DESC);
