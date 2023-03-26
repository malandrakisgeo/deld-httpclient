package org.gmalandrakis.deld.core;


import org.gmalandrakis.deld.model.Response;
import org.gmalandrakis.deld.utils.DELDObjectConverter;
import org.gmalandrakis.deld.utils.HeaderUtils;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;

public final class DELDClient {

    private String baseURL;

    public HashMap<Class, Object> interfaceProxyList;

    public DELDClient(String baseURL, HashMap<Class, Object> interfaceProxyList) {
        this.baseURL = baseURL;
        this.interfaceProxyList = interfaceProxyList;

    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL; //TODO: Set if not null, else warning or exception
    }

    protected Response<?> sendRequest(HttpRequest request, Class<?> returnType) throws Exception {
        HttpResponse<?> clientResp;
        Response<?> response;

        var client = HttpClient.newHttpClient();
        HashMap<String, String> headers = new HashMap<String, String>();

        Object obj = null;
        if (HeaderUtils.acceptHeader(request, "application/octet-stream")) {
            if (returnType.isInstance(InputStream.nullInputStream())) {
                clientResp = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } else {
                clientResp = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            }
            obj = clientResp.body();

        } else if (HeaderUtils.acceptHeader(request, "application/xml")) {
            clientResp = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (clientResp.body() != null && !isErrorCode(clientResp.statusCode())) {
                obj = DELDObjectConverter.objectConverterXml(clientResp.body().toString(), returnType);
            }
        } else { //if no accept-header set, assume json string
            clientResp = client.send(request, HttpResponse.BodyHandlers.ofString());//
            if (clientResp.body() != null && !isErrorCode(clientResp.statusCode())) {
                obj = DELDObjectConverter.objectConverterJson(clientResp.body().toString(), returnType);
            }
        }
        response = new Response<>(obj);

        response.setHttpStatus(clientResp.statusCode());
        headers.putAll(HeaderUtils.httpHeadersToHasgMap(clientResp.headers().map()));
        response.setHeaders(headers);

        return response;

    }

    private boolean isErrorCode(int httpStatus){
        return String.valueOf(httpStatus).startsWith("4") || String.valueOf(httpStatus).startsWith("5");
    }

}
