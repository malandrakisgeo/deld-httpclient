package org.gmalandrakis.deld.core;

import org.gmalandrakis.deld.annotations.*;
import org.gmalandrakis.deld.exception.IncompatibleAnnotationsException;
import org.gmalandrakis.deld.exception.MultipleBodiesException;
import org.gmalandrakis.deld.logging.DELDLogger;
import org.gmalandrakis.deld.model.*;
import org.gmalandrakis.deld.utils.HeaderUtils;
import org.gmalandrakis.deld.utils.QueryUtils;
import org.gmalandrakis.deld.utils.RequestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

public class DELDBuilder {

    private final HashMap<Class<?>, ServiceProxyObject<?>> interfaceProxyList = new HashMap<>();

    private final DELDClient deldClientInstance;

    private final DELDLogger logger;


    public DELDBuilder() {
        this.logger = new DELDLogger();
        this.deldClientInstance = new DELDClient();
    }

    public DELDBuilder(int threadsNeeded) {
        this.logger = new DELDLogger();
        this.deldClientInstance = new DELDClient(threadsNeeded);
    }

    public <T> T createService(Class<T> service) {
        this.addServiceInternal(service);
        return (T) this.interfaceProxyList.get(service).getProxyObject();
    }

    private void addServiceInternal(Class<?> service) {
        if (!service.isInterface()) {
            throw new RuntimeException("The class " + service.getName() + " should be an interface!");
        }
        if (interfaceProxyList.get(service) != null) {
            return;
        }
        var asyncan = service.getAnnotation(Async.class);
        Arrays.stream(service.getMethods()).forEach(method -> {
            boolean inAsyncClass = false; //effectively final parameter needed in lambda expression
            if (asyncan != null) {
                inAsyncClass = true;
            }
            try {
                annotationChecks(method, inAsyncClass);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        var obj = Proxy.newProxyInstance(service.getClassLoader(),
                new java.lang.Class[]{service},
                this.createInvocationHandler());

        ServiceProxyObject serviceProxyObject = new ServiceProxyObject();
        serviceProxyObject.setProxyObject(obj);

        var baseUrlannotation = service.getAnnotation(BaseURL.class);
        if (baseUrlannotation != null && !(baseUrlannotation).url().isBlank()) {
            serviceProxyObject.setBaseUrl(((BaseURL) baseUrlannotation).url());
        }
        interfaceProxyList.put(service, serviceProxyObject);

    }

    protected void annotationChecks(Method method, boolean asyncService) throws Exception {
        var annotationStream = Stream.of(method.getAnnotations());
        var an = annotationStream
                .filter(annotation -> annotation instanceof POST || annotation instanceof GET
                        || annotation instanceof PUT || annotation instanceof DELETE).toList();

        if (an.isEmpty()) {
            return;
        }

        if (an.size() > 1) {
            throw new IncompatibleAnnotationsException(method);
        }

        var asyncMethod = method.getAnnotation(Async.class);
        var syncMethod = method.getAnnotation(Sync.class);

        //error if both @Sync and @Async are present in the same function
        if (asyncMethod != null && syncMethod != null) {
            throw new IncompatibleAnnotationsException(method, asyncMethod, syncMethod);
        }

        if (!method.getReturnType().isInstance(new AsyncResponse<>()) && ((asyncService && syncMethod != null) || asyncMethod != null)) {
            throw new Exception("Error on " + method.getName() + ". Async methods must return AsyncResponse<?>! ");
        }

        if (syncMethod != null && !method.getReturnType().isInstance(new Response<>())) {
            throw new Exception("Error on " + method.getName() + ". Sync methods must return Response<?>! ");
        }
        var parameterAnnotationList = Stream.of(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Body.class))
                .toList();

        if (parameterAnnotationList.size() > 1) {
            throw new MultipleBodiesException(method);
        }

        if (an.get(0) instanceof POST) {
            var contentType = Stream.of(method.getAnnotations())
                    .filter(annotation -> annotation instanceof DefaultHeader)
                    .filter((header -> {
                        DefaultHeader h = (DefaultHeader) header;
                        return h.headerName().equalsIgnoreCase("content-type"); //TODO: Set an enum list of acceptable headers.
                    })).findAny();

            if (contentType.isEmpty()) {
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
               /* if (Modifier.isNative(method.getModifiers())) {
                    TODO: Something about native methods.
                } */

                var proxyInstance = interfaceProxyList.get(method.getDeclaringClass());
                //  var serviceAnnotationsStream = Set.of(method.getDeclaringClass().getAnnotations());

                Set<Annotation> methodAnnotations =
                        Set.of(method.getAnnotations());

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

                //TODO: Simplify
                methodAnnotations.forEach(annotation -> {
                    if (annotation instanceof DELETE) {
                        var ann = (DELETE) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(HttpMethod.DELETE);
                    } else if (annotation instanceof PUT) {
                        var ann = (PUT) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(HttpMethod.PUT);
                    } else if (annotation instanceof POST) {
                        var ann = (POST) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(HttpMethod.POST);
                    } else if (annotation instanceof GET) {
                        var ann = (GET) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(HttpMethod.GET);
                    }
                });

                if (req.getHttpMethod() == null || req.getHttpMethod().toString().isBlank()) {
                    return null;
                }

                HeaderUtils.fixHeaders(req, method);
                QueryUtils.handleQueryParams(req, method, args);

                /*
                    Suppose we have a returnType like Response<SomeObject>.
                    We only want to parse the SomeObject-classtype as argument to the sendGetRequest method.
                    Hence the code below.
                    var retType = Class.forName(Arrays.stream(((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()).toList().get(0).getTypeName());*/

                var retType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];

                var request = RequestUtils.prepareHttpRequest(req);

                if (isAsync(method)) {
                    return deldClientInstance.handleAsync(request, retType);
                }
                return deldClientInstance.handleSync(request, retType);
            }
        };
    }

    private boolean isAsync(Method method) {
        var asyncMethod = method.getAnnotation(Async.class);

        if (asyncMethod != null) {
            return true;
        }
        var asyncClass = method.getDeclaringClass().getAnnotation(Async.class);
        var syncMeth = method.getAnnotation(Sync.class);

        return (asyncClass != null && syncMeth == null);
    }

}
