package com.mukundsankaran.bookit.validation;

/**
 * Created by mukund on 4/14/18.
 *
 * Custom annotation for validating Enum values
 *
 * Borrowed from Source - https://funofprograming.wordpress.com/2016/09/29/java-enum-validator/
 */
import com.mukundsankaran.bookit.validation.impl.EnumValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Constraint(validatedBy = {EnumValidator.class})
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Enum {
    public abstract String message() default "Invalid value";

    public abstract Class<?>[] groups() default {};

    public abstract Class<? extends Payload>[] payload() default {};

    public abstract Class<? extends java.lang.Enum<?>> enumClass();

    public abstract boolean ignoreCase() default false;
}
