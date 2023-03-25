package org.gmalandrakis.deld;

import org.gmalandrakis.deld.core.DELDClient;
import org.gmalandrakis.deld.logging.DELDLogger;
import org.gmalandrakis.deld.model.Request;
import org.gmalandrakis.deld.model.Response;
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
    public void shouldRunWithoutProblem() {

        try {
            TestObject t = new TestObject();
            Object o = new Object();
            var ppa = t.getClass().cast(o);

            ppa.hashCode();
            this.testMeth(TestObject.class);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void checkShit() {

        try {
            TestObject t = new TestObject();
            t.getClass().getMethods();
            var bb = Arrays.stream(t.getClass().getMethods()).toList();
            var aaa = Arrays.stream(t.getClass().getMethods()).toList().get(0).getReturnType();
            aaa.getName();

            var dd = Arrays.stream(t.getClass().getDeclaredMethods()).toList();

            var aada = t.getClass().getDeclaredMethod("testMethod");


            var bool = aada.getReturnType().isInstance(InputStream.nullInputStream());


            aada.hashCode();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void checkServgice() throws Exception {

        var meth = TestService.class.getDeclaredMethod("getUpdatedCustomer");
        var bb = meth.getGenericReturnType();
        bb.hashCode();

        ParameterizedType f = (ParameterizedType) meth.getGenericReturnType();
        var cc = f.getRawType();

        Arrays.stream(((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()).toList().get(0).getTypeName();

        var dd = meth.getGenericReturnType().getTypeName();
        dd.hashCode();
        var retType = Class.forName(Arrays.stream(((ParameterizedType) meth.getGenericReturnType()).getActualTypeArguments()).toList().get(0).getTypeName());

        retType.hashCode();
    }

    private void testMeth(Class<?> returnType) throws Exception {
        Response<?> resp = new Response(returnType.getConstructor().newInstance());
        resp.hashCode();

        var bb = DELDObjectConverter.objectConverterJson("{\"field1\": \"fddf\"\n" +
                "}", resp.getBody().getClass());
        bb.hashCode();

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
        req.setHttpMethod(Request.Method.POST);
        DELDClient deldClient = new DELDClient(null, null);

       // deldClient.prepareHttpRequest(req);
    }
}
