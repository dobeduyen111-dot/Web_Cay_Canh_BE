package ceb.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final String PHONE_REGEX = "^(0|\\+84)[0-9]{8,10}$";

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null) {
            return true;
        }

        String normalizedPhone = phone.trim().replaceAll("\\s+", "");
        if (normalizedPhone.isEmpty()) {
            return true;
        }

        if (!normalizedPhone.matches(PHONE_REGEX)) {
            ValidationUtils.buildConstraintViolationWithTemplate(context, "", "Khong dung dinh dang so dien thoai. Vui long kiem tra lai!");
            return false;
        }

        return true;
    }
}
