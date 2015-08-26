package com.google.openbidder.click;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.base.Strings;
import com.google.openbidder.api.click.ClickInterceptor;
import com.google.openbidder.api.click.ClickRequest;
import com.google.openbidder.api.click.ClickResponse;
import com.google.openbidder.api.interceptor.InterceptorAbortException;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.platform.NoExchange;
import com.google.openbidder.api.testing.click.ClickTestUtil;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;
import org.junit.Test;

/**
 * Tests for {@link ClickRequestReceiver}.
 */
public class ClickRequestReceiverTest {
  private static final String AD_URL = "http://ad.network.com/click";

  private static class ClickRedirectInterceptor implements ClickInterceptor {
    @Override
    public void execute(InterceptorChain<ClickRequest, ClickResponse> chain) {
      chain.response().setRedirectLocation(
          chain.request().httpRequest().getParameterDecoded("ad_url"));
      chain.proceed();
    }
  }

  private ClickRequestReceiver newReceiver(ClickInterceptor... interceptors) {
    MetricRegistry metricRegistry = new MetricRegistry();
    return new ClickRequestReceiver(
        NoExchange.INSTANCE,
        metricRegistry,
        ClickTestUtil.newClickController(metricRegistry, interceptors));
  }

  @Test
  public void testRequestResponse() {
    HttpRequest httpRequest = newHttpClickRequest(AD_URL);
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver(new ClickRedirectInterceptor()).receive(
        new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, httpResponse.getStatusCode());
    assertEquals(AD_URL, String.valueOf(httpResponse.getRedirectUri()));
  }

  @Test
  public void testRequestResponseNoLocation() {
    HttpRequest httpRequest = newHttpClickRequest(null);
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver().receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
    assertNull(httpResponse.getRedirectUri());
  }

  @Test
  public void testInterceptorAbort() {
    HttpRequest httpRequest = newHttpClickRequest(AD_URL);
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();
    newReceiver(new ClickInterceptor() {
      @Override
      public void execute(InterceptorChain<ClickRequest, ClickResponse> chain) {
        throw new InterceptorAbortException("Tracking ads is hard, let's go shopping");
      }
    }).receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
    assertEquals(HttpStatus.SC_OK, httpResponse.getStatusCode());
  }

  @Test(expected = NullPointerException.class)
  public void testInterceptorRuntimeException() {
    HttpRequest httpRequest = newHttpClickRequest(AD_URL);
    HttpResponse.Builder httpResponse = StandardHttpResponse.newBuilder();

    newReceiver(new ClickInterceptor() {
      @Override
      public void execute(InterceptorChain<ClickRequest, ClickResponse> chain) {
        throw new NullPointerException();
      }
    }).receive(new DefaultHttpReceiverContext(httpRequest, httpResponse));
  }

  public static HttpRequest newHttpClickRequest(String adUrl) {
    HttpRequest.Builder httpRequest = StandardHttpRequest.newBuilder()
        .setMethod("GET")
        .setUri("http://example.com");
    if (!Strings.isNullOrEmpty(adUrl)) {
      httpRequest.addParameter("ad_url", adUrl);
    }
    return httpRequest.build();
  }
}
