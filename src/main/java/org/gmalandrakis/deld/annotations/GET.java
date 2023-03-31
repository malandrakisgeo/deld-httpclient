package org.gmalandrakis.deld.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;


import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface GET {


    String url() default ""; //if using a baseurl

    String fullUrl() default "";

    ; //whole url


}
