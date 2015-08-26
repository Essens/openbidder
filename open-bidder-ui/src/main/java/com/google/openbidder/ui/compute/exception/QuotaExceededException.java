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

package com.google.openbidder.ui.compute.exception;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.openbidder.ui.compute.ResourceQuotaType;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quota exceeded.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class QuotaExceededException extends ComputeEngineException {

  private static final String MESSAGE = "%s quota exceeded (limit %s)";
  private static final Pattern QUOTA_EXCEEDED = Pattern.compile(
      "Quota '(\\S+)' exceeded.(?:\\s+Limit: ([\\d\\.]+))?");

  private final ResourceQuotaType quotaType;
  private final int limit;

  public QuotaExceededException(String apiProjectId, ResourceQuotaType quotaType, int limit) {
    this(apiProjectId, quotaType, limit, quotaType.getTitle());
  }

  public QuotaExceededException(
      String apiProjectId,
      ResourceQuotaType quotaType,
      int limit,
      String title) {

    super(String.format(MESSAGE, title, limit), apiProjectId);
    this.quotaType = quotaType;
    this.limit = limit;
  }

  public ResourceQuotaType getQuotaType() {
    return quotaType;
  }

  public double getLimit() {
    return limit;
  }

  public static QuotaExceededException parseErrorMessage(String apiProjectId, String errorMessage) {
    Matcher matcher = QUOTA_EXCEEDED.matcher(errorMessage);
    checkArgument(matcher.find(), "Could not parse error message: %s", errorMessage);
    ResourceQuotaType quotaType = ResourceQuotaType.valueOf(matcher.group(1));
    int limit = (int) Double.parseDouble(matcher.group(2));
    return quotaType == null
        ? new QuotaExceededException(apiProjectId, quotaType, limit, matcher.group(1))
        : new QuotaExceededException(apiProjectId, quotaType, limit);
  }
}
