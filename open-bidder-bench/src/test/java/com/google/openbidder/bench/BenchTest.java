package com.google.openbidder.bench;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.doubleclick.openrtb.DoubleClickOpenRtbMapper;
import com.google.doubleclick.openrtb.ExtMapper;
import com.google.openbidder.exchange.doubleclick.testing.DoubleClickTestUtil;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.json.OpenRtbJsonFactory;
import com.google.openrtb.json.OpenRtbJsonWriter;
import com.google.protobuf.ByteString;
import com.google.protos.adx.NetworkBid;

import com.codahale.metrics.MetricRegistry;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BenchTest {
  private static final Logger logger = LoggerFactory.getLogger(BenchTest.class);

  @Test
  public void testDataSanity() throws IOException {
    assertEquals(
        Bench.PROTORTB,
        Bench.mapper.toOpenRtbBidRequest(Bench.PROTODC).build());
    assertEquals(Bench.PROTORTB, Bench.protortbJsonReader.readBidRequest(Bench.ORTB_JSON_BS));
  }

  @Test
  public void testRealRequest() throws IOException {
    NetworkBid.BidRequest dcReq = DoubleClickData.newRealRequest();
    ByteString dcReqBuf = dcReq.toByteString();
    DoubleClickOpenRtbMapper mapper = new DoubleClickOpenRtbMapper(
        new MetricRegistry(),
        DoubleClickTestUtil.getMetadata(),
        null,
        null,
        ImmutableList.<ExtMapper>of());
    OpenRtb.BidRequest ortbReq = mapper.toOpenRtbBidRequest(dcReq).build();
    ByteString ortbReqBuf = ortbReq.toByteString();
    OpenRtbJsonWriter ortbJsonWriter = OpenRtbJsonFactory.create().newWriter();

    ByteString json = ByteString.copyFromUtf8(ortbJsonWriter.writeBidRequest(ortbReq));

    logger.info(
          "\nDoubleClick/Protobuf     : {} bytes"
        + "\nDoubleClick/Protobuf/gzip: {} bytes"
        + "\nOpenRTB/Protobuf         : {} bytes"
        + "\nOpenRTB/Protobuf/gzip    : {} bytes"
        + "\nOpenRTB/JSON             : {} bytes"
        + "\nOpenRTB/JSON/gzip        : {} bytes"
        + "\n"
        + "\nDoubleClick/Protobuf Model:\n{}"
        + "\n"
        + "\nOpenRTB/Protobuf Model:\n{}"
        + "\n"
        + "\nOpenRTB/JSON Model:\n{}\n",
        dcReqBuf.size(), Bench.zip(dcReqBuf).size(),
        ortbReqBuf.size(), Bench.zip(ortbReqBuf).size(),
        json.size(), Bench.zip(json).size(),
        dcReq, ortbReq, json.toStringUtf8());
  }
}
