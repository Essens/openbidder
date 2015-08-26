package com.google.openbidder.exchange.doubleclick.match;

import static org.junit.Assert.assertEquals;

import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openbidder.http.HttpRequest;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.receiver.DefaultHttpReceiverContext;
import com.google.openbidder.http.request.StandardHttpRequest;
import com.google.openbidder.http.response.StandardHttpResponse;

import com.codahale.metrics.MetricRegistry;

import org.apache.http.HttpStatus;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Tests for {@link DoubleClickMatchRequestReceiver}.
 */
public class DoubleClickMatchRequestReceiverTest {

  @Test
  public void testMatch() throws URISyntaxException {
    HttpRequest request = StandardHttpRequest.newBuilder()
        .setUri(DoubleClickMatchRequestTest.MATCH_REQUEST_COOKIE)
        .build();
    HttpResponse.Builder response = StandardHttpResponse.newBuilder();
    MetricRegistry metricRegistry = new MetricRegistry();
    DoubleClickMatchRequestReceiver receiver = new DoubleClickMatchRequestReceiver(
        metricRegistry,
        DoubleClickTestUtil.newMatchController(metricRegistry),
        new URI(DoubleClickMatchResponseTest.DOUBLECLICK_REDIRECT_URI),
        "nid");
    receiver.receive(new DefaultHttpReceiverContext(request, response));
    assertEquals(HttpStatus.SC_OK, response.getStatusCode());
  }

  @Test
  public void testMatchPush() throws URISyntaxException {
    HttpRequest request = StandardHttpRequest.newBuilder()
        .setUri(DoubleClickMatchRequestTest.MATCH_REQUEST_COOKIE)
        .addParameter(DoubleClickMatchTag.GOOGLE_PUSH, "myPush")
        .build();
    HttpResponse.Builder response = StandardHttpResponse.newBuilder();
    MetricRegistry metricRegistry = new MetricRegistry();
    DoubleClickMatchRequestReceiver receiver = new DoubleClickMatchRequestReceiver(
        metricRegistry,
        DoubleClickTestUtil.newMatchController(metricRegistry),
        new URI(DoubleClickMatchResponseTest.DOUBLECLICK_REDIRECT_URI),
        "myNid");
    receiver.receive(new DefaultHttpReceiverContext(request, response));
    assertEquals("myNid", response.getRedirectParameter(DoubleClickMatchTag.GOOGLE_NID));
    assertEquals("myPush", response.getRedirectParameter(DoubleClickMatchTag.GOOGLE_PUSH));
    assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusCode());
  }
}
