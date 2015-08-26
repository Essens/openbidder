/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.google.openbidder.bidding.builders;


import com.google.common.base.Stopwatch;
import com.google.doubleclick.util.DoubleClickMacros;
import com.google.gson.Gson;
import com.google.openbidder.api.bidding.*;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.bidding.builders.SimpleBidBuilder;
import com.google.openbidder.cache.ConfigsCache;
import com.google.openbidder.cache.CreativesCache;
import com.google.openbidder.cache.RulesCache;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.containers.NativeRtbContainer.RequestContainer;
//import com.google.openbidder.containers.RequestContainer;
import com.google.openbidder.data.bidding.Config;
import com.google.openbidder.data.bidding.Creative;
import com.google.openbidder.data.bidding.Rule;
import com.google.openbidder.interfaces.INativeBidBuilder;
import com.google.openbidder.queues.RequestsQueue;
import com.google.openbidder.workers.*;
import com.google.openrtb.OpenRtb;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidRequest.AdSlot;
import com.google.protos.adx.NetworkBid.BidResponse.Ad;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkhalilov
 */
public class NativeBidBuilder implements INativeBidBuilder  {
    
    private final Logger logger = LoggerFactory.getLogger(NativeBidBuilder.class);

    @Override
    public NetworkBid.BidResponse.Ad Build(BidRequest Request,
            OpenRtb.BidRequest.Impression Imp,
            List<Rule> Rules,
            List<Creative> Creatives) {

        Rule chosenRule = null;
        Creative chosenCreative = null;

        for (Rule R : Rules) {

            OpenRtb.BidRequest.Impression.Banner ImpressionBanner = Imp.getBanner();
            OpenRtb.BidRequest.Device ImpressionDevice = Request.openRtb().getDevice();
            OpenRtb.BidRequest.Geo ImpressionDeviceGeo = Request.openRtb().getDevice().getGeo();
            OpenRtb.BidRequest.Geo ImpressionUserGeo = Request.openRtb().getUser().getGeo();

            // Searching for a rule that satisifies "country", "height" and "width" parameters
            if (R.Country.toLowerCase().equals(ImpressionDeviceGeo.getCountry().toLowerCase())
                    && R.BannerHeight == ImpressionBanner.getH()
                    && R.BannerWidth == ImpressionBanner.getW()
                    && ImpressionDevice.getUa().toLowerCase().contains(R.OperatingSystem.toLowerCase())) {

                chosenRule = R;

                // Searching for a creative that satisifies the rule chosen
                for (Creative C : Creatives) {
                    if (C.ID == R.CreativeID) {
                        chosenCreative = C;
                        break;
                    }
                }

                break;
            }
        }

        if (chosenRule != null & chosenCreative != null) {

            String HtmlSnippet
                    = chosenCreative.HtmlSnippet.replaceAll("%\\{\\$\\{RequestID\\}\\}%", Request.openRtb().getId());
            
            String ClickThroughUrl =
                    chosenCreative.ClickThroughUrl + 
                    "&" + "RequestID" + "=" + Request.openRtb().getId() +
                    "&" + "ImpressionID" + "=" + Imp.getId() +
                    "&" + "BidID" + "=" + Imp.getId();
            
            return NetworkBid.BidResponse.Ad.newBuilder().addAdslot( 
                    NetworkBid.BidResponse.Ad.AdSlot.newBuilder()
                    .setId(Integer.valueOf(Imp.getId()))
                    .setMaxCpmMicros((long) (Imp.getBidfloor() > 0 ? Imp.getBidfloor() * 2 * 1000 : 15 * 1000)))
                    .setBuyerCreativeId(chosenCreative.BuyerCreativeID)
                    .setHeight(chosenCreative.Height)
                    .setWidth(chosenCreative.Width)
                    .setHtmlSnippet(HtmlSnippet)
                    .addClickThroughUrl(ClickThroughUrl)
                    .build();

        } else {
            return null;
        }
    }
}
