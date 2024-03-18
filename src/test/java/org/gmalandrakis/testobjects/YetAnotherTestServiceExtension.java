package org.gmalandrakis.testobjects;

import org.gmalandrakis.deld.annotations.Body;
import org.gmalandrakis.deld.annotations.DefaultHeader;
import org.gmalandrakis.deld.annotations.GET;
import org.gmalandrakis.deld.model.Response;
import org.junit.Test;

public interface YetAnotherTestServiceExtension  extends TestService {

    @GET(fullUrl = "http://examplehost.com/")
    @DefaultHeader(headerName = "Accept", value = "application/json")
    public Response<TestObject> extraMethod(@Body TestObject customer);
}
