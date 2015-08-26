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

package com.google.openbidder.ui.resource.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The given zone host name already been used by another zone.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ZoneHostNameAlreadyExistsException extends RuntimeException {

  private static final String MESSAGE = "Zone host name %s has already been used for zone %s";

  public ZoneHostNameAlreadyExistsException(String hostName, String zoneName) {
    super(String.format(MESSAGE, hostName, zoneName));
  }
}
