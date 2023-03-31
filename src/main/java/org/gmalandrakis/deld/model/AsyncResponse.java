package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Generated;

import java.util.concurrent.CompletableFuture;

@Generated
@Data
@EqualsAndHashCode(callSuper=true)
public class AsyncResponse<T> extends CompletableFuture<T> {

    T body;

    int httpStatus = 0;
    private CaseInsensitiveHashMap<String, String> headers = new CaseInsensitiveHashMap<String, String>();

    private String associatedRequestId;

    private boolean failed;

    public AsyncResponse() {

    }
}
