package com.jromax.bcisecurityuserservice.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class PropertyPatternValidator implements ConstraintValidator<PropertyPattern, String> {

    @Autowired
    private Environment environment;

    private String propertyKey;
    private boolean allowEmpty;

    @Override
    public void initialize(PropertyPattern annotation) {
        this.propertyKey = annotation.property();
        this.allowEmpty = annotation.allowEmpty();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return allowEmpty; // Let @NotNull or other constraints handle requiredness
        }
        String regex = environment != null ? environment.getProperty(propertyKey) : null;
        if (regex == null || regex.trim().isEmpty()) {
            // If not configured, consider it valid to avoid blocking runtime; alternatively, could return false
            return true;
        }
        try {
            boolean matches = Pattern.compile(regex).matcher(value).matches();
            if (!matches) {
                // Try to resolve message from application.properties
                String message = null;
                if (environment != null) {
                    // 1) propertyKey + ".message"
                    String k1 = propertyKey + ".message";
                    message = environment.getProperty(k1);
                    if (message == null) {
                        // 2) replace trailing "-regex" with "-message" if present
                        if (propertyKey.endsWith("-regex")) {
                            String k2 = propertyKey.substring(0, propertyKey.length() - 6) + "-message";
                            message = environment.getProperty(k2);
                        }
                    }
                }
                if (message != null && !message.isEmpty()) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
                }
            }
            return matches;
        } catch (Exception e) {
            // Invalid regex configuration; fail-safe: treat as valid to avoid noisy validation failure.
            return true;
        }
    }
}
