--liquibase formatted sql
--changeset jhunt:02-create-ioj-appeal-table.sql
CREATE TABLE ioj_appeal.ioj_appeal (
    appeal_id UUID PRIMARY KEY,
    rep_id INT NOT NULL,
    receipt_date TIMESTAMP NOT NULL,
    appeal_reason TEXT NOT NULL,
    appeal_assessor TEXT NOT NULL,
    appeal_decision TEXT NOT NULL,
    decision_reason TEXT NOT NULL,
    notes TEXT,
    decision_date TIMESTAMP NOT NULL,
    appeal_status TEXT NOT NULL,
    legacy_appeal_id BIGINT UNIQUE, -- still optional, but each legacy ID maps to one appeal
    case_management_unit_id INT,
    created_by TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    modified_by TEXT,
    modified_at TIMESTAMP
);