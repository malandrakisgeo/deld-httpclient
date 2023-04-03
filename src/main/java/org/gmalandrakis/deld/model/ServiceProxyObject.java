package org.gmalandrakis.deld.model;

import lombok.Data;
import lombok.Generated;

@Generated
@Data
public class ServiceProxyObject {

    private Object proxyObject;
    private String baseUrl;
    private CaseInsensitiveHashMap<String, String> commonHeaders;

    //TODO: Add stuff such as port, etc

    public Object setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return proxyObject;
    }

}
