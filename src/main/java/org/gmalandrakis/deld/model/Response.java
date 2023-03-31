package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.Generated;

import java.util.HashMap;

@Generated
@Data
public class Response<T> {
    T body;

    int httpStatus = 0;
    private CaseInsensitiveHashMap<String, String>  headers= new CaseInsensitiveHashMap<String, String> ();

    private String associatedRequestId;

    private boolean failed;

    public Response(T obj){
        body=obj;
    }

    public boolean isFailed(){
        return String.valueOf(httpStatus).startsWith("4") || String.valueOf(httpStatus).startsWith("5");
    }

    public void setHeaders(HashMap<String, String> headersHashMap){
        this.headers.putAll(headersHashMap);
    }

    public void setHeaders(CaseInsensitiveHashMap<String, String> headers){
        this.headers = headers;
    }

}
