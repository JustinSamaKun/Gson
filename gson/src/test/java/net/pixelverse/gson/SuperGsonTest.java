package net.pixelverse.gson;

import com.google.gson.JsonObject;
import junit.framework.TestCase;
import net.pixelverse.gson.common.TestTypes;

import java.util.*;

public class SuperGsonTest extends TestCase {

    private SuperGson gson = new SuperGson();

    public void testDeserializeOutOfOrder() {
        String target = "Hello";
        JsonObject json = new JsonObject();
        json.addProperty("data", target);
        json.addProperty("type", target.getClass().getName());
        System.out.println(json);
        String out = gson.fromJson(json.toString());
        assertEquals(target, out);
    }

    public void testObject() {
        TestTypes.ComplexClass complexClass = new TestTypes.ComplexClass();
        doSerializationTest(complexClass);
    }

    public void testMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("list", new ArrayList<String>(Arrays.asList("hello")));
        doSerializationTest(map);
    }

    public void testList() {
        List<TestTypes.ComplexClass> objects = new ArrayList<TestTypes.ComplexClass>(Arrays.asList(new TestTypes.ComplexClass(), new TestTypes.ComplexClass()));
        doSerializationTest(objects);
    }

    private <T> void doSerializationTest(T object) {
        String json = gson.toJson(object);
        System.out.println(json);
        T out = gson.fromJson(json);
        assertEquals(object, out);
    }
}
