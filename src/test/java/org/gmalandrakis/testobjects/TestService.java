package org.gmalandrakis.testobjects;

import org.gmalandrakis.deld.annotations.Body;
import org.gmalandrakis.deld.annotations.DefaultHeader;
import org.gmalandrakis.deld.annotations.GET;
import org.gmalandrakis.deld.model.Response;

public interface TestService {

    @GET(fullUrl = "http://examplehost.com/")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> getUpdatedCustomer(@Body TestObject customer);

    @GET(fullUrl = "http://examplehost.com/")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> getUpdatedCustomer();

    default public boolean getTrue(){
        return true;
    }
}
