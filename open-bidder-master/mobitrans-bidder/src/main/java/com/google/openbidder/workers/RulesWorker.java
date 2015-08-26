/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.openbidder.workers;

import com.google.openbidder.bidding.SimpleBidInterceptor;
import com.google.openbidder.cache.RulesCache;
import com.google.openbidder.data.bidding.Rule;
import com.google.openbidder.dataAccessLayer.DataAccess;
import com.google.openbidder.db.DatabaseManager;
import com.google.openbidder.queues.RequestsQueue;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkhalilov
 */
public class RulesWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RulesWorker.class);
    private DataAccess DataAccessManager;
    private Thread Current;

    public RulesWorker() {
        DataAccessManager = new DataAccess();
    }
    
    public void start() {
        if (Current == null) {
            Current = new Thread(this);
            Current.start();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                
                // 1. Fetching all the rules from the Database
                List<Rule> Rules = DataAccessManager.GetRules();
                
                // 2. Updating the cache with the new rules fetched (while locking access to the object)
                synchronized(RulesCache.Cache)
                {
                    RulesCache.Cache.clear();
                    RulesCache.Cache.addAll(Rules);
                }
                
                Thread.sleep(60000);
            }
        } catch (Exception Ex) {
            logger.error(Ex.getMessage());
            logger.error("Thread has interrupted and terminated.");
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            logger.error("Thread has been finalized.");
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }
}
