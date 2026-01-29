--liquibase formatted sql
--changeset mhowell:11-alter-ioj-appeal-event-table.sql
ALTER TABLE ioj_appeal.ioj_appeal_event
    RENAME COLUMN payload TO audit_payload;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ALTER COLUMN appeal_id DROP NOT NULL,
    ALTER COLUMN audit_payload DROP NOT NULL;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ADD COLUMN trace_id TEXT,
    ADD COLUMN event_type TEXT NOT NULL,
    ADD COLUMN legacy_appeal_id BIGINT;

ALTER TABLE ioj_appeal.ioj_appeal_event
    ALTER COLUMN triggered_at TYPE TIMESTAMPTZ,
    ALTER COLUMN triggered_at SET DEFAULT now();

ALTER TABLE ioj_appeal.ioj_appeal_event
    ADD CONSTRAINT chk_ioj_appeal_event_type
        CHECK (event_type IN ('CREATE', 'ROLLBACK', 'FIND') AND event_type <> '');

-- ALTER TABLE ioj_appeal.ioj_appeal_event
--     ADD CONSTRAINT chk_ioj_appeal_identifier_present
--         CHECK (appeal_id IS NOT NULL OR legacy_appeal_id IS NOT NULL);

CREATE INDEX IF NOT EXISTS ioj_appeal_event_appeal_id_triggered_at_idx
    ON ioj_appeal.ioj_appeal_event (appeal_id, triggered_at DESC);


