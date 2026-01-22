package uk.gov.justice.laa.crime.assessmentservice.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class BadRequestLoggingAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        log.info("400 Validation failed on {} {}: {}", req.getMethod(), req.getRequestURI(), errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Void> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        // This is usually malformed JSON, wrong types, enum mismatch, date format issues, etc.
        log.info(
                "400 Could not read request body on {} {}: {}",
                req.getMethod(),
                req.getRequestURI(),
                rootCauseMessage(ex),
                ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Void> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        log.info("400 Missing request parameter on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    private String formatFieldError(FieldError fe) {
        Object rejected = fe.getRejectedValue();
        return "%s %s (rejected=%s)".formatted(fe.getField(), fe.getDefaultMessage(), rejected);
    }

    private String rootCauseMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur.getMessage();
    }
}
