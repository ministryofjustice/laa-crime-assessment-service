package uk.gov.justice.laa.crime.assessmentservice.common.dto.validation;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ValidationErrorDTO {
    String code;
    String message;
    List<String> messageList;
}
