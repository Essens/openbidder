/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.google.openbidder.workers;

import com.google.gson.Gson;
import com.google.openbidder.bidding.SimpleBidInterceptor;
import com.google.openbidder.queues.RequestsQueue;
import com.google.protos.adx.NetworkBid;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkhalilov
 */
public class RabbitMQWorker implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(RabbitMQWorker.class);
    
    private final String ExchangeName = "bid-requests";
    private final String QueueName = "ob-queue";
    private final String RoutingKey = "ob-key";

    private Thread Current;
    private Connection MQConnection = null;
    private Channel MQChannel = null;

    public RabbitMQWorker() {
        try {
            ConnectMQ();
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

                if (!IsOnline())
                    ConnectMQ();

                if (!IsBlocked()) {

                    while (RequestsQueue.Queue.size() > 0) {
                        //NetworkBid.BidRequest Container = RequestsQueue.Queue.take();
                        //PublishMQMessage(ToHex(Container.getId().toByteArray()), Container.toByteArray());
                    }

                    Thread.sleep(1);
                }
                
                

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
            DisconnectMQ();
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }
    
    private void ConnectMQ() throws InterruptedException {
        boolean IsKeepTrying = true;

        while(IsKeepTrying)
        {
            IsKeepTrying = false;
            logger.error("Connecting to RabbitMQ ...");
            
            try
            {
                CreateConnection();
                CreateChannel();
                CreateExchangesAndQueues();
            }
            catch(Exception ex)
            {
                logger.error(ex.getMessage());
                IsKeepTrying = true;        
                Thread.sleep(60 * 1000);
            }
        }
    }

    private void DisconnectMQ() {

        if (MQConnection != null && MQChannel != null) {
            try {
                MQChannel.close();
                MQConnection.close();
            } catch (IOException ex) {
                logger.error("Can not close connection ...");
                logger.error(ex.getMessage());
            }
        }
    }

    private void PublishMQMessage(String ID, byte[] Message) {
        try {
            MQChannel.basicPublish(ExchangeName, RoutingKey,
                    new AMQP.BasicProperties.Builder()
                    .contentType("text/plain")
                    .deliveryMode(2)
                    .priority(1)
                    .messageId(ID)
                    .build(), Message);

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    
    private boolean IsOnline()
    {
        return MQConnection.isOpen();
    }
    
    private boolean IsBlocked()
    {
        return MQChannel.flowBlocked();
    }

    private void CreateConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("koller60");
        factory.setVirtualHost("ob-host");
        factory.setHost("localhost");
        factory.setPort(5672);

        try {
            MQConnection = factory.newConnection();
        } catch (IOException ex) {
            logger.error("Connection creation failed ...");
            logger.error(ex.getMessage());
        }
    }

    private void CreateChannel() {
        try {
            MQChannel = MQConnection.createChannel();
        } catch (IOException ex) {
            logger.error("Can not create MQ channel ...");
            logger.error(ex.getMessage());
        }
    }

    private void CreateExchangesAndQueues() {

        try {
            MQChannel.exchangeDeclare(ExchangeName, "direct", true);
            MQChannel.queueDeclare(QueueName, true, false, false, null);
            MQChannel.queueBind(QueueName, ExchangeName, RoutingKey);
        } catch (IOException ex) {
            logger.error("Can not create Exchange And Queue ...");
            logger.error(ex.getMessage());
        }
    }

    private String ToHex(byte[] Value) {

        StringBuilder ReturnValue = new StringBuilder();

        for (int i = 0; i < Value.length; i++) {
            ReturnValue.append(String.format("%02X", Value[i]));
        }

        return ReturnValue.toString();
    }
    


}
