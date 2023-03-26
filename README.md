# DELD-httpclient

An experimental Java 17 library enabling you to store and retrieve data from/to RESTful APIs using only 
annotations and unimplemented interfaces.

The motivation for this project was getting a deeper knowledge and comprehension of how tools such as Feign and Retrofit work, 
as well as the java.net.* package and its' capabilities. 


## Installation
Just clone the repo and run "mvn clean install"!



## Usage
So far tested with Spring Boot and Quarkus projects. 

Suppose you want your application to store and retrieve data from a RESTful API on http://examplehost.com.

All you need to do is create an interface wherein each function corresponds to an endpoint. 
The library provides annotations corresponding to HTTP Methods (e.g. @GET), Headers and QueryParameters
(@DefaultHeader and @QueryParam respectively), request body (@Body), and Authentication (@Authentication). 
An interface utilizing them would look like this: 

```java
public interface ExampleService {
    @GET(fullUrl = "http://examplehost.com/endpoint1") 
    @DefaultHeader(headerName = "Accept", value = "application/json")
    @QueryParam(parameterName = "includeData", value = "all")
    public Response<MyObject> getData(@QueryParam(parameterName = "excludeData") String toBeExcluded);

    @GET(url = "endpoint2") //When using "http://examplehost.com/" as baseURL
    @DefaultHeader(headerName = "Accept", value = "application/octet-stream")  
    public Response<InputStream> getFile(); 

    @POST(url = "endpoint3")
    @DefaultHeader(headerName = "Content-Type", value = "application/xml")
    public Response<Object> postJson(@Body MyObject myObject); 
}
```

The only code necessary afterwards is a Procuder method for the Interface as a bean, e.g.

```java
public class ProducerClass {
    @Produces //javax.enterprise.inject.Produces
    ExampleService producerExample (){
        return (ExampleService)  new DELDBuilder().setBaseURL("http://examplehost.com/").forService(ExampleService.class);
    }
}

```
Or, if using Spring Boot: 

```java
@Configuration
public class ProducerClass {
    @Bean //org.springframework.context.annotation.Bean
    public TestService testServiceBean() {
        return (TestService)  new DELDBuilder().setBaseURL("http://examplehost.com/").forService(TestService.class);
    }
}
```

The Service can be injected on other components using @Inject (or @Autowired).

DELD takes care of implementing the Service-interface.

Unless explicitly specified, all "Content-Type" and "Accept" headers are assumed to be "application/json". 
For the time being, it only works with "application/json", "application/xml", and "application/octet-stream", 
with the former allowing all objects that can be converted from/to json (xml respectively) as in the Response type, 
while octet-stream must be used with Response<InputStream>  or Response<byte[]> . 


## Future plans
As of 3/2023, there is no intention of turning DELD-Httpclient to a fully-operational framework
comparable to Feign or Spring's WebClient. 

However, there is the intention of making it support Asynchronous communication.


## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

[MIT](https://choosealicense.com/licenses/mit/)