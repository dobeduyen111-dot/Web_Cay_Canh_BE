package ceb.exception;

import java.util.Arrays;
import java.util.List;

public class ApiError {

    private final List<ApiErrorEntry> errors;

    public ApiError(ApiErrorEntry... errorEntries) {
        errors = Arrays.asList(errorEntries);
    }

    public ApiError(List<ApiErrorEntry> apiErrorEntries) {
        errors = apiErrorEntries;
    }

    public List<ApiErrorEntry> getErrors() {
        return errors;
    }
}
