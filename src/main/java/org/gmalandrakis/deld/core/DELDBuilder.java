package org.gmalandrakis.deld.core;

import org.gmalandrakis.deld.annotations.*;
import org.gmalandrakis.deld.exception.IncompatibleAnnotationsException;
import org.gmalandrakis.deld.exception.MultipleBodiesException;
import org.gmalandrakis.deld.logging.DELDLogger;
import org.gmalandrakis.deld.model.AsyncResponse;
import org.gmalandrakis.deld.model.Request;
import org.gmalandrakis.deld.model.Response;
import org.gmalandrakis.deld.model.ServiceProxyObject;
import org.gmalandrakis.deld.utils.HeaderUtils;
import org.gmalandrakis.deld.utils.QueryUtils;
import org.gmalandrakis.deld.utils.RequestUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class DELDBuilder {

    private HashMap<Class, ServiceProxyObject> interfaceProxyList = new HashMap<>();

    private DELDClient deldClientInstance;

    private DELDLogger logger;



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
            throw new RuntimeException("The class " + service.getName() + " should be an interface!");
        }
        if (interfaceProxyList.get(service) != null) {
            return this;
        }
        var fnd = Arrays.stream(service.getAnnotations()).filter(annotation -> annotation instanceof Async).findAny();

        Arrays.stream(service.getMethods()).forEach(method -> {
            boolean isAsync = false; //effectively final parameter needed in lambda expression
            if (fnd.isPresent()) {
                isAsync = true;
            }
            try {
                annotationChecks(method, isAsync);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        var obj = Proxy.newProxyInstance(service.getClassLoader(),
                new java.lang.Class[]{service},
                this.createInvocationHandler());

        ServiceProxyObject serviceProxyObject = new ServiceProxyObject();
        serviceProxyObject.setProxyObject(obj);
        var baseUrl = Arrays.stream(service.getAnnotations())
                .filter(annotation -> annotation instanceof BaseURL)
                .findAny();

        if (baseUrl.isPresent() && !((BaseURL) baseUrl.get()).url().isBlank()) {
            serviceProxyObject.setBaseUrl(((BaseURL) baseUrl.get()).url());
        }
        interfaceProxyList.put(service, serviceProxyObject);

        return this;
    }

    protected void annotationChecks(Method method, boolean asyncService) throws Exception {
        Supplier<Stream<Annotation>> streamSupplier =
                () -> Stream.of(method.getAnnotations());
        var st = streamSupplier.get();

        var an = streamSupplier.get()
                .filter(annotation -> annotation instanceof POST || annotation instanceof GET
                        || annotation instanceof PUT || annotation instanceof DELETE).toList();

        if (an.isEmpty()) {
            return;
        }

        if (an.size() > 1) {
            throw new IncompatibleAnnotationsException(method);
        }

        boolean asyncMethod = streamSupplier.get().anyMatch(annotation -> annotation instanceof Async);
        boolean syncMethod = streamSupplier.get().anyMatch(annotation -> annotation instanceof Sync);

        //error if both @Sync and @Async are present in the same function
        if(asyncMethod && syncMethod){
            throw new IncompatibleAnnotationsException(method, streamSupplier.get().filter(annotation -> annotation instanceof Async).findFirst().get(), streamSupplier.get().filter(annotation -> annotation instanceof Sync).findFirst().get());
        }

        if (!method.getReturnType().isInstance(new AsyncResponse<>()) &&( (asyncService && !syncMethod) || asyncMethod)) {
            throw new Exception("Error on " + method.getName() + ". Async methods must return AsyncResponse<?>! ");
        }

        if (syncMethod && !method.getReturnType().isInstance(new Response<>())) {
            throw new Exception("Error on " + method.getName() + ". Sync methods must return Response<?>! ");
        }

        var parameterAnnotationList = Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Body.class))
                .toList();

        if (parameterAnnotationList.size() > 1) {
            throw new MultipleBodiesException(method);
        }

        if (an.get(0) instanceof POST) {
            var contentType = streamSupplier.get()
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
                var serviceAnnotationsStream = Arrays.stream(method.getDeclaringClass().getAnnotations());

                Supplier<Stream<Annotation>> methodAnnotationsStreamSupplier =
                        () -> Stream.of(method.getAnnotations());

                var asyncAn = Stream.concat(methodAnnotationsStreamSupplier.get(), serviceAnnotationsStream)
                        .filter(annotation -> annotation instanceof Async)
                        .findAny();

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
                methodAnnotationsStreamSupplier.get().forEach(annotation -> {
                    if (annotation instanceof DELETE) {
                        var ann = (DELETE) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(Request.Method.DELETE);
                    } else if (annotation instanceof PUT) {
                        var ann = (PUT) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(Request.Method.PUT);
                    } else if (annotation instanceof POST) {
                        var ann = (POST) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(Request.Method.POST);
                    } else if (annotation instanceof GET) {
                        var ann = (GET) annotation;
                        req.setUrl(ann.fullUrl().length() > 1 ? ann.fullUrl() : proxyInstance.getBaseUrl().concat(ann.url()));
                        req.setHttpMethod(Request.Method.GET);
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

                if (asyncAn.isPresent()) {
                    return deldClientInstance.handleAsync(request, retType);
                }
                return deldClientInstance.handleSync(request, retType);
            }
        };
    }

    //https://stackoverflow.com/questions/1082850/java-reflection-create-an-implementing-class/9583681#9583681
}
