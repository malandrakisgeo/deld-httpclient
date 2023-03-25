package org.gmalandrakis.testobjects;

import org.gmalandrakis.deld.annotations.Body;
import org.gmalandrakis.deld.annotations.DefaultHeader;
import org.gmalandrakis.deld.annotations.GET;
import org.gmalandrakis.deld.model.Response;

public interface TestService {
    @GET(fullUrl = "localhost:8080")
    public boolean getTrue();

    @GET(fullUrl = "localhost:8080")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> getUpdatedCustomer(@Body TestObject customer);

    @GET(fullUrl = "localhost:8080")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> getUpdatedCustomer();
}
