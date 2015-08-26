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

import com.google.common.net.InetAddresses;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validate if the input bucket name follows Google Cloud Storage Bucket naming guidelines.
 * <p>See https://developers.google.com/storage/docs/bucketnaming for more details.
 */
public class BucketNameValidator implements ConstraintValidator<BucketName, String> {
  private static final int MIN_LENGTH = 3;
  private static final int MAX_SEGMENT_LENGTH = 63;
  private static final int MAX_TOTAL_LENGTH = 222;

  @Override
  public void initialize(final BucketName target) {
  }

  /**
   * Implementation of the validation logic on Google Cloud Storage bucket name.
   * A valid Google Cloud Storage bucket name must satisfy:
   * <ol>
   *   <li>contain only lowercase letters, numbers, dashes and dots</li>
   *   <li>start and end with a number or letter</li>
   *   <li>contain 3 to 63 characters. Names containing dots can hold up to 222 characters
   *   but each dot-separated component can be no longer than 63 characters</li>
   *   <li>cannot be represented as an IP address in dotted-decimal notation</li>
   *   <li>cannot begin with the "goog" prefix</li>
   * </ol>
   */
  @Override
  public boolean isValid(String jarBucketName, final ConstraintValidatorContext context)  {
    if (jarBucketName == null) {
      return true;
    }

    if (!isValidStartAndEnd(jarBucketName) || jarBucketName.startsWith("goog") ||
        !jarBucketName.matches("^[a-z0-9\\.-]+$")) {
      return false;
    }

    //check if contains dots
    if (jarBucketName.indexOf(".") == -1) {
      if (!isValidLength(jarBucketName))  {
        return false;
      }
    } else {
      if (jarBucketName.length() > MAX_TOTAL_LENGTH ||
          InetAddresses.isInetAddress(jarBucketName)) {
        return false;
      }
      //TODO(jnwang): Bucket name contains dot(s) also needs further verification, e.g it has
      // to be syntactically valid DNS name and ends with a currently-recognized top-level domain.
      String[] parts = jarBucketName.split("\\.");
      for (String part : parts) {
        if (part.length() > MAX_SEGMENT_LENGTH)  {
          return false;
        }
      }
    }
    return true;
  }

  private boolean isValidStartAndEnd(String jarBucketName) {
    char start = jarBucketName.charAt(0);
    char end = jarBucketName.charAt(jarBucketName.length() - 1);
    return (Character.isDigit(start) || Character.isLetter(start))
        && (Character.isDigit(end) || Character.isLetter(end));
  }

  private boolean isValidLength(String jarBucketName) {
    return jarBucketName.length() >= MIN_LENGTH && jarBucketName.length() <= MAX_SEGMENT_LENGTH;
  }
}
