package com.jromax.bcisecurityuserservice.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = PropertyPatternValidator.class)
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Retention(RUNTIME)
public @interface PropertyPattern {
    String message() default "Value does not match required pattern";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Spring property key that holds the regex pattern to apply.
     */
    String property();

    /**
     * Optional: whether to allow nulls/empty values (controller-level @Valid often allows optional fields).
     */
    boolean allowEmpty() default true;
}
