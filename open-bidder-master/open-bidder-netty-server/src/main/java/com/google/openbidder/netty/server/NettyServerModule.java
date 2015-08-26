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

package com.google.openbidder.netty.server;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.openbidder.config.server.WebserverRuntime;
import com.google.openbidder.config.system.AvailableProcessors;
import com.google.openbidder.netty.server.config.BossGroup;
import com.google.openbidder.netty.server.config.ServerConnectionChannel;
import com.google.openbidder.netty.server.config.UserGroup;
import com.google.openbidder.netty.server.config.WorkerGroup;
import com.google.openbidder.util.ReflectionUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.SystemPropertyUtil;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Singleton;

/**
 * Netty server configuration.
 */
@Parameters(separators = "=")
public class NettyServerModule extends AbstractModule {
  private static final Logger logger = LoggerFactory.getLogger(NettyServerModule.class);

  @Parameter(names = "--boss_threads_per_cpu",
      description = "Number of boss group threads per CPU")
  private Double bossThreadsPerCpu;

  @Parameter(names = "--boss_threads",
      description = "Number of boss group threads")
  private Integer bossThreads;

  @Parameter(names = "--worker_threads_per_cpu",
      description = "Number of worker group threads per CPU")
  private Double workerThreadsPerCpu;

  @Parameter(names = "--worker_threads",
      description = "Number of worker group threads")
  private Integer workerThreads;

  @Parameter(names = "--user_threads_per_cpu",
      description = "Number of user group threads per CPU"
          + " (default = use worker threads)")
  private Double userThreadsPerCpu;

  @Parameter(names = "--user_threads",
      description = "Number of user group threads (default = use worker threads)")
  private Integer userThreads;

  @Parameter(names = "--native_epoll", arity = 1,
      description = "Use native epoll handler (Linux only, ignored otherwise)")
  private boolean nativeEpoll = true;

  @Parameter(names = "--native_epoll_maxevents",
      description = "Maximum events per epoll call (implies --native_epoll=true)")
  private Integer nativeEpollMaxevents;

  @Parameter(names = "--allocator", arity = 1,
      description = "Netty's memory allocator: STANDARD, POOLED, POOLED_TL")
  private NettyAllocator allocator = NettyAllocator.POOLED_TL;

  @Override
  protected void configure() {
    if (nativeEpollMaxevents != null) {
      nativeEpoll = true;
    }
    if (nativeEpoll // Same check from io.netty.channel.epoll.Native
        && !SystemPropertyUtil.get("os.name").toLowerCase(Locale.UK).trim().startsWith("linux")) {
      nativeEpoll = false;
    }

    bind(new TypeLiteral<Class<? extends ServerChannel>>() {})
        .annotatedWith(ServerConnectionChannel.class)
        .toInstance(nativeEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class);
    bind(new TypeLiteral<ChannelInitializer<SocketChannel>>() {})
        .to(NettyChannelInitializer.class)
        .in(Scopes.SINGLETON);
    bind(Service.class)
        .annotatedWith(WebserverRuntime.class)
        .to(NettyServer.class)
        .in(Scopes.SINGLETON);
    ImmutableMap.Builder<ChannelOption<?>, Object> channelOptions = ImmutableMap.builder();
    if (allocator == NettyAllocator.POOLED) {
      channelOptions.put(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(
          true,
          (int) ReflectionUtils.readField(PooledByteBufAllocator.class, "DEFAULT_NUM_HEAP_ARENA"),
          (int) ReflectionUtils.readField(PooledByteBufAllocator.class, "DEFAULT_NUM_DIRECT_ARENA"),
          (int) ReflectionUtils.readField(PooledByteBufAllocator.class, "DEFAULT_PAGE_SIZE"),
          (int) ReflectionUtils.readField(PooledByteBufAllocator.class, "DEFAULT_MAX_ORDER"),
          0, 0, 0 /* No TL cache */));
    } else if (allocator == NettyAllocator.POOLED_TL) {
      channelOptions.put(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true));
    }
    bind(new TypeLiteral<Map<ChannelOption<?>, Object>>() {}).toInstance(channelOptions.build());
  }

  @Provides
  @Singleton
  @BossGroup
  public int provideBossGroupSize(@AvailableProcessors int availableProcessors) {
    checkArgument(bossThreads == null || bossThreadsPerCpu == null,
        "Cannot provide both --boss_threads and --boss_threads_per_cpu");
    return poolSize(bossThreads, bossThreadsPerCpu, availableProcessors,
        BossGroup.DEFAULT_PER_CPU);
  }

  @Provides
  @Singleton
  @WorkerGroup
  public int provideWorkerGroupSize(@AvailableProcessors int availableProcessors) {
    checkArgument(workerThreads == null || workerThreadsPerCpu == null,
        "Cannot provide both --worker_threads and --worker_threads_per_cpu");
    return poolSize(workerThreads, workerThreadsPerCpu, availableProcessors,
        WorkerGroup.DEFAULT_PER_CPU);
  }

  @Provides
  @Singleton
  @UserGroup
  public int provideUserGroupSize(@AvailableProcessors int availableProcessors) {
    checkArgument(userThreads == null || userThreadsPerCpu == null,
        "Cannot provide both --user_threads and --user_threads_per_cpu");
    return poolSize(userThreads, userThreadsPerCpu, availableProcessors,
        UserGroup.DEFAULT_PER_CPU);
  }

  @Provides
  @Singleton
  @BossGroup
  public EventLoopGroup provideBossGroup(@BossGroup int bossGroupSize) {
    logger.info("Boss group size: {}", bossGroupSize);
    return newEventLoopGroup(bossGroupSize);
  }

  @Provides
  @Singleton
  @WorkerGroup
  public EventLoopGroup provideWorkerGroup(@WorkerGroup int workerGroupSize) {
    logger.info("Worker group size: {}", workerGroupSize);
    return newEventLoopGroup(workerGroupSize);
  }

  @Provides
  @Singleton
  @UserGroup
  public @Nullable EventExecutorGroup provideUserGroup(@UserGroup int userGroupSize) {
    if (userGroupSize == 0) {
      logger.info("User group not installed, processing requests on worker group");
      return null;
    } else {
      logger.info("User group size: {}", userGroupSize);
      return new DefaultEventExecutorGroup(userGroupSize);
    }
  }

  private static int poolSize(Integer absolute, Double relative, int relBase, double defRelValue) {
    return absolute == null
        ? (int) Math.round(relative == null ? defRelValue : relative) * relBase
        : absolute;
  }

  private EventLoopGroup newEventLoopGroup(int groupSize) {
    return nativeEpoll
        ? (nativeEpollMaxevents == null)
            ? new EpollEventLoopGroup(groupSize)
            : new EpollEventLoopGroup(groupSize, null, nativeEpollMaxevents)
        : new NioEventLoopGroup(groupSize);
  }

  /**
   * Options for Netty's memory allocator.
   */
  public static enum NettyAllocator {
    /** Default memory allocator (non-pooled) */
    STANDARD,
    /** Pooled memory allocator */
    POOLED,
    /** Pooled memory allocator with Thread-Local cache */
    POOLED_TL;
  }
}
