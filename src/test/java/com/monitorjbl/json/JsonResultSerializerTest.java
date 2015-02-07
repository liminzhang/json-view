package com.monitorjbl.json;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.monitorjbl.json.model.TestChildObject;
import com.monitorjbl.json.model.TestObject;
import com.monitorjbl.json.model.TestSubobject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class JsonResultSerializerTest {

  ObjectMapper sut;

  @Before
  public void setup() {
    sut = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(JsonResult.class, new JsonResultSerializer());
    sut.registerModule(module);
  }

  @Test
  public void testJsonIgnore() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoredDirect("ignore me");
    String serialized = sut.writeValueAsString(JsonResult.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("int1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNull(obj.get("ignoredDirect"));
  }

  @Test
  public void testJsonIgnoreProperties() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setIgnoreIndirect("ignore me");
    String serialized = sut.writeValueAsString(JsonResult.with(ref));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNotNull(obj.get("int1"));
    assertEquals(ref.getInt1(), obj.get("int1"));
    assertNull(obj.get("ignoreIndirect"));
  }

  @Test
  public void testBasicSerialization() throws IOException {
    TestObject ref = new TestObject();
    ref.setInt1(1);
    ref.setStr2("asdf");
    ref.setArray(new String[]{"apple", "banana"});
    ref.setList(Arrays.asList("red", "blue", "green"));
    ref.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));
    String serialized = sut.writeValueAsString(
        JsonResult.with(ref).onClass(TestObject.class, Match.on()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect")));

    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNull(obj.get("str2"));
    assertNotNull(obj.get("sub"));
    assertNull(((Map) obj.get("sub")).get("val"));
  }

  @Test
  public void testInheritance() throws IOException {
    TestChildObject ref = new TestChildObject();
    ref.setChildField("green");
    ref.setIgnoredDirect("ignore me");
    ref.setIgnoreIndirect("ignore me too");
    ref.setArray(new String[]{"pizza", "french fry"});

    String serialized = sut.writeValueAsString(
        JsonResult.with(ref).onClass(TestObject.class, Match.on()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect")));
    Map<String, Object> obj = sut.readValue(serialized, HashMap.class);
    assertNull(obj.get("ignoredIndirect"));
    assertNotNull(obj.get("ignoredDirect"));
    assertEquals(ref.getIgnoredDirect(), obj.get("ignoredDirect"));
    assertNotNull(obj.get("childField"));
    assertEquals(ref.getChildField(), obj.get("childField"));
  }

  @Test
  public void testList() throws IOException {
    TestObject ref1 = new TestObject();
    ref1.setInt1(1);
    ref1.setStr2("asdf");
    ref1.setArray(new String[]{"apple", "banana"});
    ref1.setList(Arrays.asList("red", "blue", "green"));
    ref1.setSub(new TestSubobject("qwerqwerqwerqw", new TestSubobject("poxcpvoxcv")));

    TestObject ref2 = new TestObject();
    ref2.setInt1(2);
    ref2.setStr2("asdf");
    ref2.setArray(new String[]{"orange", "kiwi"});
    ref2.setList(Arrays.asList("cyan", "indigo", "violet"));
    ref2.setSub(new TestSubobject("zxcvxzcv", new TestSubobject("hjhljkljh")));

    List<TestObject> refList = Arrays.asList(ref1, ref2);

    String serialized = sut.writeValueAsString(
        JsonResult.with(refList).onClass(TestObject.class, Match.on()
            .exclude("str2")
            .exclude("sub.val")
            .include("ignoredDirect")));
    List<Map<String, Object>> output = sut.readValue(serialized, ArrayList.class);

    assertEquals(refList.size(), output.size());
    for (int i = 0; i < output.size(); i++) {
      Map<String, Object> obj = output.get(i);
      TestObject ref = refList.get(i);

      assertEquals(ref.getInt1(), obj.get("int1"));
      assertNull(obj.get("str2"));
      assertNotNull(obj.get("sub"));
      assertNull(((Map) obj.get("sub")).get("val"));

      assertNotNull(obj.get("array"));
      assertTrue(obj.get("array") instanceof List);
      List array = (List) obj.get("array");
      assertEquals(ref.getArray().length, array.size());
      for (int j = 0; j < array.size(); j++) {
        assertEquals(ref.getArray()[j], array.get(j));
      }

      assertNotNull(obj.get("list"));
      assertTrue(obj.get("list") instanceof List);
      List list = (List) obj.get("list");
      assertEquals(ref.getList().size(), list.size());
      for (int j = 0; j < list.size(); j++) {
        assertEquals(ref.getList().get(j), list.get(j));
      }

    }

  }
}