package ceb.exception;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ExceptionHandlerBase {

    @Autowired
    protected ObjectMapper objectMapper;

    protected ResponseEntity<ApiError> buildResponse(HttpStatus status, DomainException exception) {
        return ResponseEntity.status(status).body(new ApiError(toApiErrorEntry(exception)));
    }

    protected ResponseEntity<ApiError> buildResponse(HttpStatus status, ErrorCode errorCode, String message) {
        return ResponseEntity.status(status).body(new ApiError(toApiErrorEntry(errorCode, message)));
    }

    protected ResponseEntity<ApiError> buildResponse(HttpStatus status, List<ApiErrorEntry> errorEntries) {
        return ResponseEntity.status(status).body(new ApiError(errorEntries));
    }

    protected ApiErrorEntry toApiErrorEntry(DomainException exception) {
        return toApiErrorEntry(exception.getErrorCode(), exception.getMessage());
    }

    protected ApiErrorEntry toApiErrorEntry(ErrorCode errorCode, String message) {
        return ApiErrorEntry.builder()
                .unifiedErrorCode(errorCode.getCode())
                .errorCode(errorCode.name())
                .errorMessage(message)
                .build();
    }

    protected void logError(String methodException, HttpServletRequest request, List<ApiErrorEntry> apiErrorEntries, Throwable exception) {
        log.error("method: {}, endpoint: {}, queryString: {}, apiErrorEntries: {}",
                methodException,
                request != null ? request.getRequestURI() : "null",
                request != null ? request.getQueryString() : "null",
                parseApiErrorToJsonString(apiErrorEntries));
    }

    protected void logErrorWithStackTrace(String methodException, HttpServletRequest request, List<ApiErrorEntry> apiErrorEntries, Throwable exception) {
        log.error("method: {}, endpoint: {}, queryString: {}, apiErrorEntries: {}",
                methodException,
                request != null ? request.getRequestURI() : "null",
                request != null ? request.getQueryString() : "null",
                parseApiErrorToJsonString(apiErrorEntries),
                exception);
    }

    protected String parseApiErrorToJsonString(List<ApiErrorEntry> apiErrorEntries) {
        try {
            return objectMapper != null ? objectMapper.writeValueAsString(apiErrorEntries) : "[]";
        } catch (Exception ex) {
            log.error("Error occurred during convert ApiErrorEntry to json string", ex);
            return null;
        }
    }
}
