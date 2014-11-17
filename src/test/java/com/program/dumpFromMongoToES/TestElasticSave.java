package com.program.dumpFromMongoToES;

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
import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.commons.configuration.Configuration;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.HashMap;
import java.io.IOException;
import io.searchbox.core.Index;
import io.searchbox.core.Bulk;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.client.JestResult;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;


import com.github.fakemongo.Fongo;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

@PowerMockIgnore({"javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({ElasticSave.class})
public class TestElasticSave {
    private JestClient _esClient = null;
    private Configuration _pluginconfigXml = null;
    private ElasticSave _elasticSave = null;
    private DBCursor _cur = null;
    private DBCollection _collection = null;
    private Fongo _fongo = null;
    private String _indexname = "indexname";
    private String _indextype = "indextype";
    private String _dumpType = "general";
    private List<String> _inputName = new ArrayList<String>();
    private List<String> _cutRegexName = new ArrayList<String>();
    private List<String> _resultCutName = new ArrayList<String>();
    private String regex = "\\w+";
    private String cutregex = "\\d+";


    @Before
    public void setUp() {
        _inputName.add("inputname");
        _cutRegexName.add("cutregexname");
        _resultCutName.add("resultname");
        _esClient = PowerMockito.mock(JestClient.class);
        _pluginconfigXml = PowerMockito.mock(Configuration.class);
        _fongo = new Fongo("crawler mongo server");
        _collection = _fongo.getDB(_indexname).getCollection(_indextype);
        _collection.insert(new BasicDBObject("key_one", "value_one"));
        _collection.insert(new BasicDBObject("_id", "1234567890"));
        _cur = _collection.find();
        _elasticSave = new ElasticSave(_esClient, _pluginconfigXml);
    }

    @Test
    public void testSaveToES() {
        JestResult resultSearch = new JestResult(new Gson());
        ArrayList<Index> list = new ArrayList<Index>();
        Map<String, Object> clearedMap = new HashMap<String, Object>();
        Bulk.Builder bulk = new Bulk.Builder()
                .defaultIndex(_indexname)
                .defaultType(_indextype);
        clearedMap.put("_id", "1234567890");
        clearedMap.put("key_one", "value_one");
        Object typeId = "";
        typeId = clearedMap.get("_id");
        JSONObject mapJson = new JSONObject(clearedMap);
        String esString = mapJson.toString();
        String esString = mapJson.toString();
        Index index = new Index.Builder(esString)
                                   .index(_indexname)
                                   .type(_indextype)
                                   .id(typeId + "")
                                   .build();
        list.add(index);
        PowerMockito.when(_pluginconfigXml.getList("input.field.name")).thenReturn(_inputName);
        PowerMockito.when(_pluginconfigXml.getList("cutregex.field.name")).thenReturn(_cutRegexName);
        PowerMockito.when(_pluginconfigXml.getList("cutregex.field.resultName")).thenReturn(_resultCutName);
        PowerMockito.when(_pluginconfigXml.getString("input.field(0)[@regex]")).thenReturn(regex);
        PowerMockito.when(_pluginconfigXml.getString("cutregex.field(0)[@cut]")).thenReturn(cutregex);
        try {
            PowerMockito.when(_esClient.execute(bulk.addAction(list).build())).thenReturn(resultSearch);
            _elasticSave.saveToES(_cur, _indexname, _indextype, _dumpType);
            verify(_esClient, times(1)).execute(bulk.addAction(list).build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
	
