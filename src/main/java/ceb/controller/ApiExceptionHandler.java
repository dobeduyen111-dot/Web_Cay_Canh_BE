package ceb.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import ceb.exception.ApiError;
import ceb.exception.ApiErrorEntry;
import ceb.exception.BadRequestException;
import ceb.exception.ErrorCode;
import ceb.exception.ExceptionHandlerBase;
import ceb.exception.ResourceNotFoundException;
import ceb.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler extends ExceptionHandlerBase {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException ex) {
        List<ApiErrorEntry> apiErrorEntries = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String violationName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
                    String message = violationName + " " + error.getDefaultMessage();
                    return toApiErrorEntry(ErrorCode.VALIDATION_ERROR, String.format(ErrorCode.VALIDATION_ERROR.getMessage(), message));
                }).toList();

        logError("handleMethodArgumentNotValid", request, apiErrorEntries, null);
        return buildResponse(HttpStatus.BAD_REQUEST, apiErrorEntries);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiError> handleHandlerMethodValidation(HttpServletRequest request, HandlerMethodValidationException ex) {
        List<ApiErrorEntry> apiErrorEntries = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> result.getResolvableErrors().forEach(error -> {
            String parameterName = result.getMethodParameter().getParameterName();
            String violationName = parameterName == null ? "parameter" : parameterName;
            String message = violationName + " " + error.getDefaultMessage();
            apiErrorEntries.add(toApiErrorEntry(ErrorCode.VALIDATION_ERROR, String.format(ErrorCode.VALIDATION_ERROR.getMessage(), message)));
        }));

        logError("handleHandlerMethodValidation", request, apiErrorEntries, null);
        return buildResponse(HttpStatus.BAD_REQUEST, apiErrorEntries);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(HttpServletRequest request, ConstraintViolationException ex) {
        List<ApiErrorEntry> apiErrorEntries = ex.getConstraintViolations().stream()
                .map(violation -> {
                    String message = violation.getPropertyPath().toString() + " " + violation.getMessage();
                    return toApiErrorEntry(ErrorCode.VALIDATION_ERROR, String.format(ErrorCode.VALIDATION_ERROR.getMessage(), message));
                }).toList();

        logError("handleConstraintViolation", request, apiErrorEntries, null);
        return buildResponse(HttpStatus.BAD_REQUEST, apiErrorEntries);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(HttpServletRequest request, ResourceNotFoundException ex) {
        List<ApiErrorEntry> entries = List.of(toApiErrorEntry(ex));
        logError("handleNotFound", request, entries, ex);
        return buildResponse(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(HttpServletRequest request, BadRequestException ex) {
        List<ApiErrorEntry> entries = List.of(toApiErrorEntry(ex));
        logError("handleBadRequest", request, entries, ex);
        return buildResponse(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(HttpServletRequest request, UnauthorizedException ex) {
        List<ApiErrorEntry> entries = List.of(toApiErrorEntry(ex));
        logError("handleUnauthorized", request, entries, ex);
        return buildResponse(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(HttpServletRequest request, Exception ex) {
        List<ApiErrorEntry> entries = List.of(toApiErrorEntry(ErrorCode.UNKNOWN_ERROR, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau."));
        logErrorWithStackTrace("handleGeneric", request, entries, ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.UNKNOWN_ERROR, "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.");
    }
}
