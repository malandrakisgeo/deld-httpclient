package org.gmalandrakis.deld.annotations;

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
