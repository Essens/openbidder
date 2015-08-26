package com.google.openbidder.jetty.servlet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.openbidder.config.http.Feature;
import com.google.openbidder.config.server.BidderListenPort;
import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.Protocol;
import com.google.openbidder.http.route.HttpRoute;
import com.google.openbidder.http.route.HttpRouter;
import com.google.openbidder.jetty.ReceiverServlet;
import com.google.openbidder.servlet.testing.HttpServletResponseTester;

import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Vector;

import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * Tests for {@link ReceiverServlet}.
 */
public class ReceiverServletTest {

  @Test
  public void test() throws IOException, ServletException {
    ImmutableSet<Feature> features = ImmutableSet.<Feature>of(Feature.OTHER);
    NopReceiver receiver = new NopReceiver();
    HttpReceiver otherReceiver = new NopReceiver();
    ReceiverServlet servlet = new ReceiverServlet(ImmutableMap.<Integer, HttpRouter>of(
        BidderListenPort.DEFAULT,
        new HttpRouter(
            ImmutableSet.of(HttpRoute.create("test", HttpRoute.GET, "/path", receiver, features)),
            features,
            otherReceiver, otherReceiver, otherReceiver)));
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getProtocol()).thenReturn(Protocol.HTTP_1_1.toString());
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURL()).thenReturn(
        new StringBuffer("http://bidder.com:" + BidderListenPort.DEFAULT + "/path"));
    when(request.getHeaderNames()).thenReturn(new Vector<String>().elements());
    when(request.getParameterMap()).thenReturn(ImmutableMap.<String, String[]>of());
    when(request.getCookies()).thenReturn(null);
    when(request.getServerPort()).thenReturn(BidderListenPort.DEFAULT);
    when(request.getInputStream()).thenReturn(new ServletInputStream() {
      @Override public int read() throws IOException { return -1; }
      @Override public void setReadListener(ReadListener readListener) { }
      @Override public boolean isReady() { return true; }
      @Override public boolean isFinished() { return true; }
    });
    HttpServletResponseTester responseTester = HttpServletResponseTester.create();
    servlet.service(request, responseTester.getHttpResponse());
    assertEquals(1, receiver.receiveCounter);
  }

  static class NopReceiver implements HttpReceiver {
    int receiveCounter;
    @Override public void receive(HttpReceiverContext ctx) {
      ++receiveCounter;
    }
  }
}
