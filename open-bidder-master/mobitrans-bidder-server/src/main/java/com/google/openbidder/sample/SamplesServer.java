package com.google.openbidder.sample;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import com.google.openbidder.bigquery.BigQueryModule;
import com.google.openbidder.binary.BidderServer;
import com.google.openbidder.cloudstorage.GoogleCloudStorageModule;
import com.google.openbidder.deals.PreferredDealsModule;
import com.google.openbidder.exchange.doubleclick.DoubleClickModule;
import com.google.openbidder.exchange.doubleclick.impression.DoubleClickImpressionModule;
import com.google.openbidder.exchange.doubleclick.interceptor.DoubleClickInterceptorsModule;
import com.google.openbidder.exchange.doubleclick.match.DoubleClickMatchModule;
import com.google.openbidder.metrics.reporter.bigquery.BigQueryMetricsReporterModule;
import com.google.openbidder.remarketing.services.RemarketingModule;
import com.google.openbidder.storage.guice.StorageModule;

/**
 * Main program for samples deployment.
 */
public class SamplesServer extends BidderServer {

    public SamplesServer(String[] args) {
        super(args);
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            args = new String[9];
            args[0] = "--api_project_id=openbid1";
            args[1] = "--api_project_number=549127527448";
            args[2] = "--service_account_id=549127527448-23e5rqafu4lqeuh740r91b12pb696nro@developer.gserviceaccount.com";
            args[3] = "--p12_file_path=T:\\Open-Bidder\\google-open-bidder-trial\\bin\\bidder.p12";
            args[4] = "--bid_interceptors=mobitrans.openbidder.bidding.RandomBidInterceptor";
            args[5] = "--impression_interceptors=com.google.openbidder.impression.SimpleImpressionInterceptor ";
            args[6] = "--listen_port=18081";
            args[7] = "--load_balancer_port=18080";
            args[8] = "--admin_port=18082";
        }

        new SamplesServer(args).main();
    }

    /**
     * CHANGE to load extra modules from your app, if any; or additional
     * "extension" modules from openbidder (those not added by
     * {@link BidderServer}). If you don't need any extra modules, delete this
     * method, or even this class, using {@link BidderServer} as your main
     * class.
     */
    @Override
    protected ImmutableList<Module> getModules() {
        return ImmutableList.<Module>builder()
                .addAll(super.getModules())
                // DoubleClick connector
                .add(new DoubleClickModule())
                .add(new DoubleClickMatchModule())
                .add(new DoubleClickImpressionModule())
                .add(new DoubleClickInterceptorsModule())
                // Application-level modules (interceptors) and optional features
                .add(new GoogleCloudStorageModule())
                .add(new StorageModule())
                .add(new RemarketingModule())
                .add(new BigQueryModule())
                .add(new BigQueryMetricsReporterModule())
                .add(new PreferredDealsModule())
                //.add(new SamplesModule())
                //.add(new WeatherModule())
                .build();
    }
}
