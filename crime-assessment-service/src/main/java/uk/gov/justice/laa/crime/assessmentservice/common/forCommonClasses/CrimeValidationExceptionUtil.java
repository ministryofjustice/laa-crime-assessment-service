package uk.gov.justice.laa.crime.assessmentservice.common.forCommonClasses;

import lombok.experimental.UtilityClass;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

// TODO : This needs to be moved into crime commons.

@UtilityClass
public class CrimeValidationExceptionUtil {

    private static final String MESSAGE_LIST_NAME = "messageList";

    public static ResponseEntity<ProblemDetail> buildValidationErrorResponse(List<String> errorMessageList) {
        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value());
        problemDetail.setProperties(Map.of(MESSAGE_LIST_NAME, errorMessageList));
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }

    public static List<String> getErrorMessages(ProblemDetail problemDetail) {
        if (Objects.isNull(problemDetail)) {
            return List.of();
        }
        // check if we're using messageList.
        List<String> messages = getMessageList(problemDetail);
        if (!messages.isEmpty()) {
            return messages;
        }
        // if empty, check incase we can get detail via another field.
        else if (Objects.nonNull(problemDetail.getDetail())) {
            return List.of(problemDetail.getDetail());
        }
        // otherwise, just return empty.
        return List.of();
    }

    @SuppressWarnings("unchecked")
    // Extract the message list if present, pass an empty list back otherwise.
    private List<String> getMessageList(ProblemDetail problemDetail) {
        Map<String, Object> properties = problemDetail.getProperties();
        if (Objects.nonNull(properties) && properties.containsKey(MESSAGE_LIST_NAME)) {
            Object messageList = properties.get(MESSAGE_LIST_NAME);
            // return the list if it's a list of strings, as expected.
            if (messageList instanceof List
                    && ((List<?>) messageList).getFirst().getClass().equals(String.class)) {
                return (List<String>) messageList;
            }
        }
        return Collections.emptyList();
    }
}
