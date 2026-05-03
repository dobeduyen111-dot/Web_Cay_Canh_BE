package ceb.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PriceValidator implements ConstraintValidator<ValidPrice, Double> {

    @Override
    public boolean isValid(Double price, ConstraintValidatorContext context) {
        if (price == null) {
            return true; // use @NotNull if mandatory
        }

        if (price <= 0) {
            ValidationUtils.buildConstraintViolationWithTemplate(context, "", "Gia tien phai lon hon 0!");
            return false;
        }
        
        if (price > 1000000000) { // Limit to 1 tỷ for instance
            ValidationUtils.buildConstraintViolationWithTemplate(context, "", "Gia tien khong the vuot qua 1.000.000.000!");
            return false;
        }

        return true;
    }
}
