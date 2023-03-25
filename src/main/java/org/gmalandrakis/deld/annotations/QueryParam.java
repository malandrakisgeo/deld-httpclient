package org.gmalandrakis.deld.annotations;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({PARAMETER, METHOD})
@Retention(RUNTIME)
/**
 The user either sets the param on the method (i.e., if the value is the same for all requests), or on an argument (if the value varies).
 Using it for any non-string parameter will lead to a runtime exception.

 Example:

 @QueryParam(parameterName = "includeData", value = "all")
 public Response<DataObject> getData(@QueryParam(parameterName = "excludeData") String toBeExcluded);


 */
public @interface QueryParam {

    String parameterName() default "";

    String value() default "";
}
