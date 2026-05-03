package ceb.validation;

import jakarta.validation.ConstraintValidatorContext;

public class ValidationUtils {

    private ValidationUtils() {
        // Hide implicit public constructor
    }

    public static void buildConstraintViolationWithTemplate(ConstraintValidatorContext context, String propertyNode, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyNode)
                .addConstraintViolation();
    }
}
