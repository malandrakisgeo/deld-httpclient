package org.gmalandrakis.deld.core;

import org.gmalandrakis.deld.annotations.*;
import org.gmalandrakis.deld.exception.IncompatibleAnnotationsException;
import org.gmalandrakis.deld.exception.MultipleBodiesException;
import org.gmalandrakis.deld.logging.DELDLogger;
import org.gmalandrakis.deld.model.AsyncResponse;
import org.gmalandrakis.deld.model.Request;
import org.gmalandrakis.deld.model.ServiceProxyObject;
import org.gmalandrakis.deld.utils.HeaderUtils;
import org.gmalandrakis.deld.utils.QueryUtils;
import org.gmalandrakis.deld.utils.RequestUtils;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;

public class DELDBuilder {

    private HashMap<Class, ServiceProxyObject> interfaceProxyList = new HashMap<>();

    private DELDClient deldClientInstance;

    private DELDLogger logger;

    private Method errorCallbackMethod;  //TODO: add feature


    public DELDBuilder() {
        this.logger = new DELDLogger();
        this.deldClientInstance = new DELDClient();
    }

    public Object forService(Class service) {
        this.addService(service);
        return this.interfaceProxyList.get(service).getProxyObject();
    }


    public DELDBuilder addService(Class service) {
        if (!service.isInterface()) {
            throw new RuntimeException("Class " + service.getName() + " should be interface!");
        }
        if (interfaceProxyList.get(service) != null) {
            return this;
        }

        Arrays.stream(service.getMethods()).forEach(method -> {
            try {
                annotationChecks(method);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        var baseUrl = Arrays.stream(service.getAnnotations())
                .filter(annotation -> annotation instanceof BaseURL)
                .findAny();

        var obj = Proxy.newProxyInstance(service.getClassLoader(),
                new java.lang.Class[]{service},
                this.createInvocationHandler());

        ServiceProxyObject serviceProxyObject = new ServiceProxyObject();
        serviceProxyObject.setProxyObject(obj);
        if (baseUrl.isPresent() && !((BaseURL) baseUrl.get()).url().isBlank()) {
            serviceProxyObject.setBaseUrl(((BaseURL) baseUrl.get()).url());
        }

        interfaceProxyList.put(service, serviceProxyObject);

        return this;
    }

    protected void annotationChecks(Method method) throws Exception {

        var getAn = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof GET)
                .findAny();
        var postAn = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof POST)
                .findAny();
        var asyncAn = Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof Async)
                .findAny();


        if (getAn.isEmpty() && postAn.isEmpty()) {
            return;
        }

        if (getAn.isPresent() && postAn.isPresent()) {
            throw new IncompatibleAnnotationsException(method, getAn.get(), postAn.get());
        }

        if (asyncAn.isPresent() && !method.getReturnType().isInstance(new AsyncResponse<>())) {
            throw new Exception("TODO: Add a message"); //TODO
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
                if (method.isDefault()) {
                    return InvocationHandler.invokeDefault(proxy, method, args);
                }
                if (Modifier.isNative(method.getModifiers())) {
                 /*
                    TODO: Something about native methods.
                  */
                }
                var proxyInstance = interfaceProxyList.get(method.getDeclaringClass());


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
                    req.setUrl(getRequest.fullUrl().length() > 1 ? getRequest.fullUrl() : proxyInstance.getBaseUrl().concat(getRequest.url()));
                    req.setHttpMethod(Request.Method.GET);

                } else {
                    var postReq = (POST) postAn.get();
                    req.setUrl(postReq.fullUrl().length() > 1 ? postReq.fullUrl() : proxyInstance.getBaseUrl().concat(postReq.url()));
                    req.setHttpMethod(Request.Method.POST);
                }

                var request = RequestUtils.prepareHttpRequest(req);

                var response = deldClientInstance.sendRequest(request, retType);


                return response;
            }
        };


    }

    //https://stackoverflow.com/questions/1082850/java-reflection-create-an-implementing-class/9583681#9583681
}
