/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.ui.util.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link BucketNameValidator}
 */
public class BucketNameValidatorTest {
  private BucketNameValidator validator;

  @Before
  public void setUp() {
    validator = new BucketNameValidator();
    validator.initialize(null);
  }

  @Test
  public void testEmptyBucketName() {
    assertTrue(validator.isValid(null, null));
  }

  @Test
  public void testStartAndEnd() {
    List<String> validBucketNameList = ImmutableList.of(
        "abc1",
        "1abc",
        "123");
    for (String validBucketName : validBucketNameList) {
      assertTrue(validator.isValid(validBucketName, null));
    }

    List<String> invalidBucketNameList = ImmutableList.of(
        ".abc1",
        "$abc",
        "-123.");
    for (String invalidBucketName : invalidBucketNameList) {
      assertFalse(validator.isValid(invalidBucketName, null));
    }
  }

  @Test
  public void testPrefix() {
    assertTrue(validator.isValid("gooabc11", null));
    assertFalse(validator.isValid("googabc11", null));
  }

  @Test
  public void testBucketNameAllowedCharSet() {
    List<String> validBucketNameList = ImmutableList.of(
        "abc123",
        "ab.c",
        "a-c",
        "a--.1.bc");
    for (String validBucketName : validBucketNameList) {
      assertTrue(validator.isValid(validBucketName, null));
    }

    List<String> invalidBucketNameList = ImmutableList.of(
        "%%^",
        "aBc",
        "1@(2.3");
    for (String invalidBucketName : invalidBucketNameList) {
      assertFalse(validator.isValid(invalidBucketName, null));
    }
  }

  @Test
  public void testDottedIpAddress() {
    assertTrue(validator.isValid("192.168.5.com", null));
    assertFalse(validator.isValid("192.168.5.4", null));
  }

  @Test
  public void testLength() {
    assertTrue(validator.isValid("abcabc", null));
    assertFalse(validator.isValid("a2", null));

    StringBuilder bucketNameTooLong = new StringBuilder(64);
    for (int i = 0; i < 64; i++) {
      bucketNameTooLong.append('a');
    }
    assertFalse(validator.isValid(bucketNameTooLong.toString(), null));
  }
}
