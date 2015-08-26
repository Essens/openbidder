/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.google.openbidder.interfaces;

import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.data.bidding.Creative;
import com.google.openbidder.data.bidding.Rule;
import com.google.openrtb.OpenRtb;
import com.google.protos.adx.*;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidResponse.Ad;
import java.util.List;

/**
 *
 * @author tkhalilov
 */
public interface INativeBidBuilder {
        Ad Build(BidRequest Request, OpenRtb.BidRequest.Impression Imprsn, List<Rule> Rules, List<Creative> Creatives);
}
