package com.gospelee.api.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RRNValidator implements ConstraintValidator<RRN, String> {

    private static final String RRNRegex = "^([0-9]{6})-?([0-9]{7})$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        return value.matches(RRNRegex);
    }
}
