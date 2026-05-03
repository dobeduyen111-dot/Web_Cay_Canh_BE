package ceb.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    // Must be 6-72 chars, optionally require at least one letter and at least one number
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,72}$";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return true; // Use @NotBlank if it's mandatory
        }

        if (!password.matches(PASSWORD_REGEX)) {
            ValidationUtils.buildConstraintViolationWithTemplate(context, "", "Mat khau phai tu 6 den 72 ky tu, bao gom it nhat 1 bo so va 1 chu cai!");
            return false;
        }

        return true;
    }
}
