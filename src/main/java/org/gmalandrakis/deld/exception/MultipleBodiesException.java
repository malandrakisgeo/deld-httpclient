package org.gmalandrakis.deld.exception;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class MultipleBodiesException extends GenericDELDException {

    public MultipleBodiesException(Method method) {
        super("Multiple request bodies defined on " + method.getName() + ".At most one object may be used as request body");
        this.method = method;
    }


}
