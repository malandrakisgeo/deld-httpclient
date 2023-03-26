package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.Generated;

import java.util.HashMap;

@Generated
@Data
public class Response<T> {
    T body;

    int httpStatus = 0;
    private HashMap<String, String> headers= new HashMap<String, String>();//TODO: Use a case-insensitive hashmap here

    private String associatedRequestId;

    private boolean failed;

    public Response(T obj){
        body=obj;
    }

    public boolean isFailed(){
        return String.valueOf(httpStatus).startsWith("4") || String.valueOf(httpStatus).startsWith("5");
    }



}
