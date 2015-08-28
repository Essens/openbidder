/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobitrans.openbidder.workers;

import mobitrans.openbidder.bidding.RandomBidInterceptor;
import mobitrans.openbidder.cache.CounterCache;
//import mobitrans.openbidder.queues.RequestsQueue;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkhalilov
 */
public class CounterTimerWorker extends TimerTask {
    
    private final Logger logger = LoggerFactory.getLogger(CounterTimerWorker.class);
    
    @Override
    public void run() {
        
        logger.error("Requests per last second: " + CounterCache.RequestsPerSecondCounter);
        logger.error("Bids per last second: " + CounterCache.BidsPerSecondCounter);
        //logger.error("Queue Size per last second: " + RequestsQueue.Queue.size());
        
        CounterCache.RequestsPerSecondCounter.set(0);
        CounterCache.BidsPerSecondCounter.set(0);        
    }
    
}
