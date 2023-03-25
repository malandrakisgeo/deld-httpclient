package org.gmalandrakis.deld.utils;

import org.gmalandrakis.deld.annotations.QueryParam;
import org.gmalandrakis.deld.model.Request;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class QueryUtils {

    public static void handleQueryParams(final Request<?> req, Method method, Object[] args) {
        if(method == null || req == null)
            return;

        var methodGeneralParams = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof QueryParam)
                .toList();
        req.setQueryParameters(QueryUtils.queryListToHashMap(methodGeneralParams));

        Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.getAnnotation(QueryParam.class) != null)
                .forEach(parameter -> {
                    var ann = parameter.getAnnotation(QueryParam.class);
                    if(ann != null){
                        int i = Arrays.stream(method.getParameters()).toList().indexOf(parameter);
                        req.getQueryParameters().put(ann.parameterName(), (String) args[i]); //TODO: What happens if no index is found?
                    }
                });
    }
    public static HashMap<String, String> queryListToHashMap(List<Annotation> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        HashMap<String, String> hashMap = new HashMap<>();

        list.forEach(annotation -> {
            QueryParam header = (QueryParam) annotation;
            hashMap.put(header.parameterName(), header.value());
        });

        return hashMap;
    }


    public static String[] hashMapToQueryParameters(HashMap<String, String> hashMap) {

        if (hashMap == null || hashMap.size() == 0) {
            return null;
        }
        List<String> queryList = new ArrayList<>();

        hashMap.forEach((s, s2) -> {
            queryList.add(s);
            queryList.add(s2);
        });

        return queryList.toArray(new String[hashMap.size() * 2]);
    }
}
