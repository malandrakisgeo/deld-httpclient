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

    private String requestId; //TODO: If a user chooses so, each request will be assigned an id for debug purposes

    private CaseInsensitiveHashMap<String, String>  headers = new CaseInsensitiveHashMap<String, String> ();

    private CaseInsensitiveHashMap<String, String>  queryParameters = new CaseInsensitiveHashMap<String, String> ();

    public Request(T t) {
        this.body = t;
    }

    public Request() {
    }

    public enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }

}
