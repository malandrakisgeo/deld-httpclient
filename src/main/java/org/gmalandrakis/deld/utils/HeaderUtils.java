package org.gmalandrakis.deld.utils;

import org.gmalandrakis.deld.annotations.DefaultHeader;
import org.gmalandrakis.deld.model.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.http.HttpRequest;
import java.util.*;

public class HeaderUtils {


    public static void fixHeaders(final Request<?> req, Method method) {
        if(method == null || req == null)
            return;

        var headers = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof DefaultHeader)
                .toList();

        if(headers.size() > 0){
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


    public static HashMap<String, String>   httpHeadersToHasgMap(String[] headers) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (headers == null || headers.length == 0) {
            return null;
        }

        for(int i=0; i<=headers.length; i++){
            hashMap.put(headers[i], headers[++i]);
        }

        return hashMap;
    }


    public static HashMap<String, String>   httpHeadersToHasgMap(Map<String, List<String>> headerMap) {
        HashMap<String, String> hashMap = new HashMap<>();
        if (headerMap == null || headerMap.size() == 0) {
            return null;
        }

        headerMap.forEach((map, list) ->{
            list.forEach(value -> hashMap.put(map,value));

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
