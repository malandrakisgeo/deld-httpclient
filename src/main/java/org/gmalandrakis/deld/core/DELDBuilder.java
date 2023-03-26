package org.gmalandrakis.deld.core;

import org.gmalandrakis.deld.annotations.*;
import org.gmalandrakis.deld.exception.IncompatibleAnnotationsException;
import org.gmalandrakis.deld.exception.MultipleBodiesException;
import org.gmalandrakis.deld.logging.DELDLogger;
import org.gmalandrakis.deld.model.Request;
import org.gmalandrakis.deld.utils.HeaderUtils;
import org.gmalandrakis.deld.utils.QueryUtils;
import org.gmalandrakis.deld.utils.RequestUtils;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DELDBuilder {

    private String baseURL;

    private HashMap<Class, Object> interfaceProxyList = new HashMap<>();


    private DELDClient deldClientInstance;

    private DELDLogger logger;

    private boolean debug = false;

    private Method errorCallbackMethod;  //TODO: add feature


    public DELDBuilder() {
        this.logger = new DELDLogger();
    }

    public Object forService(Class service){
        this.addService(service);
        this.build();
        return this.interfaceProxyList.get(service);
    }

    public DELDBuilder debugMode(){
        this.debug = true;
        return this;
    }

    /*public void errorCallbackMethod(Method errorHandler){
        this.errorCallbackMethod = errorHandler;
    }*/

    public DELDBuilder addService(Class service) {
        this.logger = new DELDLogger();

        if (!service.isInterface()) {
            throw new RuntimeException("Class " + service.getName() + " should be interface!");
        }

        Arrays.stream(service.getMethods()).forEach(method -> {
            try {
                annotationFinder(method);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        var obj = Proxy.newProxyInstance(service.getClassLoader(),
                new java.lang.Class[]{service},
                this.createInvocationHandler()); //TODO: Make sure that the invocationHandler is called only on unimplemented methods (not e.g. hashCode)

        interfaceProxyList.put(service, obj);

        return this;
    }

    public DELDClient build() {
        if(deldClientInstance == null){
            DELDClient deldClient = new DELDClient(baseURL, interfaceProxyList);
            this.deldClientInstance = deldClient;
        }
        return this.deldClientInstance;
    }

    public DELDBuilder setBaseURL(String str) {
        this.baseURL = str;
        return this;
    }

    protected void annotationFinder(Method method) throws Exception {

        var getAn = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof GET)
                .findAny();
        var postAn = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof POST)
                .findAny();

        if (getAn.isEmpty() && postAn.isEmpty()) {
            return;
        }

        if (getAn.isPresent() && postAn.isPresent()) {
            throw new IncompatibleAnnotationsException(method, getAn.get(), postAn.get());
        }

        var parameterAnnotationList = Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Body.class))
                .toList();

        if (parameterAnnotationList.size() > 1) {
            throw new MultipleBodiesException(method);
        }

        if (postAn.isPresent()) {
            var contentType = Arrays.stream(method.getAnnotations())
                    .filter(annotation -> annotation instanceof DefaultHeader)
                    .filter((header -> {
                        DefaultHeader h = (DefaultHeader) header;
                        return h.headerName().equalsIgnoreCase("content-type"); //TODO: Set an enum list of acceptable headers.
                    })).findAny();

            if (!contentType.isPresent()) {
                this.logger.printWarning("No Content-Type header set for POST request on method " + method.getName() + " application/json will be assumed");
            }
        }

    }

    private InvocationHandler createInvocationHandler() {

        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                var getAn = Arrays.stream(method.getAnnotations())
                        .filter(annotation -> annotation instanceof GET)
                        .findAny();

                var postAn = Arrays.stream(method.getAnnotations())
                        .filter(annotation -> annotation instanceof POST)
                        .findAny();

                if (getAn.isEmpty() && postAn.isEmpty()) {
                    return null;
                }

                var bodyArgument = Arrays.stream(method.getParameters())
                        .filter(parameter -> parameter.getAnnotation(Body.class) != null)
                        .findFirst(); //at most one

                Request<?> req;

                if (bodyArgument.isPresent()) {
                    int i = Arrays.stream(method.getParameters()).toList().indexOf(bodyArgument.get());
                    req = new Request<>(args[i]);
                } else {
                    req = new Request<>();
                }

                HeaderUtils.fixHeaders(req, method);
                QueryUtils.handleQueryParams(req, method, args);

                /*
                    Suppose we have a returnType like Response<SomeObject>.
                    We only want to parse the SomeObject-classtype as argument to the sendGetRequest method.
                    Hence the code below.

                    TODO: Find a more straightforward way to achieve this.

                    var retType = Class.forName(Arrays.stream(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()).toList().get(0).getTypeName());
                  */

                var retType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];

                if (getAn.isPresent()) {
                    var getRequest = (GET) getAn.get();
                    req.setUrl(getRequest.fullUrl().length() > 1 ? getRequest.fullUrl() : deldClientInstance.getBaseURL().concat(getRequest.url()));
                    req.setHttpMethod(Request.Method.GET);

                } else {
                    var postReq = (POST) postAn.get();
                    req.setUrl(postReq.fullUrl().length() > 1 ? postReq.fullUrl() : deldClientInstance.getBaseURL().concat(postReq.url()));
                    req.setHttpMethod(Request.Method.POST);

                }

                var request = RequestUtils.prepareHttpRequest(req);

                var response = deldClientInstance.sendRequest(request, retType);

                if(debug){
                    response.setAssociatedRequestId(req.getRequestId());
                    //TODO: Add a debug feature.
                }

                return response;
            }
        };


    }

    //https://stackoverflow.com/questions/1082850/java-reflection-create-an-implementing-class/9583681#9583681
}
