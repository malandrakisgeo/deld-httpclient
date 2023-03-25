package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.Generated;

import java.sql.Timestamp;
import java.util.HashMap;

@Generated
@Data
public class Request<T> {

    private Method httpMethod;

    private T body;

    private String url;

    private Timestamp timestamp;

    private String traceId; //In case several requests are interdependent or related

    private HashMap<String, String> headers; //TODO: Switch to CaseInsensitiveHashMap

    private HashMap<String, String> queryParameters;

    public Request(T t) {
        this.body = t;
        this.headers = new HashMap<String, String>();
        this.queryParameters = new HashMap<String, String>(); //TODO: Doesn't seem to prevent nullpointers. Why?
    }

    public Request() {
        this.headers = new HashMap<String, String>();
        this.queryParameters = new HashMap<String, String>();

    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

}
