package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Generated
@Data
@EqualsAndHashCode(callSuper=true)
/**
    Every method expecting an AsyncResponse<T> must have a @Async annotation, either at method or service level.
    The result is obtained either via DELDResponse's getBody(), or via CompletableFuture's get().
 */
public class AsyncResponse<T> extends CompletableFuture<T> implements DELDResponse<T> {

    T body;

    int httpStatus = 0;

    private String associatedRequestId;

    private boolean failed;

    public AsyncResponse() {
    }
    public AsyncResponse(T obj){
        this.setBody(obj);
    }

    @Override
    public void setBody(Object obj){
        body=(T)obj;
        this.complete(body);
    }
}
