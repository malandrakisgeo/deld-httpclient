package org.gmalandrakis.deld.utils;

import org.gmalandrakis.deld.annotations.DefaultHeader;
import org.gmalandrakis.deld.model.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;
import java.util.*;

public class HeaderUtils {


    public static void fixHeaders(final Request<?> req, Method method) {
        if (method == null || req == null)
            return;

        var headers = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof DefaultHeader)
                .toList();

        if (headers.size() > 0) {
            req.getHeaders().putAll(HeaderUtils.headerListToHashMap(headers));
        }

    }

    public static HashMap<String, String> headerListToHashMap(List<Annotation> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap<>();

        list.forEach(annotation -> {
            DefaultHeader header = (DefaultHeader) annotation;
            hashMap.put(header.headerName(), header.value());
        });

        return hashMap;
    }


    public static String[] hashMapToHttpHeaders(HashMap<String, String> hashMap) {

        if (hashMap == null || hashMap.size() == 0) {
            return null;
        }
        List<String> headerlist = new ArrayList<>();

        hashMap.forEach((s, s2) -> {
            headerlist.add(s);
            headerlist.add(s2);
        });

        return headerlist.toArray(new String[hashMap.size() * 2]);
    }


    public static HashMap<String, String> httpHeadersToHashMap(String[] headers) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (headers == null || headers.length == 0) {
            return null;
        }

        for (int i = 0; i <= headers.length; i++) {
            hashMap.put(headers[i], headers[++i]);
        }

        return hashMap;
    }


    public static HashMap<String, String> httpHeadersToHashMap(Map<String, List<String>> headerMap) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (headerMap == null || headerMap.size() == 0) {
            return null;
        }

        headerMap.forEach((map, list) -> {
            if(list.size() > 1){
                final String[] str = {new String()};
                str[0] = str[0].concat(list.get(0));
                list.forEach(partialHeader->{
                    str[0] = str[0].concat(",").concat(partialHeader);  //in accordance to RFC 2616
                });
                hashMap.put(map, str[0]);
            }else{
                hashMap.put(map, list.get(0));
            }
        });

        return hashMap;
    }

    public static boolean acceptHeader(HttpRequest req, String type) {
        if (req == null || req.headers() == null) {
            return false;
        }
        return (req.headers().firstValue("Accept").isPresent() && req.headers().firstValue("Accept").get().equalsIgnoreCase(type))
                || (req.headers().firstValue("accept").isPresent() && req.headers().firstValue("accept").get().equalsIgnoreCase(type));
    }

    public static String accepts(HttpRequest req) {
        if (req == null || req.headers() == null) {
            return null;
        }
        if (req.headers().firstValue("Accept").isPresent()) {
            return req.headers().firstValue("Accept").get();
        } else if (req.headers().firstValue("accept").isPresent()) {
            return req.headers().firstValue("accept").get();
        } else {
            return null;
        }

    }

    public static boolean hasContentTypeHeader(HttpRequest req, String type) {
        if (req == null || req.headers() == null) {
            return false;
        }
        return (req.headers().firstValue("content-type").isPresent() && req.headers().firstValue("content-type").get().equalsIgnoreCase(type))
                || (req.headers().firstValue("Content-Type").isPresent() && req.headers().firstValue("Content-Type").get().equalsIgnoreCase(type));

    }

    public static boolean acceptHeader(Request<?> req, String type) {
        if (req == null || req.getHeaders() == null) {
            return false;
        }
        return (req.getHeaders().get("Accept") != null && req.getHeaders().get("Accept").equalsIgnoreCase(type));
    }

    public static boolean hasContentTypeHeader(Request<?> req, String type) {
        if (req == null || req.getHeaders() == null) {
            return false;
        }
        return (req.getHeaders().get("Content-Type") != null && req.getHeaders().get("Content-Type").equalsIgnoreCase(type));
    }

}
