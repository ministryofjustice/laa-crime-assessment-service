package uk.gov.justice.laa.crime.assessmentservice.iojappeal.repository;

import uk.gov.justice.laa.crime.assessmentservice.iojappeal.entity.IojAppealEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IojAppealRepository extends JpaRepository<IojAppealEntity, UUID> {

    Optional<IojAppealEntity> findByAppealId(UUID appealId);

    Optional<IojAppealEntity> findByAppealIdAndRolledBackAtIsNull(UUID appealId);

    Optional<IojAppealEntity> findByLegacyAppealIdAndRolledBackAtIsNull(Integer legacyAppealId);

    boolean existsByLegacyAppealIdAndRolledBackAtIsNotNull(Integer legacyAppealId);
}
