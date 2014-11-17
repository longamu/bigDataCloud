package com.program;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Address;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times; 
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

/**

 * @author wendy926
 
 */
@RunWith(PowerMockRunner.class)
public class TestSendFeed {
    private static Logger logger =
            Log.getLog(TestSendFeed.class.getName());
    private static Class<GiveUrl> reflect = GiveUrl.class;
    private List<Address> _addrList = null;
    private Connection _connect_mock;
    private ConnectionFactory _factory_mock;
    private GiveUrl _giveUrl;
    private Channel _producerChannel;
    private String _queueName;
    private String _sendMessage;
    private Address[] _addrArr;

    @Before
    public void setup() {
        _addrList = new ArrayList<Address>();
        _addrList.add(new Address("192.168.100.11", 5672));
        _addrArr = new Address[_addrList.size()];
        _addrList.toArray(_addrArr);
        _queueName = "scheduler test queue";
        _sendMessage = "a test case";
        _factory_mock = 
            PowerMockito.mock(ConnectionFactory.class);  
        _giveUrl = new SendFeed(_factory_mock);
        _producerChannel =
            PowerMockito.mock(Channel.class);
        _connect_mock =
            PowerMockito.mock(Connection.class);
    }

    @Test
    public void testSend() throws NoSuchMethodException, IOException, 
            IllegalAccessException, InvocationTargetException {
        Method method = 
                reflect.getDeclaredMethod("send", 
                                          String.class, 
                                          String.class);
        method.setAccessible(true);
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getRabbitMQAddress()).thenReturn(_addrArr);
        PowerMockito.when(Utils.suffle(Utils.getRabbitMQAddress())).thenReturn(_addrArr);
        PowerMockito.when(
                _factory_mock.newConnection(Utils.suffle(Utils.getRabbitMQAddress()))).thenReturn(_connect_mock);
        PowerMockito.when(
                _connect_mock.createChannel()).thenReturn(_producerChannel);
        method.invoke(_giveUrl, _queueName, _sendMessage);
        verify(_connect_mock, times(1)).createChannel();
        verify(_producerChannel,times(1)).queueDeclare(_queueName, true, false, false, null);
        verify(_producerChannel,times(1)).basicPublish("", _queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    _sendMessage.getBytes());
    }

    @Test
    public void testInitUrl() throws IOException {  // deliver real connection
        String mode = "local";
        String jobid = "01";
        String urls = "http://www.testcase.com";
        int timeout = 0;
        String type = "testcase_type";
        String src = "testcase_db";
        String priority = "00";
        String sign = "00";
        PowerMockito.mockStatic(InitMain.class);
        PowerMockito.mockStatic(ConfigUtil.class);
        PowerMockito.when(ConfigUtil.reloadProperties()).thenReturn(true);
        PowerMockito.when(ConfigUtil.getPropertieString(
                "scheduler.request.queue")).thenReturn("test queue");
        PowerMockito.when(ConfigUtil.getPropertieInteger(
                "scheduler.redis.key.timeout")).thenReturn(2520);
        PowerMockito.mockStatic(Utils.class);
        PowerMockito.when(Utils.getRabbitMQAddress()).thenReturn(_addrArr);
        PowerMockito.when(Utils.suffle(Utils.getRabbitMQAddress())).thenReturn(_addrArr);
        PowerMockito.when(
                _factory_mock.newConnection(Utils.suffle(Utils.getRabbitMQAddress()))).thenReturn(_connect_mock);
        PowerMockito.when(
                _connect_mock.createChannel()).thenReturn(_producerChannel);
        _giveUrl.initUrl(jobid, urls, timeout, type, src, 
                mode, priority, sign);
        verify(_connect_mock, times(1)).createChannel();
    }
}
