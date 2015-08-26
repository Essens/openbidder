package com.google.openbidder.impression;

import static org.junit.Assert.assertEquals;

import com.google.openbidder.api.impression.ImpressionInterceptor;
import com.google.openbidder.api.impression.ImpressionRequest;
import com.google.openbidder.api.impression.ImpressionResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.impression.ImpressionTestUtil;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;
import org.junit.Test;


/**
 * Tests for {@link ImpressionRequestReceiver}.
 */
public class ImpressionRequestReceiverTest {

  private ImpressionRequestReceiver newReceiver(ImpressionInterceptor... interceptors) {
    MetricRegistry metricRegistry = new MetricRegistry();
    return new ImpressionRequestReceiver(
        NoExchange.INSTANCE,
        metricRegistry,
        ImpressionTestUtil.newImpressionController(metricRegistry, interceptors));
  }

  @Test
  public void testRequestResponse() {
    HttpRequest httpRequest = newImpressionRequest("100");
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver().receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
  }

  @Test
  public void testBadRequest() {
    HttpRequest httpRequest = newImpressionRequest("GARBAGE");
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver().receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
  }

  @Test
  public void testInterceptorAbort() {
    HttpRequest httpRequest = newImpressionRequest("100");
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver(new ImpressionInterceptor() {
      @Override
      public void execute(InterceptorChain<ImpressionRequest, ImpressionResponse> chain) {
        throw new InterceptorAbortException("Tracking ads is hard, let's go shopping");
      }
    }).receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
  }

  @Test(expected = NullPointerException.class)
  public void testInterceptorRuntimeException() {
    HttpRequest httpRequest = newImpressionRequest("GARBAGE");
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver(new ImpressionInterceptor() {
      @Override
      public void execute(InterceptorChain<ImpressionRequest, ImpressionResponse> chain) {
        throw new NullPointerException();
      }
    }).receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
  }

  public static HttpRequest newImpressionRequest(String price) {
    return StandardHttpRequest.newBuilder()
        .setMethod("GET")
        .setUri("http://localhost/impression")
        .addParameter(PriceName.DEFAULT, price == null ? "" : price)
        .build();
  }
}
