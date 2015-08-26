/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.google.openbidder.netty.server;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.openbidder.http.HttpReceiver;
import com.google.openbidder.http.HttpReceiverContext;
import com.google.openbidder.http.HttpResponse;
import com.google.openbidder.http.response.StandardHttpResponse;
import com.google.openbidder.netty.common.NettyHttpResponse;

import org.junit.Test;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * Tests for {@link NettyHttpServerCodec}.
 */
public class NettyHttpServerCodecTest {

  @Test
  public void testWrite() throws Exception {
    NettyHttpServerCodec codec = new NettyHttpServerCodec();
    ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
    ByteBufAllocator allocator = new PooledByteBufAllocator();
    when(ctx.alloc()).thenReturn(allocator);
    ChannelPromise promise = mock(ChannelPromise.class);

    HttpResponse httpResponse = StandardHttpResponse.newBuilder().build();
    codec.write(ctx, httpResponse, promise);
    // Note: real ChannelHandlerContext would deallocate the NettyHttpResponse's buffer

    codec.write(ctx, NettyHttpResponse.getNettyResponse(httpResponse, allocator), promise);

    NettyHttpResponse nettyResponse = new NettyHttpResponse(httpResponse, allocator);
    codec.write(ctx, nettyResponse, promise);
    assertTrue(nettyResponse.netty().content().release());

    codec.write(ctx, "message", promise);
  }

  static class NopReceiver implements HttpReceiver {
    int receiveCounter;
    @Override public void receive(HttpReceiverContext ctx) {
      ++receiveCounter;
    }
  }
}
