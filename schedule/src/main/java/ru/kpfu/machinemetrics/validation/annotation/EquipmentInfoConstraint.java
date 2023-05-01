package ru.kpfu.machinemetrics.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.kpfu.machinemetrics.validation.validator.EquipmentInfoValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;

@Documented
@Constraint(validatedBy = EquipmentInfoValidator.class)
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EquipmentInfoConstraint {

    String message() default "{exception.general}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
