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

package com.google.openbidder.bench;

import com.google.caliper.Benchmark;
import com.google.caliper.runner.CaliperMain;
import com.google.common.collect.ImmutableList;
import com.google.doubleclick.openrtb.DoubleClickOpenRtbMapper;
import com.google.doubleclick.openrtb.ExtMapper;
import com.google.doubleclick.util.DoubleClickMetadata;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonReader;
import com.google.openrtb.json.OpenRtbJsonWriter;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import com.codahale.metrics.MetricRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Benchmark for OpenRTB models, DoubleClick model, JSON serialization.
 */
public class Bench extends Benchmark {
  private static final Logger logger = LoggerFactory.getLogger(Bench.class);
  // DoubleClick/Proto
  static final DoubleClickOpenRtbMapper mapper = new DoubleClickOpenRtbMapper(
      new MetricRegistry(),
      new DoubleClickMetadata(new DoubleClickMetadata.ResourceTransport()),
      null,
      ImmutableList.<ExtMapper>of());
  static final com.google.protos.adx.NetworkBid.BidRequest
      PROTODC = DoubleClickData.newRequest(true);
  static final ByteString PROTODC_BUF = DoubleClickData.newRequest(true).toByteString();
  static final ByteString PROTODC_BUF_GZ = zip(PROTODC_BUF);

  // OpenRTB/Proto
  static final com.google.openrtb.OpenRtb.BidRequest
      PROTORTB = OpenRtbData.newBidRequest(true);
  static final ByteString PROTORTB_BUF = PROTORTB.toByteString();
  static final ByteString PROTORTB_BUF_GZ = zip(PROTORTB_BUF);

  // OpenRTB/JSON
  static final OpenRtbJsonFactory jsonFactory = OpenRtbJsonFactory.create();
  static final OpenRtbJsonWriter protortbJsonWriter = jsonFactory.newWriter();
  static final OpenRtbJsonReader protortbJsonReader = jsonFactory.newReader();

  static final String ORTB_JSON;
  static final ByteString ORTB_JSON_BS;
  static final ByteString ORTB_JSON_GZ;

  static {
    ByteString json = null;
    try {
      ByteString.Output bos = ByteString.newOutput();
      protortbJsonWriter.writeBidRequest(PROTORTB, bos);
      json = bos.toByteString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    ORTB_JSON_BS = json;
    ORTB_JSON_GZ = zip(json);
    ORTB_JSON = json.toStringUtf8();
  }

  public static void main(String[] args) {
      

    logger.info(
          "\nDoubleClick/Protobuf     : {} bytes"
        + "\nDoubleClick/Protobuf/gzip: {} bytes"
        + "\nOpenRTB/Protobuf         : {} bytes"
        + "\nOpenRTB/Protobuf/gzip    : {} bytes"
        + "\nOpenRTB/JSON             : {} bytes"
        + "\nOpenRTB/JSON/gzip        : {} bytes"
        + "\n"
        + "\nOpenRTB/Protobuf Model:\n{}"
        + "\n"
        + "\nOpenRTB/JSON Model:\n{}\n",
        PROTODC_BUF.size(), PROTODC_BUF_GZ.size(),
        PROTORTB_BUF.size(), PROTORTB_BUF_GZ.size(),
        ORTB_JSON.length(), ORTB_JSON_GZ.size(),
        PROTORTB, ORTB_JSON);
    CaliperMain.main(Bench.class, args);
  }

  public int time_build_ProtoOrtb(int reps) {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += OpenRtbData.newBidRequest(true).getUser().getId().length();
    }
    return dummy;
  }

  public int time_buf_protoDc(int reps) throws InvalidProtocolBufferException {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += com.google.protos.adx.NetworkBid.BidRequest
          .parseFrom(PROTODC_BUF).getGoogleUserId().length();
    }
    return dummy;
  }

  public int time_buf_protoOrtb(int reps) throws InvalidProtocolBufferException {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += com.google.openrtb.OpenRtb.BidRequest
          .parseFrom(PROTORTB_BUF).getUser().getId().length();
    }
    return dummy;
  }

  public int time_json_protoOrtb(int reps) throws IOException {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += protortbJsonReader.readBidRequest(ORTB_JSON_BS).getUser().getId().length();
    }
    return dummy;
  }

  public int time_protoDc_protoOrtb(int reps) {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += mapper.toOpenRtbBidRequest(PROTODC).getUser().getId().length();
    }
    return dummy;
  }

  public int protoOrtb_buf(int reps) {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += PROTORTB.toByteString().size();
    }
    return dummy;
  }

  public int protoOrtb_json(int reps) throws IOException {
    int dummy = 0;
    for (int i = 0; i < reps; ++i) {
      dummy += protortbJsonWriter.writeBidRequest(PROTORTB).length();
    }
    return dummy;
  }

  static ByteString zip(ByteString data) {
    try (ByteString.Output bso = ByteString.newOutput(data.size());
        GZIPOutputStream zos = new GZIPOutputStream(bso, true)) {
      data.writeTo(zos);
      zos.flush();
      return bso.toByteString();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
