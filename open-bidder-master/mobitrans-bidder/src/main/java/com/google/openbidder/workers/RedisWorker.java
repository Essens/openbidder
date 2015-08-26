/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.openbidder.workers;

import com.google.openbidder.containers.NativeRtbContainer;
import com.google.openbidder.queues.RequestsQueue;
import com.google.protos.adx.NetworkBid;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 *
 * @author tkhalilov
 */
public class RedisWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RedisWorker.class);

    private final String RedisHost = "130.211.95.112";
    private final int RedisPort = 6379;

    private Thread Current;
    private Jedis Client;

    public RedisWorker() {

        try {
            ConnectRedis();
        } catch (InterruptedException EObj) {
            logger.error(EObj.getMessage());
            logger.error("Thread has interrupted and terminated.");
        }
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

                try {
                    Client.ping();
                } catch (Exception Ex) {
                    logger.error("Redis is not connected, retrying connection ...");
                    ConnectRedis();
                }

                //logger.error("redis_worker:start");
                while (RequestsQueue.Queue.size() > 0) {

                    NativeRtbContainer.RequestContainer Container = RequestsQueue.Queue.poll(100, TimeUnit.MILLISECONDS);

                    if (Container != null) {
                        byte[] RequestID = Container.getId().getBytes();
                        byte[] Message = Container.toByteArray();

                        try {
                            Client.set(RequestID, Message);
                            Client.expire(RequestID, 60);
                        } catch (Exception Ex) {
                            logger.error("Redis is not connected, retrying connection ...");
                        }
                    }
                }
                //logger.error("redis_worker:done");

                Thread.sleep(1);

            }
        } catch (InterruptedException EObj) {
            logger.error(EObj.getMessage());
            logger.error("Thread has interrupted and terminated.");
        }
    }

    @Override
    public void finalize() throws Throwable {
        try {
            logger.error("Thread has been finalized.");
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }

    private void ConnectRedis() throws InterruptedException {
        boolean IsKeepTrying = true;

        while (IsKeepTrying) {
            IsKeepTrying = false;
            logger.error("Connecting to Redis ...");

            try {
                Client = new Jedis(RedisHost, RedisPort);
                Client.connect();
            } catch (Exception ex) {
                logger.error("There was an exception connecting to Redis ...");
                logger.error(ex.getMessage());
                IsKeepTrying = true;
                Thread.sleep(60 * 1000);
            }
        }
    }
}
