package com.patientmanagement.patientservice.dto.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.Period;

@Slf4j
public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {
    int minAge;

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext constraintValidatorContext) {
        if (dateOfBirth == null) {
            return true; // let @NotNull handle this
        }
        log.info(Period.between(dateOfBirth, LocalDate.now()).getYears() + " years old---------------------------------------------------------------------------------------------------------------------------------------");

        return Period.between(dateOfBirth, LocalDate.now()).getYears() >= minAge;

    }
}
