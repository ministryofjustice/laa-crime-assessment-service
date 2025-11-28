--liquibase formatted sql
--changeset dedwards:10-create-ioj-appeal-enum-casts.sql dbms:PostgreSQL
--comment: /* Do not run if h2, as it does not have appropriate language support for this file. */
--comment: /* h2 is only for unit tests, and the functionality this file creates is only needed in Postgres */
CREATE CAST (character varying AS ioj_appeal.assessor_enum) with inout as assignment;
CREATE CAST (character varying AS ioj_appeal.reason_enum) with inout as assignment;
CREATE CAST (character varying AS ioj_appeal.decision_reason_enum) with inout as assignment;