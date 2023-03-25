package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.Generated;

import java.util.HashMap;

@Generated
@Data
public class Response<T> {

    T body;

    int httpstatus;
    private HashMap<String, String> headers;

    public Response(T obj){
        body=obj;
        headers = new HashMap<String, String>(); //TODO: Use a case-insensitive hashmap here
    }



}
