package com.example.taskmanager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * HTTP status code (e.g., 400, 404, 500)
     */
    private int status;

    /**
     * Error type/code (e.g., "NOT_FOUND", "VALIDATION_ERROR")
     */
    private String error;

    /**
     * Human-readable error message
     */
    private String message;

    /**
     * Request path that caused the error
     */
    private String path;

    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;

    /**
     * Field-level validation errors (for validation failures)
     */
    private List<FieldError> fieldErrors;

    /**
     * Inner class for field-level validation errors.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
        private Object rejectedValue;
    }


}
