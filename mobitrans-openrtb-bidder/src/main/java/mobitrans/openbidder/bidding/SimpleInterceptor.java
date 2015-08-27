/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mobitrans.openbidder.bidding;

import com.google.openbidder.api.bidding.BidInterceptor;
import com.google.openbidder.api.bidding.BidRequest;
import com.google.openbidder.api.bidding.BidResponse;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openrtb.OpenRtb.BidRequest.Imp;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;

/**
 *
 * @author Aakif
 */
public class SimpleInterceptor implements BidInterceptor {

    @Override
    public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

        for (Imp imp : chain.request().imps()) {
            chain.response().addBid(Bid.newBuilder()
                    .setId("1")
                    .setImpid(imp.getId())
                    .setPrice(imp.getBidfloor() * 2)
                    .setAdm("...ad snippet..."));
        }

        chain.proceed();
    }

}
