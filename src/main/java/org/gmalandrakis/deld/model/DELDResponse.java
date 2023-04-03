package org.gmalandrakis.deld.model;

import java.util.HashMap;

public interface DELDResponse<T> {
     CaseInsensitiveHashMap<String, String> headers = new CaseInsensitiveHashMap<String, String>();


    default void setHeaders(HashMap<String, String> headersHashMap){
        this.headers.putAll(headersHashMap);
    }

    default void setHeaders(CaseInsensitiveHashMap<String, String> headers){
        this.headers.putAll(headers);
    }

    default CaseInsensitiveHashMap<String, String> getHeaders(){
        return headers;
    }

    void setBody(T body);
    void setHttpStatus(int status);


}
