package org.gmalandrakis.deld.exception;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class IncompatibleAnnotationsException extends GenericDELDException {

    public IncompatibleAnnotationsException(Method method, Annotation annotation1, Annotation annotation2) {
        super("Incompatible Annotations on method " + method.getName() + " . At most one of the following may be used: " + annotation1.annotationType().getName() + ", " + annotation2.annotationType().getName());
        this.method = method;
    }

    public IncompatibleAnnotationsException(Parameter parameter, Annotation annotation1, Annotation annotation2) {
        super("Incompatible Annotations on parameter " + parameter.getName()+ " . At most one of the following may be used: " + annotation1.annotationType().getName() + ", " + annotation2.annotationType().getName());
        this.parameter = parameter;
    }
}
