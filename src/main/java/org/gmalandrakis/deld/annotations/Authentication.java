package org.gmalandrakis.deld.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({PARAMETER, METHOD})
public @interface Authentication {
//TODO: Implementation
    AuthType authType();

    String username() default "";

    String password() default "";

    String token() default "";

    String refreshToken() default "";

    public enum AuthType {
        BASIC,
        TOKEN
    }
}
