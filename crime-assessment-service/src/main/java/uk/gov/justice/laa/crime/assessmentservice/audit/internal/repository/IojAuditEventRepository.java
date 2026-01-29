package uk.gov.justice.laa.crime.assessmentservice.audit.internal.repository;

import uk.gov.justice.laa.crime.assessmentservice.audit.internal.entity.IojAuditEventEntity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IojAuditEventRepository extends JpaRepository<IojAuditEventEntity, UUID> {}
