package com.patientmanagement.patientservice.dto.validators;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgeValidator.class)
public @interface ValidAge {

    String message() default "Patient must be at least 18 years old";

    int min() default 18;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
