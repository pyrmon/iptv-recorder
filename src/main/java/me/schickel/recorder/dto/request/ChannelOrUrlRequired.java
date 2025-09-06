package me.schickel.recorder.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ChannelOrUrlValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ChannelOrUrlRequired {
    String message() default "Either channel name or M3U URL must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}