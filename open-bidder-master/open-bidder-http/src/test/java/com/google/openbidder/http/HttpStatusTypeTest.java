package com.google.openbidder.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.openbidder.util.testing.TestUtil;

import org.junit.Test;

/**
 * Tests for {@link HttpStatusType}.
 */
public class HttpStatusTypeTest {

  @Test
  public void test() {
    TestUtil.testCommonEnum(HttpStatusType.values());
    assertEquals(200, HttpStatusType.SUCCESS.min());
    assertEquals(299, HttpStatusType.SUCCESS.max());
    assertTrue(HttpStatusType.SUCCESS.contains(200));
    assertTrue(!HttpStatusType.SUCCESS.contains(100));
    assertTrue(!HttpStatusType.SUCCESS.contains(500));
  }
}
