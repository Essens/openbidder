package com.google.openbidder.interfaces;

/**
 *
 * @author tkhalilov
 */

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.data.bidding.Creative;
import com.google.openbidder.data.bidding.Rule;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import java.util.ArrayList;
import java.util.List;

public interface IOpenRtbBidBuilder {
    Bid Build(BidRequest Request, Impression Imprsn, List<Rule> Rules, List<Creative> Creatives);
}
