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

import com.google.common.net.InetAddresses;

import java.net.Inet4Address;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validate if the input is a list of well-constructed CIDR blocks.
 */
public class CidrListValidator
    implements ConstraintValidator<CidrList, List<String>> {

  @Override
  public void initialize(CidrList target) {
  }

  @Override
  public boolean isValid(List<String> cidrList, ConstraintValidatorContext context) {
    if (cidrList == null || cidrList.isEmpty()) {
      return true;
    }

    for (String cidr : cidrList) {
      if (!isValidCidr(cidr)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if the input String is a well-constructed CIDR block.
   * <p>
   * IPv4 and IPv6 are both supported.
   * <p>
   * CIDR prefixSize is optional and thus a valid block could either represent a single IP address
   * or a CIDR netblock.
   */
  private boolean isValidCidr(String cidr) {

    int slash = cidr.indexOf('/');
    if (slash > -1) {
      String ip = cidr.substring(0, slash);
      int prefixSize = Integer.parseInt(cidr.substring(slash + 1));

      if (InetAddresses.isInetAddress(ip)) {
        if (InetAddresses.forString(ip) instanceof Inet4Address) {
          return isInRange(0, 32, prefixSize);
        } else {
          return isInRange(0, 128, prefixSize);
        }
      }

      return false;
    } else {
      return InetAddresses.isInetAddress(cidr);
    }
  }

  private boolean isInRange(int min, int max, int number) {
    return number >= min && number <= max;
  }
}
