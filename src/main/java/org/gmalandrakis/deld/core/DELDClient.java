package org.gmalandrakis.deld.core;


import lombok.Data;
import lombok.Generated;
import org.gmalandrakis.deld.model.AsyncResponse;
import org.gmalandrakis.deld.model.DELDResponse;
import org.gmalandrakis.deld.model.Response;
import org.gmalandrakis.deld.model.ServiceProxyObject;
import org.gmalandrakis.deld.utils.DELDObjectConverter;
import org.gmalandrakis.deld.utils.HeaderUtils;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Generated
@Data
public final class DELDClient {

    private ExecutorService executorService;
    private HashMap<Class<?>, ServiceProxyObject> interfaceProxyList;
    private HttpClient client;


    public DELDClient() {
         this(2);
    }

    public DELDClient(int threadsNeeded) {
        if (threadsNeeded < 1) {
            throw new RuntimeException("At least one thread required.");
        }
        this.executorService = Executors.newFixedThreadPool(threadsNeeded);
        this.client = HttpClient.newBuilder().executor(executorService).connectTimeout(Duration.ofSeconds(5)).build(); //TODO: Let the user define the timeout
    }

    DELDResponse<?> handleSync(HttpRequest request, Class<?> returnType) throws Exception {
        Response<?> response = new Response<>();
        HttpResponse.BodyHandler<?> properHandler = HttpResponse.BodyHandlers.ofString();

        if (HeaderUtils.acceptHeader(request, "application/octet-stream")) {
            if (returnType.isInstance(InputStream.nullInputStream())) {
                properHandler = HttpResponse.BodyHandlers.ofInputStream();
            } else {
                properHandler = HttpResponse.BodyHandlers.ofByteArray();
            }
            var streamResponse = client.send(request, properHandler);
            handleStream(streamResponse, response);
        } else { //application/json or application/xml
            var objResponse = client.send(request, properHandler);
            handleObject(objResponse, response, returnType, HeaderUtils.accepts(request));
        }
        return response;
    }

    DELDResponse<?> handleAsync(HttpRequest request, Class<?> returnType) throws Exception {
        AsyncResponse<?> asyncResponse = new AsyncResponse<>();
        HttpResponse.BodyHandler<?> properHandler = HttpResponse.BodyHandlers.ofString();
        if (HeaderUtils.acceptHeader(request, "application/octet-stream")) {
            if (returnType.isInstance(InputStream.nullInputStream())) {
                properHandler = HttpResponse.BodyHandlers.ofInputStream();
            } else {
                properHandler = HttpResponse.BodyHandlers.ofByteArray();
            }
            client.sendAsync(request, properHandler)
                    .thenApply(httpResp -> handleStream(httpResp, asyncResponse))
                    .get(5, TimeUnit.SECONDS);
        } else {
            client.sendAsync(request, properHandler)
                    .thenApply(httpResp -> handleObject(httpResp, asyncResponse, returnType, HeaderUtils.accepts(request)))
                    .get(5, TimeUnit.SECONDS);
        }
        return asyncResponse;
    }


    private static DELDResponse<?> handleStream(HttpResponse<?> obj, DELDResponse rsp) {
        if (obj.body() != null) {
            rsp.setBody(obj.body());
        } else {
            rsp.setBody(InputStream.nullInputStream());
        }
        rsp.setHttpStatus(obj.statusCode());
        rsp.setHeaders(HeaderUtils.httpHeadersToHashMap(obj.headers().map()));
        return rsp;
    }

    private static DELDResponse<?> handleObject(HttpResponse<?> obj, DELDResponse rsp, Class<?> returnType, String accepts) {
        try {
            if (obj.body() != null) {
                if (notError(obj.statusCode())) {
                    if (accepts != null && accepts.contains("json")) {
                        rsp.setBody(DELDObjectConverter.objectConverterJson(obj.body().toString(), returnType));
                    } else if (accepts != null && accepts.contains("xml")) {
                        try {
                            rsp.setBody(DELDObjectConverter.objectConverterXml(obj.body().toString(), returnType));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    } else {//assume json
                        rsp.setBody(DELDObjectConverter.objectConverterJson(obj.body().toString(), returnType));
                    }
                } else {
                    rsp.setBody(obj.body()); //TODO: Test
                }
            }
            rsp.setHttpStatus(obj.statusCode());
            rsp.setHeaders(HeaderUtils.httpHeadersToHashMap(obj.headers().map()));
        } catch (Exception e) {
            e.printStackTrace();
            rsp.setBody(e);
            rsp.setHttpStatus(500);
        }
        return rsp;
    }

    private static boolean notError(int httpStatus) {
        return String.valueOf(httpStatus).startsWith("2"); //TODO: It is not that simple, is it?
    }

}
