/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

package com.google.openbidder.exchange.doubleclick.interceptor;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.common.io.BaseEncoding;
import com.google.inject.BindingAnnotation;
import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.platform.CompatibleExchanges;
import com.google.openbidder.exchange.doubleclick.config.DoubleClick;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageLite;
import com.google.protos.adx.NetworkBid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

/**
 * Dumps requests to a file.
 */
@CompatibleExchanges(DoubleClick.NAME)
public class BidRequestDumpInterceptor implements BidInterceptor {
  private final Logger logger = LoggerFactory.getLogger(BidRequestDumpInterceptor.class);

  private BufferedWriter writer;

  @Inject
  public BidRequestDumpInterceptor(@DumpFile File file) {
    BufferedWriter writer = null;

    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
    } catch (FileNotFoundException e) {
      logger.warn("Failed to open output file, dump will be disabled", e);
    }

    this.writer = writer;
  }

  @PreDestroy
  public void close() {
    BufferedWriter writer = this.writer;

    if (writer != null) {
      try {
        this.writer = null;
        writer.close();
      } catch (IOException e) {
      }
    }
  }

  @Override
  public void execute(InterceptorChain<BidRequest, BidResponse> chain) {
    BufferedWriter writer = this.writer;

    // Use the logger's level just as a convenient way to enable or disable dumping
    if (logger.isDebugEnabled() && writer != null) {
      try {
        writer.write(dump(chain.request().<NetworkBid.BidRequest>nativeRequest()));
        writer.newLine();
      } catch (IOException e) {
        close();
        logger.warn("Write error, disabling dump", e);
      }
    }

    chain.proceed();
  }

  public static String dump(MessageLite msg) {
    return BaseEncoding.base16().encode(msg.toByteString().toByteArray());
  }

  public static NetworkBid.BidRequest undump(String line) throws InvalidProtocolBufferException {
    return NetworkBid.BidRequest.parseFrom(BaseEncoding.base16().decode(line));
  }

  @BindingAnnotation
  @Target({ FIELD, PARAMETER, METHOD })
  @Retention(RUNTIME)
  public @interface DumpFile {
  }
}
