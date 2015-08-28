/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mobitrans.openbidder.workers;

import mobitrans.openbidder.bidding.RandomBidInterceptor;
import mobitrans.openbidder.cache.ConfigsCache;
import mobitrans.openbidder.cache.CreativesCache;
import mobitrans.openbidder.data.bidding.Config;
import mobitrans.openbidder.data.bidding.Creative;
import mobitrans.openbidder.dataAccessLayer.DataAccess;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkhalilov
 */
public class ConfigWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ConfigWorker.class);
    private DataAccess DataAccessManager;
    private Thread Current;

    public ConfigWorker() {
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
                List<Config> Configs = DataAccessManager.GetConfigs();

                // 2. Updating the cache with the new rules fetched (while locking access to the object)
                synchronized (ConfigsCache.Cache) {
                    ConfigsCache.Cache.clear();
                    ConfigsCache.Cache.addAll(Configs);
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
