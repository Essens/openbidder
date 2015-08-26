package com.google.openbidder.util;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Tests for {@link ReflectionUtils}.
 */
public class ReflectionUtilsTest {
  private static final Object anonObj = new Object() {};
  private static final class NestedKlass {}
  private static final NestedKlass nestedObj = new NestedKlass();

  @Test
  public void testSimpleName() {
    assertEquals("ReflectionUtilsTest$1", ReflectionUtils.TO_SIMPLECLASSNAME.apply(anonObj));
    assertEquals("NestedKlass", ReflectionUtils.TO_SIMPLECLASSNAME.apply(nestedObj));
    assertEquals("String", ReflectionUtils.TO_SIMPLECLASSNAME.apply("x"));
    assertEquals("int[][]", ReflectionUtils.TO_SIMPLECLASSNAME.apply(new int[0][0]));
  }

  @Test
  public void testClassName() {
    assertEquals(
        getClass().getPackage().getName() + ".ReflectionUtilsTest$1",
        ReflectionUtils.TO_CLASSNAME.apply(anonObj));
    assertEquals(
        getClass().getPackage().getName() + ".ReflectionUtilsTest$NestedKlass",
        ReflectionUtils.TO_CLASSNAME.apply(nestedObj));
    assertEquals("java.lang.String", ReflectionUtils.TO_CLASSNAME.apply("x"));
    assertEquals("[[I", ReflectionUtils.TO_CLASSNAME.apply(new int[0][0]));
  }

  @Test
  public void testPrettyClassName() {
    assertEquals(
        getClass().getPackage().getName() + ".ReflectionUtilsTest$1",
        ReflectionUtils.TO_PRETTYCLASSNAME.apply(anonObj));
    assertEquals(
        getClass().getPackage().getName() + ".ReflectionUtilsTest.NestedKlass",
        ReflectionUtils.TO_PRETTYCLASSNAME.apply(nestedObj));
    assertEquals("java.lang.String", ReflectionUtils.TO_PRETTYCLASSNAME.apply("x"));
    assertEquals("int[][]", ReflectionUtils.TO_PRETTYCLASSNAME.apply(new int[0][0]));
  }

  @Test
  public void testInvoke()
      throws SecurityException, InvocationTargetException, NoSuchMethodException {
    assertEquals(3, ReflectionUtils.invoke(String.class.getMethod("length"), "xyz"));
  }

  @Test(expected = IllegalStateException.class)
  public void testInvoke_illegalState()
      throws SecurityException, InvocationTargetException, NoSuchMethodException {
    assertEquals(3, ReflectionUtils.invoke(String.class.getMethod("length"), 10));
  }

  @Test
  public void testForceAccessible()
      throws SecurityException, NoSuchMethodException {
    ReflectionUtils.forceAccessible(String.class.getMethod("length"));
    ReflectionUtils.forceAccessible(ReflectionUtilsTest.class.getDeclaredMethod("privateMethod"));
  }

  @SuppressWarnings("unused")
  private void privateMethod() {
  }

  static class TestPostPre {
    @PostConstruct public void postConstruct() {
    }

    @PreDestroy public void preDestroy() {
    }
  }

  @Test
  public void testPostConstructPreDestroy() {
    List<?> objs = singletonList(new TestPostPre());
    ReflectionUtils.invokePostConstruct(objs);
    ReflectionUtils.invokePreDestroy(objs);
  }

  @Test
  public void testPostConstruct_notFound() {
    List<?> objs = singletonList("x");
    ReflectionUtils.invokePostConstruct(objs);
  }

  @Test
  public void testPreDestroy_notFound() {
    List<?> objs = singletonList("x");
    ReflectionUtils.invokePreDestroy(objs);
  }

  @Test(expected = IllegalStateException.class)
  public void testPostConstruct_exception() {
    List<?> objs = singletonList(new Bad());
    ReflectionUtils.invokePostConstruct(objs);
  }

  @Test
  public void testPreDestroy_exception() {
    List<?> objs = singletonList(new Bad());
    ReflectionUtils.invokePreDestroy(objs);
  }

  @Test(expected = Error.class)
  public void testPostConstruct_error() {
    List<?> objs = singletonList(new SuperBad());
    ReflectionUtils.invokePostConstruct(objs);
  }

  @Test(expected = Error.class)
  public void testPreDestroy_error() {
    List<?> objs = singletonList(new SuperBad());
    ReflectionUtils.invokePreDestroy(objs);
  }

  private static class Bad {
    @PostConstruct public void badPostConstruct() {
      throw new UnsupportedOperationException();
    }

    @PreDestroy public void badPreDestroy() {
      throw new UnsupportedOperationException();
    }
  }

  private static class SuperBad {
    @PostConstruct public void badPostConstruct() {
      throw new Error();
    }

    @PreDestroy public void badPreDestroy() {
      throw new Error();
    }
  }
}
