package org.gmalandrakis.deld.model;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE;
    final String method;

    HttpMethod() {
        method = name();
    }

    HttpMethod(String httpVersion) {
        this.method = httpVersion;
    }

    @Override
    public String toString() {
        return method;
    }
}
