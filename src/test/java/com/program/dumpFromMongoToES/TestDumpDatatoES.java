package com.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.io.IOException;

import com.github.fakemongo.Fongo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import io.searchbox.client.JestClient;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

/**
 
 * @author wendy926

 */
@PowerMockIgnore({"javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({ElasticSave.class, ElasticMappingRebuild.class})
public class TestDumpDatatoES {
    private DumpDataToES _dumpDataToES = null;
    private ElasticMappingRebuild _rebuildES = null;
    private ElasticSave _saveES = null;
    private JestClient _esClient = null;
    private Configuration _pluginconfigXml = null;
    private String _indexname = "index";
    private String _indextype = "type";
    private Mongo _mongo = null;
    private DBCollection _collection = null;
    private DBCursor _cur = null;

    @Before
    public void setup() {
        Fongo fongo = new Fongo("crawler mongo server");
        _collection = fongo.getDB(_indexname).getCollection(_indextype);
        _collection.insert(new BasicDBObject("key_one", "value_one"));
        _cur = _collection.find();
        _rebuildES = PowerMockito.mock(ElasticMappingRebuild.class);
        _saveES = PowerMockito.mock(ElasticSave.class);
        _esClient = PowerMockito.mock(JestClient.class);
        _pluginconfigXml = PowerMockito.mock(Configuration.class);
        _dumpDataToES = new DumpDataToES(_cur, _rebuildES,
                _saveES, _esClient, _pluginconfigXml, _indexname, _indextype);
    }

    @Test
    public void testdumpDataToES() {
        String dumpType = "dealtype";
        PowerMockito.when(_rebuildES.rebuild(_indexname, _indextype)).thenReturn(true);
        PowerMockito.when(_saveES.saveToES(_cur, _indexname, _indextype, dumpType)).thenReturn(true);
        _dumpDataToES.dumpDataToES(dumpType);
        verify(_rebuildES, times(1)).rebuild(_indexname, _indextype);
        verify(_saveES, times(1)).saveToES(_cur, _indexname, _indextype, dumpType);
    }
}

