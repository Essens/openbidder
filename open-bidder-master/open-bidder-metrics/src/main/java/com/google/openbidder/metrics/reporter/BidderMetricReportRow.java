/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.openbidder.metrics.reporter;

import com.google.api.client.util.Key;

/**
 * A row in the bidder metrics report table
 */
public class BidderMetricReportRow {

  @Key("timestamp_millis")
  private Long timestamp;

  @Key("bidder_name")
  private String bidderName;

  @Key("region")
  private String region;

  @Key("metric_name")
  private String metricName;

  @Key("metric_type")
  private String metricType;

  @Key("meter_count")
  private Long meterCount;

  @Key("meter_mean")
  private Double meterMean;

  @Key("meter_m1")
  private Double meter1Minute;

  @Key("meter_m5")
  private Double meter5Minutes;

  @Key("meter_m15")
  private Double meter15Minutes;

  @Key("histogram_count")
  private Long histogramCount;

  @Key("histogram_min")
  private Long histogramMin;

  @Key("histogram_max")
  private Long histogramMax;

  @Key("histogram_std_dev")
  private Double histogramStdDev;

  @Key("histogram_median")
  private Double histogramMedian;

  @Key("histogram_p75")
  private Double histogram75thPercentile;

  @Key("histogram_p95")
  private Double histogram95thPercentile;

  @Key("histogram_p98")
  private Double histogram98thPercentile;

  @Key("histogram_p99")
  private Double histogram99thPercentile;

  @Key("histogram_p999")
  private Double histogram999thPercentile;

  @Key("timer_duration_min")
  private Long timerDurationMin;

  @Key("timer_duration_max")
  private Long timerDurationMax;

  @Key("timer_duration_mean")
  private Double timerDurationMean;

  @Key("timer_duration_std_dev")
  private Double timerDurationStdDev;

  @Key("timer_duration_median")
  private Double timerDurationMedian;

  @Key("timer_duration_p75")
  private Double timerDuration75thPercentile;

  @Key("timer_duration_p95")
  private Double timerDuration95thPercentile;

  @Key("timer_duration_p98")
  private Double timerDuration98thPercentile;

  @Key("timer_duration_p99")
  private Double timerDuration99thPercentile;

  @Key("timer_duration_p999")
  private Double timerDuration999thPercentile;

  @Key("timer_rate_count")
  private Long timerRateCount;

  @Key("timer_rate_mean")
  private Double timerRateMean;

  @Key("timer_rate_m1")
  private Double timerRate1Minute;

  @Key("timer_rate_m5")
  private Double timerRate5Minutes;

  @Key("timer_rate_m15")
  private Double timerRate15Minutes;

  @Key("counter_count")
  private Long counterCount;

  /**
   * @return the timestamp of the metrics
   */
  public final Long getTimestamp() {
    return timestamp;
  }

  public final void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * @return VM name of the bidder
   */
  public final String getBidderName() {
    return bidderName;
  }

  public final void setBidderName(String bidderName) {
    this.bidderName = bidderName;
  }

  /**
   * @return Region of the bidder, if appropriate
   */
  public final String getRegion() {
    return region;
  }

  public final void setRegion(String region) {
    this.region = region;
  }

  /**
   * @return User defined name of the metric
   */
  public final String getMetricName() {
    return metricName;
  }

  public final void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  /**
   * @return Metric type.  Can be meter, counter or histogram
   */
  public final String getMetricType() {
    return metricType;
  }

  public final void setMetricType(String metricType) {
    this.metricType = metricType;
  }

  /**
   * @return Count of the meter.  Only defined for meter type.
   */
  public final Long getMeterCount() {
    return meterCount;
  }

  public final void setMeterCount(Long meterCount) {
    this.meterCount = meterCount;
  }

  /**
   * @return Mean of the meter.  Only defined for meter type.
   */
  public final Double getMeterMean() {
    return meterMean;
  }

  public final void setMeterMean(Double meterMean) {
    this.meterMean = meterMean;
  }

  /**
   * @return Mean of the meter in the last 1 minute.  Only defined for meter type.
   */
  public final Double getMeter1Minute() {
    return meter1Minute;
  }

  public final void setMeter1Minute(Double meterM1) {
    this.meter1Minute = meterM1;
  }

  /**
   * @return Mean of the meter in the last 5 minutes.  Only defined for meter type.
   */
  public final Double getMeter5Minutes() {
    return meter5Minutes;
  }

  public final void setMeter5Minutes(Double meterM5) {
    this.meter5Minutes = meterM5;
  }

  /**
   * @return Mean of the meter in the last 15 minutes.  Only defined for meter type.
   */
  public final Double getMeter15Minutes() {
    return meter15Minutes;
  }

  public final void setMeter15Minutes(Double meterM15) {
    this.meter15Minutes = meterM15;
  }

  /**
   * @return Number of samples in the histogram
   */
  public final Long getHistogramCount() {
    return histogramCount;
  }

  public final void setHistogramCount(Long histogramCount) {
    this.histogramCount = histogramCount;
  }

  /**
   * @return Minimum value in the histogram
   */
  public final Long getHistogramMin() {
    return histogramMin;
  }

  public final void setHistogramMin(Long histogramMin) {
    this.histogramMin = histogramMin;
  }

  /**
   * @return Maximum value in the histogram
   */
  public final Long getHistogramMax() {
    return histogramMax;
  }

  public final void setHistogramMax(Long histogramMax) {
    this.histogramMax = histogramMax;
  }

  /**
   * @return Standard deviation of the histogram
   */
  public final Double getHistogramStdDev() {
    return histogramStdDev;
  }

  public final void setHistogramStdDev(Double histogramStdDev) {
    this.histogramStdDev = histogramStdDev;
  }

  /**
   * @return Median of the histogram
   */
  public final Double getHistogramMedian() {
    return histogramMedian;
  }

  public final void setHistogramMedian(Double histogramMedian) {
    this.histogramMedian = histogramMedian;
  }

  /**
   * @return 75th percentile of the histogram
   */
  public final Double getHistogram75thPercentile() {
    return histogram75thPercentile;
  }

  public final void setHistogram75thPercentile(Double histogram75Percentile) {
    this.histogram75thPercentile = histogram75Percentile;
  }

  /**
   * @return 95th percentile of the histogram
   */
  public final Double getHistogram95thPercentile() {
    return histogram95thPercentile;
  }

  public final void setHistogram95thPercentile(Double histogram95thPercentile) {
    this.histogram95thPercentile = histogram95thPercentile;
  }

  /**
   * @return 98th percentile of the histogram
   */
  public final Double getHistogram98thPercentile() {
    return histogram98thPercentile;
  }

  public final void setHistogram98thPercentile(Double histogram98thPercentile) {
    this.histogram98thPercentile = histogram98thPercentile;
  }

  /**
   * @return 99th percentile of the histogram
   */
  public final Double getHistogram99thPercentile() {
    return histogram99thPercentile;
  }

  public final void setHistogram99thPercentile(Double histogram99thPercentile) {
    this.histogram99thPercentile = histogram99thPercentile;
  }

  /**
   * @return 99.9th percentile of the histogram
   */
  public final Double getHistogram999thPercentile() {
    return histogram999thPercentile;
  }

  public final void setHistogram999thPercentile(Double histogram999thPercentile) {
    this.histogram999thPercentile = histogram999thPercentile;
  }

  /**
   * @return Minimum duration of the timer
   */
  public final Long getTimerDurationMin() {
    return timerDurationMin;
  }

  public final void setTimerDurationMin(Long timerDurationMin) {
    this.timerDurationMin = timerDurationMin;
  }

  /**
   * @return Maximum duration of the timer
   */
  public final Long getTimerDurationMax() {
    return timerDurationMax;
  }

  public final void setTimerDurationMax(Long timerDurationMax) {
    this.timerDurationMax = timerDurationMax;
  }

  /**
   * @return Mean duration of the timer
   */
  public final Double getTimerDurationMean() {
    return timerDurationMean;
  }

  public final void setTimerDurationMean(Double timerDurationMean) {
    this.timerDurationMean = timerDurationMean;
  }

  /**
   * @return Standard deviation of the timer
   */
  public final Double getTimerDurationStdDev() {
    return timerDurationStdDev;
  }

  public final void setTimerDurationStdDev(Double timerDurationStdDev) {
    this.timerDurationStdDev = timerDurationStdDev;
  }

  /**
   * @return Median duration of the timer
   */
  public final Double getTimerDurationMedian() {
    return timerDurationMedian;
  }

  public final void setTimerDurationMedian(Double timerDurationMedian) {
    this.timerDurationMedian = timerDurationMedian;
  }

  /**
   * @return 75th percentile duration of the timer
   */
  public final Double getTimerDuration75thPercentile() {
    return timerDuration75thPercentile;
  }

  public final void setTimerDuration75thPercentile(Double timerDuration75thPercentile) {
    this.timerDuration75thPercentile = timerDuration75thPercentile;
  }

  /**
   * @return 95th percentile duration of the timer
   */
  public final Double getTimerDuration95thPercentile() {
    return timerDuration95thPercentile;
  }

  public final void setTimerDuration95thPercentile(Double timerDuration95thPercentile) {
    this.timerDuration95thPercentile = timerDuration95thPercentile;
  }

  /**
   * @return 98th percentile duration of the timer
   */
  public final Double getTimerDuration98thPercentile() {
    return timerDuration98thPercentile;
  }

  public final void setTimerDuration98thPercentile(Double timerDuration98thPercentile) {
    this.timerDuration98thPercentile = timerDuration98thPercentile;
  }

  /**
   * @return 99th percentile duration of the timer
   */
  public final Double getTimerDuration99thPercentile() {
    return timerDuration99thPercentile;
  }

  public final void setTimerDuration99thPercentile(Double timerDuration99thPercentile) {
    this.timerDuration99thPercentile = timerDuration99thPercentile;
  }

  /**
   * @return 99.9th percentile duration of the timer
   */
  public final Double getTimerDuration999thPercentile() {
    return timerDuration999thPercentile;
  }

  public final void setTimerDuration999thPercentile(Double timerDuration999thPercentile) {
    this.timerDuration999thPercentile = timerDuration999thPercentile;
  }

  /**
   * @return Count of events in the timer rate
   */
  public final Long getTimerRateCount() {
    return timerRateCount;
  }

  public final void setTimerRateCount(Long timerRateCount) {
    this.timerRateCount = timerRateCount;
  }

  /**
   * @return timerRateMean Mean timer rate
   */
  public final Double getTimerRateMean() {
    return timerRateMean;
  }

  public final void setTimerRateMean(Double timerRateMean) {
    this.timerRateMean = timerRateMean;
  }

  /**
   * @return timerRate1Minute 1 minute rate of the timer
   */
  public final Double getTimerRate1Minute() {
    return timerRate1Minute;
  }

  public final void setTimerRate1Minute(Double timerRate1Minute) {
    this.timerRate1Minute = timerRate1Minute;
  }

  /**
   * @return 5 minute rate of the timer
   */
  public final Double getTimerRate5Minutes() {
    return timerRate5Minutes;
  }

  public final void setTimerRate5Minutes(Double timerRate5Minutes) {
    this.timerRate5Minutes = timerRate5Minutes;
  }

  /**
   * @return 15 minute rate of the timer
   */
  public final Double getTimerRate15Minutes() {
    return timerRate15Minutes;
  }

  public final void setTimerRate15Minutes(Double timerRate15Minutes) {
    this.timerRate15Minutes = timerRate15Minutes;
  }

  /**
   * @return count of a counter
   */
  public final Long getCounterCount() {
    return counterCount;
  }

  public final void setCounterCount(Long counterCount) {
    this.counterCount = counterCount;
  }
}
