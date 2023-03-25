package org.gmalandrakis.deld.utils;

import com.google.gson.Gson;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.StringReader;
import java.io.StringWriter;

public class DELDObjectConverter {


    public static Object objectConverterJson(String json, Class<?> tClass) {

        return new Gson().fromJson(json, tClass);
    }

    public static String objectToJson(Object obj) {
        return new Gson().toJson(obj);
    }

    public static String objectToXml(Object obj) {
        StringWriter sw = new StringWriter();

        JAXB.marshal(obj, sw);
        return sw.toString();
    }

    public static Object objectConverterXml(String xml, Class<?> tClass) throws Exception {
        StringReader stringReader = new StringReader(xml);
        JAXBContext jaxbContext = JAXBContext.newInstance(tClass);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return jaxbUnmarshaller.unmarshal(stringReader);
    }

}
