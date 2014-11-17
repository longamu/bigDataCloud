package com.program.filterUrl;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Assert;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.io.IOException;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

/**
 
 * @author wendy926

 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigUtil.class, MetricsUtil.class, RabbitMqObj.class})
public class TestCrawlerFilter {
    private static final Logger log =
            Log.getLog(TestCrawlerFilter.class.getName());
    private static String sendQueueName = "";
    private static String message = "";
    private static Channel _consumerLocalChannel = 
            PowerMockito.mock(Channel.class);
    private static Channel _producerLocalChannel = 
            PowerMockito.mock(Channel.class);
    private ConnectionFactory _factory = new ConnectionFactory();
    private static String proQname = "proQname";
    private static String conQname = "conQname";
    private String _username = "admin";
    private String _password = "admin";
    private String _queueMsg = "";
    private String _priority = "0";
    private String _sign = "00";
    private long tag = 0001;
    private Connection _connect = null;
    private CrawlerFilter _crawlerFilter = null;
    private ShardedJedis _jedis = null;
    private CrawlerConsumer _consumer = null;
    private QueueingConsumer.Delivery _delivery = null;
    private Envelope _envelope = null;
   
    @BeforeClass
    public static void setupbefore() {
        sendQueueName = "crawler.fetcher.incoming";
        message = "{\"jobid\":\"testFetcher\"," +
                  " \"retry\":\"0\"," +
                  " \"url\":\"urlfortest\"," +
                  " \"type\":\"testtype\"," +
                  " \"source\":\"testsource\"," +
                  " \"priority\":\"0\"," +
                  " \"sign\":\"00\"}";
        PowerMockito.mockStatic(ConfigUtil.class);
        PowerMockito.when(ConfigUtil.getPropertieString(
                "scheduler.request.queue")).thenReturn(conQname);
        PowerMockito.when(ConfigUtil.getPropertieString(
                "fetcher.request.queue")).thenReturn(proQname);
    }
 
    /**
     * build a local real connection and two channel mode 
     */
    @Before
    public void setup() {
        PowerMockito.mockStatic(MetricsUtil.class);
        PowerMockito.when(MetricsUtil.addCounter(
                "scheduler.receive.url.failCounter")).thenReturn(new Counter());
        PowerMockito.when(MetricsUtil.addMeter(
                "scheduler.receive.url.failMeter")).thenReturn(new Meter());
        PowerMockito.when(MetricsUtil.addCounter(
                "scheduler.receive.url.suCounter")).thenReturn(new Counter());
        PowerMockito.when(MetricsUtil.addMeter(
                "scheduler.receive.url.sucMeter")).thenReturn(new Meter());
        PowerMockito.when(MetricsUtil.addCounter(
                "scheduler.send.url.failCounter")).thenReturn(new Counter());
        PowerMockito.when(MetricsUtil.addMeter(
                "scheduler.send.url.failMeter")).thenReturn(new Meter());
        PowerMockito.when(MetricsUtil.addCounter(
                "scheduler.send.url.suCounter")).thenReturn(new Counter());
        PowerMockito.when(MetricsUtil.addMeter(
                "scheduler.send.url.suMeter")).thenReturn(new Meter());
        PowerMockito.when(MetricsUtil.addCounter(
                "scheduler.send.url.duplicated.counter")).thenReturn(new Counter());
        PowerMockito.when(MetricsUtil.addMeter(
                "scheduler.send.url.duplicated.meter")).thenReturn(new Meter());
        _jedis = PowerMockito.mock(ShardedJedis.class);
        _producerLocalChannel = PowerMockito.mock(Channel.class);
        _consumerLocalChannel = PowerMockito.mock(Channel.class);
        _consumer = PowerMockito.mock(CrawlerConsumer.class);
        _crawlerFilter =  
                new Scheduler(_producerLocalChannel, _consumerLocalChannel, 
                        _jedis, _consumer);
    }

    /**
     * receive from scheduler and then send to fetcher
     */
    @Test
    public void testRun() throws InterruptedException {
        _envelope = PowerMockito.mock(Envelope.class);
        PowerMockito.mockStatic(ConfigUtil.class);
        PowerMockito.when(ConfigUtil.getPropertieInteger(
                "crawler.rabbitmq.basicQos")).thenReturn(1);
        QueueingConsumer.Delivery delivery = 
                PowerMockito.mock(QueueingConsumer.Delivery.class);
        PowerMockito.when(_consumer.nextDelivery())
                .thenReturn(delivery)
                .thenThrow(new InterruptedException());
        PowerMockito.when(delivery.getBody()).thenReturn(message.getBytes());
        RabbitMqObj rabbitmqMessage = PowerMockito.mock(RabbitMqObj.class);
        PowerMockito.when(rabbitmqMessage.getJobId()).thenReturn("jobid");
        PowerMockito.when(rabbitmqMessage.getUrl()).thenReturn("url");
        PowerMockito.when(rabbitmqMessage.buildJSON()).thenReturn(message);
        PowerMockito.when(rabbitmqMessage.getPriority()).thenReturn("0");
        PowerMockito.when(rabbitmqMessage.getSign()).thenReturn("00");
        PowerMockito.when(delivery.getEnvelope()).thenReturn(_envelope);
        PowerMockito.when(delivery.getEnvelope().getDeliveryTag()).thenReturn(tag);
        _crawlerFilter.run();
        verify(_consumer, times(2)).nextDelivery();
        verify(delivery, times(1)).getBody();
    }

    /**
     * onRequest() can send
     */
    @Test
    public void testOnRequest() {
        PowerMockito.mockStatic(ConfigUtil.class);
        PowerMockito.when(ConfigUtil.getPropertieString(
                "scheduler.rabbitmq.exchange")).thenReturn("");
        RabbitMqObj rabbitmqMessage = PowerMockito.mock(RabbitMqObj.class);
        PowerMockito.when(rabbitmqMessage.getJobId()).thenReturn("jobid");
        PowerMockito.when(rabbitmqMessage.getUrl()).thenReturn("url");
        PowerMockito.when(rabbitmqMessage.buildJSON()).thenReturn(message);
        PowerMockito.when(rabbitmqMessage.getPriority()).thenReturn("0");
        PowerMockito.when(rabbitmqMessage.getSign()).thenReturn("00");
        _crawlerFilter.onRequest(rabbitmqMessage);
        verify(rabbitmqMessage, times(1)).getJobId();
    }
}
