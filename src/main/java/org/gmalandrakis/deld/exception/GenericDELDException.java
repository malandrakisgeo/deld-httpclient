package org.gmalandrakis.deld.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class GenericDELDException extends Exception {

    Method method;

    Parameter parameter;

    public GenericDELDException(String message) {
        super(message);
    }

    public GenericDELDException() {
    }
}
