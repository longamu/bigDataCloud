package com.program.filterUrl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ConnectionFactory;

/**

 * @author wendy926

 */
final class SendFeed {
    private static Logger logger =
            Log.getLog(SendFeed.class.getName());
    private static boolean durable = true;
    private ConnectionFactory _factory = null;
    private Connection _connect = null;
    
    public SendFeed() {
        _factory = new ConnectionFactory();
        _factory.setTopologyRecoveryEnabled(true);
        _factory.setAutomaticRecoveryEnabled(true);
        _factory.setUsername(ConfigUtil.getPropertieString("crawler.rabbitmq.username"));
        _factory.setPassword(ConfigUtil.getPropertieString("crawler.rabbitmq.password"));
    }

    public SendFeed(ConnectionFactory factory) {
        _factory = factory;
    }

    /**
     * 
     * @param url
     *            url is jobid
     * @param timeOut
     *            Minute
     * @return
     */
    private void send(String queueName, String sendMessage) {
        // send a url to scheduler.incoming
        try {
            _connect = _factory.newConnection(Utils.suffle(Utils.getRabbitMQAddress()));
            Channel producerChannel = _connect.createChannel();
            producerChannel.queueDeclare(queueName, durable, false, false, null);
            producerChannel.basicPublish("", queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    sendMessage.getBytes());
            logger.debug("send" + sendMessage + "to" + queueName );
            _connect.close();
        } catch (IOException e) {
            logger.error("IOException", e);
        }
    }

    public String initUrl(String jobid, String urls, int timeOut, String type,
            String src, String mode, String priority, String sign) {
        logger.info("two mode is " + mode);
        InitMain.init(mode);
        ConfigUtil.reloadProperties();
        String rabbitMqName = 
                ConfigUtil.getPropertieString("scheduler.request.queue");
        int count = 0;
        int time = timeOut * 60;
        if (time == 0) {
            time = ConfigUtil.getPropertieInteger("scheduler.redis.key.timeout");
        }
        // rabbitMq set
        String[] urlSplit = urls.split("\n");
        String msg = "";
        for (String url : urlSplit) {
            if (url.length() > 3) {
                String jsonStr = "{" +
                                 "    \"jobid\":\"" + jobid + "\", " +
                                 "    \"retry\":\"0\", " +
                                 "    \"url\":\"" + url + "\", " +
                                 "    \"type\":\"" + type + "\", " +
                                 "    \"source\":\"" + src + "\", " +
                                 "    \"priority\":\"" + priority + "\", " +
                                 "    \"sign\":\"" + sign + "\"" +
                                 "}";
                send(rabbitMqName, jsonStr);
                logger.info("RabbitMqName:" + rabbitMqName);
                logger.info("jsonStr:" + jsonStr);
                logger.info("send Success" + urls);
                count += 1;
                logger.info(jsonStr);
                msg = "send " + count + " urls. "
                      + "Queue name: " + rabbitMqName;
            } else if (url.length() < 4) {
                msg = "URL is outght to input a real url" +
                      "which must has more than 4 letters";
            }
        }
        return msg;
    }
}



