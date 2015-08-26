package com.google.openbidder.util.testing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * General test utilities.
 */
@javax.annotation.ParametersAreNonnullByDefault
public class TestUtil {
  private static final class SomethingElse {}
  /** Guaranteed to fail instanceof or isAssignableFrom() test for any other type in the heap. */
  public static Object SOMETHING_ELSE = new SomethingElse();

  /**
   * Tests the all-common and important equals(), hashCode(), toString(), and compareTo() methods.
   *
   * @param single Some object.
   * @return a map that contains <code>{ single : "0" }</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T> Map<T, String> testCommonMethods(T single) {
    Map<T, String> hash = new HashMap<>();
    hash.put(single, "0");
    assertEquals("0", hash.get(single));

    assertTrue(single.equals(single)); // covers optimized code for reference equality
    assertFalse(single.equals(null)); // covers null
    assertNotNull(single.toString());

    if (single instanceof Comparable<?>) {
      @SuppressWarnings("rawtypes")
      Comparable comp = (Comparable) single;
      assertEquals(0, comp.compareTo(comp));
    }
    return hash;
  }

  /**
   * Tests the all-common and important equals(), hashCode(), toString(), and compareTo() methods.
   *
   * @param different1 Some object, optionally Comparable.
   * @param different2 Some independent object that's not equal to different1. If the objects
   * are Comparable, you should provide different1 < different2.
   * @return a map that contains <code>{ different1 : "0", different2 : "1" }</code>.
   */
  @SuppressWarnings("unchecked")
  public static <T> Map<T, String> testCommonMethods(T different1, T different2) {
    assertEquals(different1, different1);
    assertFalse(different1.equals(SOMETHING_ELSE)); // covers special code for incompatible type
    assertFalse(different1.equals(different2));
    assertNotNull(different1.toString());

    Map<T, String> hash = testCommonMethods(different1);
    hash.put(different1, "0"); // replaces
    hash.put(different2, "1"); // adds
    assertEquals(2, hash.size());
    assertEquals("0", hash.get(different1)); // still there
    assertEquals("1", hash.get(different2));

    if (different1 instanceof Comparable<?>) {
      @SuppressWarnings("rawtypes")
      Comparable compSmaller = (Comparable) different1;
      @SuppressWarnings("rawtypes")
      Comparable compBigger = (Comparable) different2;

      assertTrue(compSmaller.compareTo(compBigger) < 0);
    }

    return hash;
  }

  /**
   * Tests the all-common and important equals(), hashCode(), toString(), and compareTo() methods.
   *
   * @param equal1 Some object, optionally Comparable.
   * @param equal2 Some independent object that's equal to equal1
   * @param different Some independent object that's not equal to equal1 or equal2. If the objects
   * are Comparable, you should provide equals1 < different.
   */
  @SuppressWarnings("unchecked")
  public static <T> Map<T, String> testCommonMethods(T equal1, T equal2, T different) {
    assertEquals(equal1.hashCode(), equal2.hashCode());
    assertTrue(equal1.equals(equal2));

    Map<T, String> hash = testCommonMethods(equal1, different);
    hash.put(equal2, "2");
    assertEquals("2", hash.get(equal1));
    assertEquals("2", hash.get(equal2));
    assertEquals("1", hash.get(different));

    if (equal1 instanceof Comparable<?>) {
      @SuppressWarnings("rawtypes")
      Comparable compEqual1 = (Comparable) equal1;
      @SuppressWarnings("rawtypes")
      Comparable compEqual2 = (Comparable) equal2;

      assertEquals(0, compEqual1.compareTo(compEqual2));
    }

    return hash;
  }

  public static <E extends Exception> void testCommonException(Class<E> klass) {
    try {
      String msg = "junk";
      Exception cause = new Exception();

      try {
        Constructor<E> constr1 = klass.getConstructor();
        E e = constr1.newInstance();
        testCommonException(e);
      } catch (NoSuchMethodException e) {}

      try {
        Constructor<E> constr2 = klass.getConstructor(String.class);
        E e = constr2.newInstance(msg);
        testCommonException(e);
        assertEquals(msg, e.getMessage());
      } catch (NoSuchMethodException e) {}

      try {
        Constructor<E> constr3 = klass.getConstructor(Throwable.class);
        E e = constr3.newInstance(cause);
        testCommonException(e);
        assertEquals(cause, e.getCause());
      } catch (NoSuchMethodException e) {}

      try {
        Constructor<E> constr4 = klass.getConstructor(String.class, Throwable.class);
        E e = constr4.newInstance(msg, cause);
        testCommonException(e);
        assertEquals(msg, e.getMessage());
        assertEquals(cause, e.getCause());
      } catch (NoSuchMethodException e) {}
    } catch (ReflectiveOperationException e) {
      fail(e.getMessage());
    }
  }

  public static void testCommonException(Exception e) {
    try {
      e.toString();
      throw e;
    } catch (Exception ee) {
      assertSame(ee, e);
    }
  }

  public static void testCommonEnum(Enum<?>[] e) {
    for (int i = 1; i < e.length; ++i) {
      testCommonMethods(e[i - 1], e[i]);
    }
  }
}
