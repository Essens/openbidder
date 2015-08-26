package com.google.openbidder.bidding;

import com.google.common.base.Stopwatch;
import com.google.doubleclick.util.DoubleClickMacros;
import com.google.gson.Gson;
import com.google.openbidder.api.bidding.*;
import com.google.openbidder.api.interceptor.InterceptorChain;
import com.google.openbidder.api.openrtb.ObExt;
import com.google.openbidder.bidding.builders.NativeBidBuilder;
import com.google.openbidder.bidding.builders.SimpleBidBuilder;
import com.google.openbidder.cache.ConfigsCache;
import com.google.openbidder.cache.CounterCache;
import com.google.openbidder.cache.CreativesCache;
import com.google.openbidder.cache.RulesCache;
import com.google.openbidder.config.impression.PriceName;
import com.google.openbidder.containers.NativeRtbContainer.RequestContainer;
//import com.google.openbidder.containers.RequestContainer;
import com.google.openbidder.data.bidding.Config;
import com.google.openbidder.data.bidding.Creative;
import com.google.openbidder.data.bidding.Rule;
import com.google.openbidder.queues.RequestsQueue;
import com.google.openbidder.workers.*;
import com.google.openrtb.OpenRtb.BidRequest.Impression;
import com.google.openrtb.OpenRtb.BidResponse.SeatBid.Bid;
import com.google.protos.adx.NetworkBid;
import com.google.protos.adx.NetworkBid.BidResponse.Ad;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.slf4j.*;

// 1. Interceptor is called on each impression this Open Bidder receives
// 2. Every "One" Minute the "Bidding Rules" cache is refreshed - which is fetched from Mobitrans DB via a "Background Worker"
// 3. Each (Impression) received will be built by "SimpleBidBuilder" class
// 4. Each (Impression, Bid) with its details is placed in a "ConcurrentQueue" to be persisted via a "Background Worker" -> RabbitMQWorker
// 5. SimpleBidBuilder class will build the bid based on the "Bidding Rules" passed to it
public class SimpleBidInterceptor implements BidInterceptor {

    private final Logger logger = LoggerFactory.getLogger(SimpleBidInterceptor.class);

    // To regularly count received requests/second
    private final Timer CounterTimer = new Timer();

    // To regularly update the local cache
    private final Timer CacheTimer = new Timer();

    // Local cache of all the rules fetched from the DB.
    private List<Rule> Rules = new ArrayList<Rule>();

    // Local cache of all creatives fetched from the DB.
    private List<Creative> Creatives = new ArrayList<Creative>();

    // Local cache of all the config values fetched from the DB.
    private List<Config> Configs = new ArrayList<Config>();

    // To build bid responses
    private NativeBidBuilder BidBuilder = new NativeBidBuilder();

    public SimpleBidInterceptor() {

        logger.error("Constructing the SimpleBidInterceptor....");

        // 1. Running Redis worker threads 
        for (int i = 0; i < 40; i++) {
            new RedisWorker().start();
        }

        logger.error("Starting the Creatives, Rules, and Config Workers....");

        // 2. Running Creatives/Rules worker threads
        new CreativesWorker().start();
        new RulesWorker().start();
        new ConfigWorker().start();

        logger.error("Starting the Counter Timer....");

        // 3. Schedule the timer to record the number of BidRequests received
        CounterTimer.scheduleAtFixedRate(new CounterTimerWorker(), 0, 1000);

        logger.error("Starting the Cacher Timer....");

        // 4. Schedule the timer to update local cache
        CacheTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                synchronized (CreativesCache.Cache) {
                    Creatives.clear();
                    for (Creative c : CreativesCache.Cache) {
                        Creatives.add(c);
                    }
                }

                synchronized (RulesCache.Cache) {
                    Rules.clear();
                    for (Rule r : RulesCache.Cache) {
                        Rules.add(r);
                    }
                }

                synchronized (ConfigsCache.Cache) {
                    Configs.clear();
                    for (Config c : ConfigsCache.Cache) {
                        Configs.add(c);
                    }
                }
            }
        }, 0, 5000);

        logger.error("Finished Constructing the SimpleBidInterceptor....");
    }

    @Override
    public void execute(InterceptorChain<BidRequest, BidResponse> chain) {

        // 1. Going through all impressions and building the bids
        for (Impression Imprsn : chain.request().imps()) {

            // 2. Get the "native request"
            NetworkBid.BidRequest Request = chain.request().nativeRequest();

            // 3. Building the "native ad" to place it into the "native response"
            NetworkBid.BidResponse.Ad bid = BidBuilder.Build(chain.request(), Imprsn, Rules, Creatives);

            if (bid != null) {

                // 4. Get the "native response"
                NetworkBid.BidResponse.Builder Response = chain.response().nativeResponse();

                // 5. Adding bid to the Interceptor Response
                Response.addAd(bid);

                // 6. Building the Container
                RequestContainer.Builder Container = RequestContainer.newBuilder();
                Container.setId(chain.request().openRtb().getId());

                // 7. Adding the Request to the Container
                if (Request != null) {
                    Container.setRequest(Request.toByteString());
                }

                // 8. Adding the Response to the Container
                if (Response != null) {
                    Container.setResponse(Response.build().toByteString());
                }

                if (RequestsQueue.Queue.size() < 200000) {

                    // 7. Adding the Container to the "LinkedBlockedQueue"
                    try {
                        RequestsQueue.Queue.offer(Container.build(), 100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        logger.error("There is a thread interruption error in queue access + " + ex.getMessage());
                    }

                }

                CounterCache.BidsPerSecondCounter.incrementAndGet();
            }
        }

        CounterCache.RequestsPerSecondCounter.incrementAndGet();

        chain.proceed();
    }
}
