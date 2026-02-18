package uk.gov.justice.laa.crime.assessmentservice.iojappeal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ioj.appeal.migration")
public record IojAppealMigrationProperties(boolean legacyReadFallbackEnabled) {}
