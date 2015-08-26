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

package com.google.openbidder.ui.adexchangebuyer;

import org.joda.time.LocalDate;

/**
 * DoubleClick specific utility functions.
 */
public final class DoubleClickUtils {

  public static final String PERF_REPORT = "csv_report/rtb-report-%s";
  public static final String PERF_REPORT_DATE_FORMAT = "yyyyMMdd";
  public static final String PERF_REPORT_SUFFIX = "-incr.csv";

  public static final String SNIPPET_STATUS_REPORT = "snippet_status/%s";
  public static final String SNIPPET_DATE_FORMAT = "yyyy-MM-dd";
  public static final String SNIPPET_STATUS_REPORT_SUFFIX = ".txt.gz";

  private DoubleClickUtils() {
  }

  /**
   * @return Prefix for perf reports
   */
  public static String getPerfReportPrefix(LocalDate localDate) {
    return String.format(PERF_REPORT, localDate.toString(PERF_REPORT_DATE_FORMAT));
  }

  /**
   * @return Prefix for snippet status reports
   */
  public static String getSnippetReportPrefix(LocalDate localDate) {
    return String.format(SNIPPET_STATUS_REPORT, localDate.toString(SNIPPET_DATE_FORMAT));
  }

  /**
   * @return {@code true} if this looks like a perf report, otherwise {@code false}.
   */
  public static boolean isPerfReport(String reportName) {
    return reportName.endsWith(PERF_REPORT_SUFFIX);
  }

  /**
   * @return {@code true} if this looks like a perf report, otherwise {@code false}.
   */
  public static boolean isSnippetStatusReport(String reportName) {
    return reportName.endsWith(SNIPPET_STATUS_REPORT_SUFFIX);
  }
}
