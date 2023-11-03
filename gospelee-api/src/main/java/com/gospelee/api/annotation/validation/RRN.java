package com.gospelee.api.annotation.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RRNValidator.class)
public @interface RRN {
    String message() default "주민등록번호 유효성검사";
    Class[] groups() default {};
    Class[] payload() default {};
}