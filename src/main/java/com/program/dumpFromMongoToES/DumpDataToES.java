package com.program.dumpFromMongoToES;

import com.program.ElasticMappingRebuild;
import com.program.ElasticSave;
import com.util.ConfigUtil;
import com.util.Log;
import com.util.InitMain;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.searchbox.core.Bulk;
import io.searchbox.core.Bulk.Builder;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;

/**
 
 * @author wendy926

 */
final class DumpDataToES {
    private Mongo _mongo = null;
    private String _indexname = "";
    private String _indextype = "";
    private ElasticMappingRebuild _rebuildES = null;
    private ElasticSave _saveES = null;
    private JestClientFactory _factory = null;
    private JestClient _esClient = null;
    private Configuration _pluginconfigXml = null;
    private String _esAddress = "";
    private String _configPath_plugin = "";
    private DBCollection _collection = null;
    private DBCursor _cur = null;
    private static Logger log = Log.getLog(DumpDataToES.class.getName());

    /**
     * connect to ES and Mongodb 
     */
    public DumpDataToES(String mode, String dbname, String dbcollection) {
        _indexname = dbname;
        _indextype = dbcollection;
        InitMain.init(mode);
        ConfigUtil.reloadProperties();
        try {
            _mongo = new Mongo(
                    ConfigUtil.getPropertieString("extractor.mongo.host"),
                    ConfigUtil.getPropertieInteger("extractor.mongo.port"));
            DB db = _mongo.getDB(_indexname);

            _collection = db.getCollection(_indextype);
            _cur = _collection.find();
            _esAddress = ConfigUtil
                    .getPropertieString("crawler.elasticsearch.address");
            _factory = new JestClientFactory();
            _factory.setHttpClientConfig(new HttpClientConfig
                    .Builder(_esAddress)
                    .readTimeout(25000)
                    .multiThreaded(true)
                    .build());
            _esClient = _factory.getObject();
            _configPath_plugin = ConfigUtil
                    .getPropertieString("extractor.dump_config_base_path");
            _pluginconfigXml = new XMLConfiguration(_configPath_plugin + "/" + mode +
                    "/" + _indextype + ".xml");
        } catch (UnknownHostException e) {
            log.error("Create mongo connection failed: ", e);
        } catch (ConfigurationException e) {
            log.error("Read xml failed: ", e);
        }
        _rebuildES = new ElasticMappingRebuild(_esClient, _pluginconfigXml);  // indextype = xmlName
        _saveES = new ElasticSave(_esClient, _pluginconfigXml);
    }

    /**
     * this is for testcase
     */
    public DumpDataToES(DBCursor cur,
                        ElasticMappingRebuild rebuildES,
                        ElasticSave saveES,
                        JestClient esClient,
                        Configuration pluginconfigXml,
                        String indexname,
                        String indextype) {
        _cur = cur;
        _rebuildES = rebuildES;
        _saveES = saveES;
        _esClient = esClient;
        _pluginconfigXml = pluginconfigXml;
        _indexname = indexname;
        _indextype = indextype;
    }

    /**
     * main
     */
    public void dumpDataToES(String dumpType) {
        boolean ifRebuild = _rebuildES.rebuild(_indexname, _indextype);
        log.info(ifRebuild + "create ES mapping: indexname is " +
                 _indexname + " indextype is " + _indextype);
        boolean ifSave = _saveES.saveToES(_cur, _indexname, _indextype, dumpType);
        log.info(ifSave + "successfully dump to " + _indexname + "  " + _indextype);
    }
}

