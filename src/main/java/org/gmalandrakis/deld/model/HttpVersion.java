package org.gmalandrakis.deld.model;

public enum HttpVersion {

    HTTP_1_0("HTTP/1.0"), HTTP_1_1("HTTP/1.1"), HTTP_2("HTTP/2.0"), TEST;

    final String httpVersion;

    HttpVersion() {
        httpVersion = name();
    }

    HttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    @Override
    public String toString() {
        return httpVersion;
    }

}
