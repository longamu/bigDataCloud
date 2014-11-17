package com.program.filterUrl.impl;

import com.rabbitmq.client.Address;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.json.JSONException;

/**

 * @author wendy926

 */

abstract class BasicOperateMQ {
    private static final Logger log = 
            Log.getLog(BasicOperateMQ.class.getName()); 
    private static final boolean durable = true;  // set persistent
    private static final boolean autoAck = false; 
    protected QueueingConsumer.Delivery _delivery = null;
    protected Channel _consumerChannel = null;
    protected Channel _producerChannel = null;
    protected CrawlerConsumer _consumer = null;
    protected String _queueMsg = "";
    protected ConnectionFactory _factory = null;
    protected Connection _connection = null;
    protected RabbitMqObj _rabbitmqMessage = null;
  
    public BasicOperateMQ() {
    }

    /**

     * this constructor is for test

     */
    public BasicOperateMQ(Channel productor, Channel consumer) {
        _producerChannel = productor;
        _consumerChannel = consumer;
    }

    /**

     * acknowledge some received msg

     */
    protected void ack(long deliveryTag) {
        try {
            _consumerChannel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.error("ack error...", e);
        }
    }

    protected String getQueueMsg() {
        return _queueMsg;
    }

    protected void setConsumerConfig(String qname) {
        try {
            _consumerChannel.queueDeclare(qname, durable, false, false, null);
            this.setBasicQos();
            _consumer = new CrawlerConsumer(_consumerChannel);
            _consumerChannel.basicConsume(qname, autoAck, _consumer);
        } catch (IOException e) {
            log.error("initialize error...", e);
        }
    }

    protected abstract void setBasicQos();
 
    protected void send(String exchange, String queueName, String sendMessage) {
        try {
            if (!(exchange == null || exchange.equals(""))) {
                _producerChannel.exchangeDeclare(exchange, "direct");
                _producerChannel.queueBind(queueName, exchange, queueName);
            }
            _producerChannel.queueDeclare(queueName, durable, false, false, null);
            _producerChannel.basicPublish(exchange, queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    sendMessage.getBytes());
            log.debug("send " + sendMessage + " to " + queueName );
        } catch (IOException e) {
            log.error("IOException", e);
        }
    }

    protected void send(String queueName, String sendMessage) {
        this.send("", queueName, sendMessage);
    }

    protected void close() {
        try {
            if (_consumerChannel != null && _consumerChannel.isOpen()) {
                try {
                    _consumerChannel.close();
                } catch (AlreadyClosedException e) {
                    log.error("_consumer channel already closed.", e);
                }
            }
            if (_producerChannel != null && _producerChannel.isOpen()) {
                try {
                    _producerChannel.close();
                } catch (AlreadyClosedException e) {
                    log.error("producer channel already closed.", e);
                }
            }
        } catch (IOException e) {
            log.error("ioexception", e);
        }
    }
}

