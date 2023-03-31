package org.gmalandrakis.deld.core;


import lombok.Data;
import lombok.Generated;
import org.gmalandrakis.deld.model.AsyncResponse;
import org.gmalandrakis.deld.model.Response;
import org.gmalandrakis.deld.model.ServiceProxyObject;
import org.gmalandrakis.deld.utils.DELDObjectConverter;
import org.gmalandrakis.deld.utils.HeaderUtils;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Generated
@Data
public final class DELDClient {


    private HashMap<Class, ServiceProxyObject> interfaceProxyList;

    public DELDClient(HashMap<Class, ServiceProxyObject> interfaceProxyList) {
        this.interfaceProxyList = interfaceProxyList;
    }

    public DELDClient() {

    }


    protected AsyncResponse<?> sendAsync(HttpRequest request, Class<?> returnType) throws Exception {
         ExecutorService executor = Executors.newFixedThreadPool(2);

        /*
            Coming soon!
        */
        return null;
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

    private boolean isErrorCode(int httpStatus) {
        return String.valueOf(httpStatus).startsWith("4") || String.valueOf(httpStatus).startsWith("5");
    }

}
