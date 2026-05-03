package ceb.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = {PasswordValidator.class})
public @interface ValidPassword {

    String message() default "Mat khau phai tu 6 den 72 ky tu, bao gom it nhat 1 chu cai va 1 chu so";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
