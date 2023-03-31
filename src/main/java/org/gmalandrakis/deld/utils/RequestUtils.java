package org.gmalandrakis.deld.utils;

import org.gmalandrakis.deld.model.Request;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.function.Supplier;


public class RequestUtils {

    public static HttpRequest prepareHttpRequest(Request<?> req) {
        var uri = URI.create(req.getUrl());

        if(req.getQueryParameters()!= null && req.getQueryParameters().size() > 0){
            final String[] params = {"?"};
            req.getQueryParameters().forEach((name, value) ->{
                params[0] = params[0].concat("&" + name + "=" + value);
            });
            uri = URI.create(req.getUrl() + params[0]);
        }

        HttpRequest.Builder httpreq = HttpRequest.newBuilder(uri);

        var headerArray = HeaderUtils.hashMapToHttpHeaders(req.getHeaders());
        if (headerArray != null) {
            httpreq.headers(headerArray);
        }

        switch (req.getHttpMethod()) {
            case POST -> httpreq.POST(createBodyPublisher(req));
            case PUT -> httpreq.PUT(createBodyPublisher(req));
            case DELETE -> httpreq.DELETE();
            case GET -> httpreq.GET();
        }


        return httpreq.build();
    }

    private static HttpRequest.BodyPublisher createBodyPublisher(Request<?> req) {
        if (HeaderUtils.hasContentTypeHeader(req, "application/json")) {
            return HttpRequest.BodyPublishers.ofString(DELDObjectConverter.objectToJson(req.getBody()));
        } else if (HeaderUtils.hasContentTypeHeader(req, "application/octet-stream")) {
            if (req.getBody() instanceof InputStream) {
                return HttpRequest.BodyPublishers.ofInputStream(next((InputStream) req.getBody()));
            } else {
                return HttpRequest.BodyPublishers.ofByteArray((byte[]) req.getBody());
            }
        } else if (HeaderUtils.hasContentTypeHeader(req, "application/xml")) {
            return HttpRequest.BodyPublishers.ofString(DELDObjectConverter.objectToXml(req.getBody()));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }

    private static Supplier<InputStream> next(InputStream str) {
        Supplier<InputStream> s = () -> {
            try {
                return str;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
        return s;
    }
}

