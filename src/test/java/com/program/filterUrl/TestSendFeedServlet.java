package com.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;  

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Assert;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 
 * @author wendy926
 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({})
public class TestSendFeedServlet {
    private static Logger logger =
            Log.getLog(TestSendFeedServlet.class.getName());
    private static SendFeed giveurl = null;
    private static SendFeedServlet servlet = null;
    private static MockHttpServletRequest request = null;
    private static MockHttpServletResponse response = null;
    private int _time = 0;
    private int length = 0;
    private String _jobId = "1413197931490";
    private String _url = "http://localhost:9200";
    private String _type = "yjwang";
    private String _src = "yjwang";
    private String _mode = "local";
    private String _priority = "4";
    private String _sign = "4";
    private String _timeOut = "0";
  
    @BeforeClass
    public static void setup() {
        InitMain.init("local");
        giveurl = PowerMockito.mock(GiveUrl.class);
        servlet = new GiveUrlServlet(giveurl);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void testServlet() throws ServletException, IOException {
        request.setMethod("POST");
        request.addParameter("JobId", "1413197931490");
        request.addParameter("TimeOut", "0");
        request.addParameter("type", "yjwang");
        request.addParameter("mode", "local");
        request.addParameter("url", "http://localhost:9200");
        request.addParameter("source", "yjwang");
        request.addParameter("priority", "4");
        request.addParameter("sign", "4");
        PowerMockito.mockStatic(ConfigUtil.class);
        PowerMockito.when(ConfigUtil.getPropertieInteger(
                "fetcher.rabbitmq.level.number")).thenReturn(4);
        PowerMockito.when(ConfigUtil.getPropertieInteger(
                "fetcher.rabbitmq.gradecount.number")).thenReturn(4);
        servlet.doPost(request, response);
        verify(giveurl, times(1)).initUrl(_jobId, _url, _time, _type,
                _src, _mode, _priority, _sign);
        Assert.assertEquals("text/html; charset=UTF-8", response.getContentType());
    }
}
