package com.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith; 
import org.junit.Assert;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.Connection;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse; 

import com.mongodb.Mongo;

import org.jsoup.Jsoup;

/**
 
 * @author wendy926
 
 */
@PowerMockIgnore( {"javax.management.*", "javax.net.*"}) 
@RunWith(PowerMockRunner.class)
@PrepareForTest({Connection.class, Mongo.class, ConfigUtil.class, Document.class, Jsoup.class})

public class TestRebuildServlet {
    private RebuildServlet _rebuildServ = null;
    private static String mode = "local";
    private String _indexname = "yjwang";
    private Elements _element = new Elements();
    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private static Document _docElastic = PowerMockito.mock(Document.class);
    private static Connection _connect = PowerMockito.mock(Connection.class);

    @Before
    public void setup() throws ServletException, IOException {
        InitMain.init(mode);
        ConfigUtil.reloadProperties();
        _rebuildServ = new RebuildServlet(_connect);
    }
    
    @Test
    public void testdopost() throws ServletException, IOException { 
        String adress = "http://192.168.100.4/deploy/configuration/plugins";
        String adresscon = adress + "/local/";
        Document docElastic = null;
        request.addParameter("MongoDeleDbname", "MDdbname");
        request.addParameter("MongoDeleCollection", "MDcoll");
        request.addParameter("MongoCollection", "Mcoll");
        request.addParameter("MongoDbname", "Mdbname");
        request.addParameter("ElasticDeleDbname", "ESDname");
        request.addParameter("ElasticDeleCollection", "ESDcoll");
        request.addParameter("ElasticCollection", "EScoll");
        request.addParameter("ElasticDbname", "ESname");
        request.addParameter("mode", "local");
        request.addParameter("InfluxdbCollection", "Influxcoll");
        request.addParameter("InfluxDbname", "Influxname");
        request.addParameter("InfluxDeleCollection", "InfluxDcoll");
        request.addParameter("InfluxDeleDbname", "InfluxDname");
        request.addParameter("HDFSCollection", "Hdfscoll");
        request.addParameter("HDFSDbname", "Hdfsname");
        PowerMockito.mockStatic(ConfigUtil.class);
        PowerMockito.when(ConfigUtil.reloadProperties()).thenReturn(true);
        PowerMockito.when(ConfigUtil.getPropertieString("extractor.mongo.host")).thenReturn("192.168.100.10");
        PowerMockito.when(ConfigUtil.getPropertieInteger("extractor.mongo.port")).thenReturn(27017);
        PowerMockito.when(ConfigUtil.getPropertieString(
                "extractor.parser_config_base_path")).thenReturn(adress);
        PowerMockito.when(_connect.get()).thenReturn(_docElastic);
        PowerMockito.when(_docElastic.getElementsByTag("a")).thenReturn(_element);
        PowerMockito.mockStatic(Jsoup.class);
        PowerMockito.when(Jsoup.connect(adresscon)).thenReturn(_connect);
        _rebuildServ.doPost(request, response);
        verify(_connect, times(1)).get();
        verify(_docElastic, times(2)).getElementsByTag("a");
        Assert.assertEquals("text/html; charset=UTF-8", response.getContentType());
    }
}

        
