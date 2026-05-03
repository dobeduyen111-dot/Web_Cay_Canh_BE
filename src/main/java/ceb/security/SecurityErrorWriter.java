package ceb.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ceb.exception.ApiError;
import ceb.exception.ApiErrorEntry;
import ceb.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, HttpStatus status, ErrorCode errorCode, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                ApiErrorEntry.builder()
                        .unifiedErrorCode(errorCode.getCode())
                        .errorCode(errorCode.name())
                        .errorMessage(message)
                        .build()));
    }
}
