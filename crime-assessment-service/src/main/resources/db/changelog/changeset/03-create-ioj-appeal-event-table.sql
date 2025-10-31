--liquibase formatted sql
--changeset jhunt:03-create-ioj-appeal-event-table.sql
CREATE TABLE ioj_appeal.ioj_appeal_event (
    id UUID PRIMARY KEY,
    appeal_id UUID NOT NULL REFERENCES ioj_appeal.ioj_appeal(appeal_id),
    event_type TEXT NOT NULL, -- e.g. CREATED, UPDATED, STATUS_CHANGED
    payload JSONB NOT NULL,   -- either full or partial appeal state
    triggered_by TEXT NOT NULL,
    triggered_at TIMESTAMP NOT NULL DEFAULT now()
);