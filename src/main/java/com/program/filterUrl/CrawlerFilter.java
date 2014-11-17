package com.program.filterUrl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.rabbitmq.client.Channel;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

/**

 * @author wendy926

 */
final class CrawlerFilter extends CommonQueueService {
    private static Logger logger = 
             Log.getLog(CrawlerFilter.class.getName());
    private static String consumerQueueName = 
            ConfigUtil.getPropertieString("scheduler.request.queue");
    private static String producerQueueName = 
            ConfigUtil.getPropertieString("fetcher.request.queue");
    private static ShardedJedis jedis = null;
    private static JedisPoolConfig config = null;
    private Counter _sendUrlsuCounter = null;
    private Counter _sendUrlFailCounter = null;
    private Counter _receivesuCounter = null;
    private Counter _receiveFailCounter = null;
    private Counter _sendUrlDupCounter = null;
    private Meter _sendUrlsucMeter = null;
    private Meter _sendUrlFailMeter = null;
    private Meter _receivesucMeter = null;
    private Meter _receiveFailMeter = null;
    private Meter _sendUrlDupMeter = null;

    public CrawlerFilter() {
        super(consumerQueueName, producerQueueName);
        config = new JedisPoolConfig();
        config.setMaxActive(ConfigUtil
                .getPropertieInteger("scheduler.redis.MaxActive"));
        config.setMaxIdle(ConfigUtil.getPropertieInteger("scheduler.redis.MaxIdle"));
        config.setMaxWait(ConfigUtil.getPropertieInteger("scheduler.redis.MaxWait"));
        config.setTestOnBorrow(ConfigUtil
                .getPropertieBoolean("scheduler.redis.setTestOnBorrow"));
        List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>();
        shards.add(new JedisShardInfo(ConfigUtil.getPropertieString("scheduler.redis.ip"),
                ConfigUtil.getPropertieInteger("scheduler.redis.port"), "master"));
        ShardedJedisPool shardedJedisPool = new ShardedJedisPool(config, shards);
        jedis = shardedJedisPool.getResource();
        MetricsUtil.getCPUMemoryUsage(
                this.getClass().getSimpleName().toLowerCase());
        _sendUrlsuCounter =
                MetricsUtil.addCounter("scheduler.send.url.suCounter");
        _sendUrlFailCounter =
                MetricsUtil.addCounter("scheduler.send.url.failCounter");
        _sendUrlsucMeter = MetricsUtil.addMeter("scheduler.send.url.suMeter");
        _sendUrlFailMeter = MetricsUtil.addMeter("scheduler.send.url.failMeter");
        _sendUrlDupMeter =
                MetricsUtil.addMeter("scheduler.send.url.duplicated.meter");
        _sendUrlDupCounter = 
                MetricsUtil.addCounter("scheduler.send.url.duplicated.counter");
        _receivesuCounter =
                MetricsUtil.addCounter("scheduler.receive.url.suCounter");
        _receiveFailCounter =
                MetricsUtil.addCounter("scheduler.receive.url.failCounter");
        _receivesucMeter =
                MetricsUtil.addMeter("scheduler.receive.url.sucMeter");
        _receiveFailMeter =
                MetricsUtil.addMeter("scheduler.receive.url.failMeter");
    }
 
    /**

     * this is for test

     */
    public CrawlerFilter(Channel productor, Channel consumer, ShardedJedis jedis, 
            CrawlerConsumer crawlerConsumer) {
        super(productor, consumer, crawlerConsumer);
        MetricsUtil.getCPUMemoryUsage(
                this.getClass().getSimpleName().toLowerCase());
        _sendUrlsuCounter =
                MetricsUtil.addCounter("scheduler.send.url.suCounter");
        _sendUrlFailCounter =
                MetricsUtil.addCounter("scheduler.send.url.failCounter");
        _sendUrlsucMeter = MetricsUtil.addMeter("scheduler.send.url.suMeter");
        _sendUrlFailMeter = MetricsUtil.addMeter("scheduler.send.url.failMeter");
        _sendUrlDupMeter =
                MetricsUtil.addMeter("scheduler.send.url.duplicated.meter");
        _sendUrlDupCounter =
                MetricsUtil.addCounter("scheduler.send.url.duplicated.counter");
        _receivesuCounter =
                MetricsUtil.addCounter("scheduler.receive.url.suCounter");
        _receiveFailCounter =
                MetricsUtil.addCounter("scheduler.receive.url.failCounter");
        _receivesucMeter =
                MetricsUtil.addMeter("scheduler.receive.url.sucMeter");
        _receiveFailMeter =
                MetricsUtil.addMeter("scheduler.receive.url.failMeter");
        this.jedis = jedis;
    }

    /**

     * send to ******

     */
    public void onRequest(RabbitMqObj rabbitmqMessage) {
        String jobid = rabbitmqMessage.getJobId();
        if (jobid == null || jobid == "") {
            return;
        }
        if (jedis.sadd(jobid, "") == 1) {  // if jobid exists then not add and return 0
            jedis.expire(jobid, 1);  // if content is empty then expire it in one sec.
            _sendUrlFailCounter.inc();  // it's a fail url content, add count
            _sendUrlFailMeter.mark();
            return;
        }
        String url = rabbitmqMessage.getUrl();
        if (url == null || url == "") {
            return;
        }
        String message = rabbitmqMessage.buildJSON();
        if (jedis.sadd(jobid, url) == 1) {
            String priority = rabbitmqMessage.getPriority();
            String sign = rabbitmqMessage.getSign();
            String exchange = ConfigUtil.getPropertieString(
                    "scheduler.rabbitmq.exchange");
            String sendQueueName = producerQueueName + "._P" + priority + "._C" +
                   String.format("%02d", Integer.parseInt(sign));
            send(exchange, sendQueueName, message);
            logger.info("send success");
            _sendUrlsuCounter.inc();
            _sendUrlsucMeter.mark();
        } else {
            _sendUrlDupCounter.inc();  // record duplicate urls
            _sendUrlDupMeter.mark();
        }
    }

    /**

     *   there are duplicate urls need to filter through redis
     *   run() is the main method in CrawlerFilter

     */
    public void run() {
        try {
            receive(consumerQueueName);
            logger.info("receive success");
        } catch (RuntimeException e) {
            logger.error("receive url error", e);
            _receiveFailCounter.inc();
            _receiveFailMeter.mark();
        }
        _receivesuCounter.inc();
        _receivesucMeter.mark();
    }
}

