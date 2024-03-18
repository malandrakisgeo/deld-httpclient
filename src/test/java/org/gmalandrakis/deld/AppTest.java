package org.gmalandrakis.deld;

import org.gmalandrakis.deld.annotations.Async;
import org.gmalandrakis.deld.annotations.Sync;
import org.gmalandrakis.deld.core.DELDBuilder;
import org.gmalandrakis.deld.model.CaseInsensitiveHashMap;
import org.gmalandrakis.deld.model.HttpMethod;
import org.gmalandrakis.deld.model.Request;
import org.gmalandrakis.deld.utils.DELDObjectConverter;
import org.gmalandrakis.testobjects.TestObject;
import org.gmalandrakis.testobjects.TestService;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;


/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("test", "ole");
        var ff = hashMap.entrySet();
        var ffff = ff.toArray();
        ffff.clone();

        String[] arr = new String[2];
        arr[0] = "Accept";
        arr[1] = "*/*";


        var request = HttpRequest.newBuilder(
                        URI.create("http://google.com"))
                .headers(arr)
                .build();

        var hd = request.headers();
        request.hashCode();

        //    int length = headerArray.length;

    }



    @Test
    public void debugToCheckTypes() throws Exception {

        var meth = TestService.class.getDeclaredMethod("getUpdatedCustomerAsync");
        var type = meth.getGenericReturnType();
        var asyncMethod = meth.getAnnotation(Async.class);
        var syncMethod = meth.getAnnotation(Sync.class);

        ParameterizedType f = (ParameterizedType) meth.getGenericReturnType();
        var rawType = f.getRawType();

        Arrays.stream(((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()).toList().get(0).getTypeName();

        var typeName = meth.getGenericReturnType().getTypeName();
        var retType = Class.forName(Arrays.stream(((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()).toList().get(0).getTypeName());

        retType.hashCode();
    }

    @Test
    public void testMuth() throws Exception {
        TestObject testObject = new TestObject();
        testObject.setField1("field");
        String str = DELDObjectConverter.objectToJson(testObject);
        str.hashCode();
        // DELDLogger.printWarning("No Content-Type header set for POST request. application/json assumed");

        String str2 = DELDObjectConverter.objectToXml(testObject);
        str2.hashCode();

    }

    @Test
    public void testytest() throws Exception {
        Request<InputStream> req = new Request<InputStream>();
        InputStream a = new DataInputStream(new FileInputStream("src/test/resources/sample.txt"));
        req.setBody(a);
        req.getHeaders().put("Content-Type", "application/octet-stream");
        req.setUrl("https://www.baeldung.com/convert-file-to-input-stream");
        req.setHttpMethod(HttpMethod.POST);

        // prepareHttpRequest(req);
    }


    @Test
    public void caseInsensiviteTest() throws Exception {
        CaseInsensitiveHashMap queryParameters = new CaseInsensitiveHashMap();
        queryParameters.put("StRinG1", "StrInG2");
        var a = queryParameters.get("string1");
        assert (Objects.equals(a, "StrInG2"));


        // deldClient.prepareHttpRequest(req);
    }

    @Test
    public void invocationhandlerTest() {
        var test = (TestService) new DELDBuilder().createService(TestService.class);
        var as = test.getUpdatedCustomerAsync();
        while (!as.isDone()) {
            System.out.println(as.hashCode());
        }
        test.getTrue();

    }


}
