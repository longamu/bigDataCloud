package com.program.dumpFromMongoToES;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.mapping.GetMapping;
import io.searchbox.indices.aliases.GetAliases;
import io.searchbox.core.Index;
import io.searchbox.core.Update;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import com.program.util.Log;

/**
 
 * @author wendy926

 */
final class ElasticSave {
    private static Logger logger =
            Log.getLog(ElasticSave.class.getName());
    private static final int BULK_NUM = 500;
    private static final String SEARCH_FOR_GENERAL = "general";
    private static final String SEARCH_FOR_TEXT = "text";
    private JestClient _esClient = null;
    private Configuration _pluginconfigXml = null;
    private int pages = 0;


    public ElasticSave(JestClient esClient, Configuration pluginconfigXml) {
        _esClient = esClient;
        _pluginconfigXml = pluginconfigXml;
    }

    public boolean saveToES(DBCursor cur, String indexname, String indextype, String dumpType) {
        int count = 0;
        ArrayList<Index> list = new ArrayList<Index>();
        JSONObject clearedMap = null;
        Bulk.Builder bulk = new Bulk.Builder()
                .defaultIndex(indexname)
                .defaultType(indextype);
        while (cur.hasNext()) {
            BasicDBObject dbObject = (BasicDBObject)cur.next();
            Map<String, Object> mapSource = dbObject.toMap();
            logger.info("count =  " + count++);
            if (dumpType.equals(SEARCH_FOR_GENERAL)) {
                clearedMap = cleanGeneral(mapSource);
            }
            String esString = null;
            Object typeId = "";
            typeId = clearedMap.get("_id");
            esString = clearedMap.toString();
            Index index = new Index.Builder(esString)
                                   .index(indexname)
                                   .type(indextype)
                                   .id(typeId + "")
                                   .build();
            list.add(index);
            if (list.size() == BULK_NUM) {
                try {
                    JestResult resultSearch = _esClient
                                             //.setTimeout(TimeValue.timeValueHours(3))
                                             .execute(bulk.addAction(list).build());
                } catch (Exception e) {
                    logger.info("unreported exception Exception", e);  // unreported exception Exception
                }
                list = new ArrayList<Index>();
                bulk = new Bulk.Builder()
                .defaultIndex(indexname)
                .defaultType(indextype);
            }
        }
        if (list.size() > 0 && list.size() <= BULK_NUM)  {
            try {
                JestResult resultSearch = _esClient.execute(bulk.addAction(list).build());
            } catch (Exception e) {
                logger.info("unreported exception Exception", e);  // unreported exception Exception
            }
        }
        return true;
    }

    // for better extends
    private JSONObject cleanGeneral(Map<String, Object> mapSource) {
        Map<String, Object> clearedMap = cleanMapping(mapSource);
        JSONObject mapJson = new JSONObject(clearedMap);
        return mapJson;
    }

    private Map<String, Object> cleanMapping(Map<String, Object> mapSource) {
        // for loop to test which to reguler and which to cut 
        List<String> inputName = _pluginconfigXml.getList("input.field.name");
        List<String> cutRegexName = _pluginconfigXml.getList("cutregex.field.name");
        List<String> resultCutName = _pluginconfigXml.getList("cutregex.field.resultName");
        String outputString = "";
        int inputlength = inputName.size();
        int cutlength = cutRegexName.size();
        // deal with no cut regex
        for (int i = 0; i < inputlength; i++) {
            String regex =
                        _pluginconfigXml.getString("input.field(" + i + ")[@regex]");
            String xmllabel = inputName.get(i);
            Pattern p = Pattern.compile(regex);
            String matchString = (String)mapSource.get(xmllabel);
            if (matchString == null) {
                continue;
            }
            Matcher m = p.matcher(matchString);
            while (m.find()) {
                outputString = m.group();
            }
            mapSource.put(xmllabel, outputString);
        }
        for (int i = 0; i < cutlength; ++i) {
            String cutregex =
                    _pluginconfigXml.getString("cutregex.field(" + i + ")[@cut]");
            String xmlresultlabel = resultCutName.get(i);
            String xmllabel = cutRegexName.get(i);
            Pattern p = Pattern.compile(cutregex);
            String matchString = (String)mapSource.get(xmllabel);
            if (matchString == null) {
                continue;
            }
            Matcher m = p.matcher(matchString);
            while (m.find()) {
                outputString = m.group();
            }
            mapSource.put(xmlresultlabel, outputString);
        }
        return mapSource;
    }
}

