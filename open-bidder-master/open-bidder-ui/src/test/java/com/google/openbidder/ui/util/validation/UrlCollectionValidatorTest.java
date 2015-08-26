/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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

import java.util.Collections;
import java.util.List;

/**
 * Tests for {@link UrlCollectionValidator}
 */
public class UrlCollectionValidatorTest {

  private UrlCollectionValidator validator;

  @Before
  public void setUp() {
    validator = new UrlCollectionValidator();
    validator.initialize(null);
  }

  @Test
  public void testEmptyUrlCollection() {
    assertTrue(validator.isValid(Collections.<String>emptyList(), null));
  }

  @Test
  public void testValidUrlCollection() {
    List<String> validUrlCollection = ImmutableList.of(
      "https://www.googleapis.com/auth/bigquery",
      "https://www.googleapis.com/auth/sqlservice",
      "https://www.googleapis.com/auth/compute.readonly",
      "https://www.googleapis.com/auth/devstorage.read_write",
      "https://www.googleapis.com/auth/taskqueue.full_control"
    );
    assertTrue(validator.isValid(validUrlCollection, null));
  }

  @Test
  public void testInvalidUrlCollection() {
    List<String> invalidUrlCollection = ImmutableList.of(
        "https://www.googleapis.com/auth/bigquery",
        "www.googleapis.com/auth/taskqueue",
        "127.0.0.1");
    assertFalse(validator.isValid(invalidUrlCollection, null));
  }
}
