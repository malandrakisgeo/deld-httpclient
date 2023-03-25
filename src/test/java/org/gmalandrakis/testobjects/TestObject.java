package org.gmalandrakis.testobjects;

import lombok.Data;
import lombok.Generated;
import org.gmalandrakis.deld.model.Response;

import java.io.InputStream;

@Data
@Generated
public class TestObject {
    private String field1;
    private String field2;

    public Response<InputStream> testMethod() {
        this.setField1("test");
        return null;
    }
}
