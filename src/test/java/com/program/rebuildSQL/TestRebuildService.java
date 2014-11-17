package com.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 
 * @author wendy926

 */
@PowerMockIgnore( {"javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({MongoDBRebuild.class, InfluxdbRebuild.class, ElasticsearchRebuild.class, Jedis.class})

final class TestRebuildService {
    private RebuildService _rebuildService = null;
    private MongoDBRebuild _mongodbRebuild = null;
    private ElasticsearchRebuild _elasticRebuild = null;
    private String _type1 = "mongoDB";
    private String _type2 = "elasticSearch";
    private String _website = "dbname";
    private String _tableName = "tablename";
    private StringBuilder _debugInfo = null;
    private String _dbName = "dbname";
    private Configuration _pluginconfigXml = null;
    private Configuration _mappingES = null;
    
    @Before 
    public void setUp() {
        _mongodbRebuild = PowerMockito.mock(MongoDBRebuild.class);
        _elasticRebuild = PowerMockito.mock(ElasticsearchRebuild.class);
        _debugInfo = PowerMockito.mock(StringBuilder.class);
        _pluginconfigXml = PowerMockito.mock(Configuration.class);
        _mappingES = PowerMockito.mock(Configuration.class);
        _rebuildService = new RebuildService(_mongodbRebuild, _elasticRebuild, _debugInfo, _pluginconfigXml, _mappingES);
    }

    @Test
    public void testDelete() {
        _rebuildService.delete(_type1, _website, _tableName);
        verify(_mongodbRebuild, times(1)).delete(_dbName, _tableName, _debugInfo);
        _rebuildService.delete(_type2, _website, _tableName);
        verify(_elasticRebuild, times(1)).delete(_dbName, _tableName, _debugInfo);
    }
   
    @Test
    public void testRebuildMongo() {
        String index = "index";
        String shading = "shading"; 
        PowerMockito.when(_pluginconfigXml.getString("db.index")).thenReturn(index);
        PowerMockito.when(_pluginconfigXml.getString("db.shading")).thenReturn(shading);
        _rebuildService.rebuildMongo(_dbName, _tableName);
        verify(_mongodbRebuild, times(1)).createCollection(_dbName, _tableName, index, shading,
                _debugInfo);
    }

    @Test
    public void testRebuildES() {
        List pluginName = new ArrayList<String>() {{add("pluginName");}};
        List pluginType = new ArrayList<String>() {{add("pluginType");}};
        List pluginAnalysis = new ArrayList<String>() {{add("pluginAnalysis");}};
        List mappingName = new ArrayList<String>() {{add("mappingName");}};
        List mappingType = new ArrayList<String>() {{add("mappingType");}};
        List mappingAnalysis = new ArrayList<String>() {{add("mappingAnalysis");}};
        int i = 0;
        PowerMockito.when(_pluginconfigXml.getList("field.name")).thenReturn(pluginName);
        PowerMockito.when(_pluginconfigXml.getList("field.resultType")).thenReturn(pluginType);
        PowerMockito.when(_pluginconfigXml.getList("field.analysis")).thenReturn(pluginAnalysis);
        PowerMockito.when(_mappingES.getList("field.name")).thenReturn(mappingName);
        PowerMockito.when(_mappingES.getList("field.resultType")).thenReturn(mappingType);
        PowerMockito.when(_mappingES.getList("field.analysis")).thenReturn(mappingAnalysis);
        _rebuildService.rebuildElastic(_dbName, _tableName);
        verify(_elasticRebuild, times(1)).createMapping(_dbName, _tableName, pluginName, pluginType,
                pluginAnalysis, _debugInfo);
    }
}
