/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.openbidder.bidding.builders;

import com.google.doubleclick.util.DoubleClickMacros;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.bidding.SimpleBidInterceptor;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.data.bidding.Creative;
import com.google.openbidder.data.bidding.Rule;
import com.google.openbidder.interfaces.IOpenRtbBidBuilder;
import com.google.openrtb.OpenRtb.BidRequest.*;
import com.google.openrtb.OpenRtb.BidRequest.App;
import com.google.openrtb.OpenRtb.BidRequest.Device;
import com.google.openrtb.OpenRtb.BidRequest.Geo;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidRequest.Impression.Banner;
import com.google.openrtb.OpenRtb.BidRequest.Site;
import com.google.openrtb.OpenRtb.BidRequest.User;
import com.google.openrtb.OpenRtb.BidResponse.*;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 // @author tkhalilov
 */
public class SimpleBidBuilder implements IOpenRtbBidBuilder {

    private final Logger logger = LoggerFactory.getLogger(SimpleBidBuilder.class);

    @Override
    public Bid Build(BidRequest Request,
            Impression Imp,
            List<Rule> Rules,
            List<Creative> Creatives) {

        Rule chosenRule = null;
        Creative chosenCreative = null;

        for (Rule R : Rules) {

            Banner ImpressionBanner = Imp.getBanner();
            Device ImpressionDevice = Request.openRtb().getDevice();
            Geo ImpressionDeviceGeo = Request.openRtb().getDevice().getGeo();
            Geo ImpressionUserGeo = Request.openRtb().getUser().getGeo();

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
            
            return Bid.newBuilder()
                    .setId(Imp.getId())
                    .setImpid(Imp.getId())
                    .setPrice(Imp.getBidfloor() + chosenRule.Price)
                    .setCrid(chosenCreative.BuyerCreativeID)
                    .setH(chosenCreative.Height)
                    .setW(chosenCreative.Width)
                    .setAdm(HtmlSnippet)
                    .setExtension(ObExt.bid, ObExt.Bid.newBuilder().addClickThroughUrl(ClickThroughUrl).build())
                    .build();

        } else {
            return null;
        }
    }
}
