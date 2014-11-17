package com.program.filterUrl.impl;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.Logger;

/**

 * @author wendy926

 */

public abstract class SuChannel extends BasicOperateMQ {
    private static final Logger log = 
            Log.getLog(SuChannel.class.getName());
    
    public SuChannel(String consumerQueueName, String producerQueueName) {
        try {
            _factory = new ConnectionFactory();
            _factory.setTopologyRecoveryEnabled(true);
            _factory.setAutomaticRecoveryEnabled(true);
            _factory.setUsername(ConfigUtil.getPropertieString("crawler.rabbitmq.username"));
            _factory.setPassword(ConfigUtil.getPropertieString("crawler.rabbitmq.password"));
            _connection = _factory.newConnection(Utils.suffle(Utils.getRabbitMQAddress()));
            this._consumerChannel = _connection.createChannel();
            this._producerChannel = _connection.createChannel();
            super.setConsumerConfig(consumerQueueName);
        } catch (IOException e) {
            log.error("ioexception", e);
        }
    }

    public SuChannel(Channel productor, Channel consumer, 
            CrawlerConsumer crawlerConsumer) {
        super(productor, consumer);
        _consumer = crawlerConsumer;
    }

    protected abstract void onRequest(RabbitMqObj rabbitmqMessage);

    /**

     *   BaseChannel's abstruct method

     */
    protected void setBasicQos() {
        try {
            _consumerChannel.basicQos(
                    ConfigUtil.getPropertieInteger("crawler.rabbitmq.basicQos"));
        } catch (IOException e) {
            log.error("exception", e);
        }
    }
  
    protected void receive(String recvQueue) {
        while (true) {
            try {
                _delivery = _consumer.nextDelivery();
                _queueMsg = new String(_delivery.getBody());  // Retrieve the message body. 
                _rabbitmqMessage = RabbitMqObj.getObject(_queueMsg);  // turn urlmessage json to gson
                log.info("receive success");
                if (_rabbitmqMessage == null) {  // if not receive urlmessage from scheduler
                    log.error("the message of [" + _queueMsg + "] is illeagle");
                } else {
                    this.onRequest(_rabbitmqMessage);  // msg receive from scheduler then send to fetcher
                }
            } catch (InterruptedException e) {
                log.error(e);
                break;
            } finally {
                this.ack(_delivery.getEnvelope().getDeliveryTag());
            }
        }
    } 
   
    @Override
    protected void close() {
        super.close();
        try {
            if (_connection != null && _connection.isOpen()) {
                try {
                    _connection.close();
                } catch (AlreadyClosedException e) {
                    log.error("connection already closed.", e);
                }
            }
        } catch (IOException e) {
            log.error("ioexception", e);
        }
    }
}

