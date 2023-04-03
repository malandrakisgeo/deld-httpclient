package org.gmalandrakis.testobjects;

import org.gmalandrakis.deld.annotations.Async;
import org.gmalandrakis.deld.annotations.Body;
import org.gmalandrakis.deld.annotations.DefaultHeader;
import org.gmalandrakis.deld.annotations.GET;
import org.gmalandrakis.deld.model.AsyncResponse;
import org.gmalandrakis.deld.model.Response;

public interface TestService {

    @GET(fullUrl = "http://examplehost.com/")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> getUpdatedCustomer(@Body TestObject customer);

    @GET(fullUrl = "http://examplehost.com/")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> getUpdatedCustomer();

    @Async
    @GET(fullUrl = "http://examplehost.com/")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public AsyncResponse<TestObject> getUpdatedCustomerAsync();


    default public boolean getTrue(){
        return true;
    }
}
