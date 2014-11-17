package com.program.dumpFromMongoToES;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import io.searchbox.indices.IndicesExists;
import io.searchbox.indices.mapping.PutMapping;
import io.searchbox.indices.aliases.GetAliases;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Index;
import io.searchbox.core.Delete;

import com.program.util.ConfigUtil;
import com.program.util.Log;

/**
 
 * @author wendy926

 */
/**
 * rebuild a ES mapping with input indexname and indextype
 * read output of the xml
 */
final class ElasticMappingRebuild {
    private static Logger logger =
            Log.getLog(ElasticMappingRebuild.class.getName());
    private JestClient _esClient = null;
    private Configuration _pluginconfigXml = null;

    // already connect to ES
    public ElasticMappingRebuild(JestClient esClient, Configuration pluginconfigXml) {
        _pluginconfigXml = pluginconfigXml;
        _esClient = esClient;
    }

    /**
     * change the field name 
     */
    public boolean rebuild(String indexName, String indexType) {
        boolean ifdelete = delete(indexName, indexType);
        logger.info("ifdelete " + ifdelete);
        logger.info("firstly delete indexname " + indexName + " indexType " + indexType);
        List<String> pluginName = _pluginconfigXml.getList("output.field.name");
        List<String> pluginType = _pluginconfigXml.getList("output.field.resultType");
        List<String> pluginAnalysis = _pluginconfigXml.getList("output.field.analysis");
        XContentBuilder temp = null;
        String msg = "";
        String esString = null;
        try {
            IndicesExists indicesExists =
                    new IndicesExists.Builder(indexName).build();
            JestResult result = _esClient.execute(indicesExists);
            if (!result.isSucceeded()) {
                _esClient.execute(new CreateIndex.Builder(indexName).build());
            }
        } catch (Exception e) {  // unreported exception Exception
            logger.info(indexName + ":" + "created error");
        }
        Map<String, String> typeMap = new HashMap<String, String>();
        Map<String, String> analysisMap = new HashMap<String, String>();
        int nameLength = pluginName.size();
        for (int i = 0; i < nameLength; i++) {
            typeMap.put(pluginName.get(i), pluginType.get(i));
            analysisMap.put(pluginName.get(i), pluginAnalysis.get(i));
        }
        // get real mapping, json type
        XContentBuilder mapping = getMapping(typeMap, analysisMap, indexType);
        try {
            esString = mapping.string();
            PutMapping putMapping = new PutMapping.Builder(indexName,
                                                           indexType,
                                                           esString)
                                                  .build();
            _esClient.execute(putMapping);
            logger.info(indexName + ":" + indexType + ":" + "mapping created");
            msg = indexName + ":" + indexType + ":" + "mapping creaded";
        } catch (Exception e) {  // unreported exception Exception
            logger.info(indexName + ":" + indexType + ":"
                    + "mapping creaded error");
            msg = indexName + ":" + indexType + ":" + "mapping creaded error";
        }
        boolean flag = false;
        if (flag == true) {
           this._esClient.shutdownClient();
           flag = true;
        }
        return true;
    }


    private boolean delete(String indexDelete, String indexType) {
        String msg = "";
        try {
            if (indexType == "" || indexType == null) {
                _esClient.execute(new Delete.Builder(indexDelete).build());
                logger.info(indexDelete + ":deleted");
                msg = indexDelete + ":deleted";
            } else {
                _esClient.execute(new Delete.Builder(indexDelete)
                                            .type(indexType)
                                            .build());
                logger.info(indexDelete + ":" + indexType + ":deleted");
                msg = indexDelete + ":" + indexType + ":deleted";
            }
        } catch (Exception e) {  // unreported exception Exception
            logger.info(indexDelete + ":" + ":deleted error");
        }
        boolean flag = false;
        if (flag == true) {
           this._esClient.shutdownClient();
           flag = true;
        }
        return true;
    }

    private XContentBuilder getMapping(Map<String, String> typeMap,
            Map<String, String> analysisMap, String indexType) {
        XContentBuilder mapping = null;
        XContentBuilder temp = null;
        try {
            temp = XContentFactory.jsonBuilder().startObject()
                    .startObject(indexType).startObject("properties");
        } catch (IOException e) {
            logger.error("mapping init error ", e);
        }
        Iterator<Map.Entry<String, String>> iter = typeMap.entrySet()
                .iterator();
        int mapSize = typeMap.size();
        int analysisSize = analysisMap.size();
        if (mapSize != analysisSize) {
            logger.error("config of mapping is illegal");
        }
        // create a real mapping
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            String resultType = entry.getValue();
            String name = entry.getKey();
            String analysisTag = analysisMap.get(name);
            if (resultType.equals("String") || resultType.equals("arraylist")) {
                try {
                    // change the analysis type later
                    if (analysisTag.equals("false")) {
                        temp = temp.startObject(name)
                                   .field("index", "not_analyzed")
                                   .field("type", "string").endObject();
                    } else {
                        temp = temp.startObject(name)
                                   .field("type", "string")
                                   .field("store", "yes")
                                   .field("analyzer", analysisTag)
                                   .endObject();
                    }
                } catch (IOException e) {
                    logger.error("mapping string or arraylist error ", e);
                }
            } else {
                try {
                    temp = temp.startObject(name)
                               .field("index", "not_analyzed")
                               .field("type", resultType).endObject();
                } catch (IOException e) {
                    logger.error("mapping type error ", e);
                }
            }
        }

        try {
            mapping = temp.endObject().endObject().endObject();
        } catch (IOException e) {
            logger.info("mapping  end error ", e);
        }
        return mapping;
    }
}

