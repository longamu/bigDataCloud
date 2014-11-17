package com.program.rebuildSQL.impl;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;

/**

 * @author wendy926

 */
public final class RebuildService {
    private static final Logger logger = 
            Log.getLog(RebuildService.class.getName());
    private StringBuilder _debugInfo = null;
    private String _mode = "";
    private String _tableName = "";
    private Jedis _jedis = null;
    private MongoDBRebuild _mongodbRebuild = null;
    private InfluxdbRebuild _influxdbRebuild = null;
    private ElasticsearchRebuild _elasticRebuild = null;
    private Configuration _pluginconfigXml = null;
    private Configuration _mappingES = null;
    
    public RebuildService(String mode) {
        _mode = mode;
        InitMain.init(_mode);
        ConfigUtil.reloadProperties();
    } 

    public RebuildService(String mode, String tableName) {
        _mode = mode;
        _tableName = tableName;
        InitMain.init(_mode);
        ConfigUtil.reloadProperties();
        String esAddress = ConfigUtil
                .getPropertieString("crawler.elasticsearch.address");
        String mongoHost = ConfigUtil
                .getPropertieString("extractor.mongo.host");
        int mongoPort = ConfigUtil.getPropertieInteger("extractor.mongo.port");
        String configPath_plugin = ConfigUtil
                .getPropertieString("extractor.parser_config_base_path"); 
        String configPath_mapping_ES = ConfigUtil
                .getPropertieString("crawler.mapping.filepath.mapping");
        _mongodbRebuild = new MongoDBRebuild(mongoHost, mongoPort);
        _elasticRebuild = new ElasticsearchRebuild(esAddress);
        _debugInfo = new StringBuilder();
        try {
            _pluginconfigXml = new XMLConfiguration(configPath_plugin + "/" + _mode +
                    "/" + tableName + ".xml");
            _mappingES = new XMLConfiguration(configPath_plugin + "/" + _mode + 
                    "/" + "mappings.xml");
        } catch (ConfigurationException e) {
            logger.error(e);
        }
    }

    public RebuildService(MongoDBRebuild mongodbRebuild, 
            ElasticsearchRebuild elasticRebuild, StringBuilder debugInfo, 
            Configuration pluginconfigXml, Configuration mappingES) {
        _mongodbRebuild = mongodbRebuild;
        _elasticRebuild = elasticRebuild;
        _debugInfo = debugInfo;
        _pluginconfigXml = pluginconfigXml;
        _mappingES = mappingES;
    }

    /**
     * redis Empty init
     */
    public boolean clearRedis() {
        String adresses = ConfigUtil
                .getPropertieString("scheduler.redis.hostport");
        String[] adress = adresses.split(",");
        for (String temp : adress) {
            String[] host = temp.split(":");
            String ip = host[0];
            int port = Integer.parseInt(host[1]);
            _jedis = new Jedis(ip, port);
            String flushDB = _jedis.flushDB();
            logger.info(ip + ":" + port + ":empty");
        }
        return true;
    }

    public boolean rebuildMongo(String dbName, String tableName) {
        String index = _pluginconfigXml.getString("db.index");
        String shading = _pluginconfigXml.getString("db.shading");
        if (!_mongodbRebuild.createCollection(dbName, tableName, index, shading,
                _debugInfo)) {
            return false;
        }
        return true;
    }

    public boolean rebuildElastic(String dbName, String tableName) {
        List<String> pluginName = _pluginconfigXml.getList("field.name");
        List<String> pluginType = _pluginconfigXml.getList("field.resultType");
        List<String> pluginAnalysis = _pluginconfigXml.getList("field.analysis");
        List<String> mappingName = _mappingES.getList("field.name");
        List<String> mappingType = _mappingES.getList("field.resultType");
        List<String> mappingAnalysis = _mappingES.getList("field.analysis");
        int mappingLength = mappingName.size();
        for(int i = 0; i < mappingLength; i++) {
            pluginName.add(mappingName.get(i));
            pluginType.add(mappingType.get(i));
            pluginAnalysis.add(mappingAnalysis.get(i));
        }
        _elasticRebuild.createMapping(dbName, tableName, pluginName,
                pluginType, pluginAnalysis, _debugInfo);
        return true;
    }

    public String getErrorMsg() {
        return _debugInfo.toString();
    }
  
    /**
     * 
     * @param type
     *            mongoDB or elasticSearch
     * @param dbName
     * @param tableName
     * @return
     */
    public boolean delete(String type, String dbName, String tableName) {
        if (type.equals(FieldNames.TYPE_NAME_MONGO)) {
            _mongodbRebuild.delete(dbName, tableName, _debugInfo);

        } else if (type.equals(FieldNames.TYPE_NAME_ES)) {
            _elasticRebuild.delete(dbName, tableName, _debugInfo);

        } else {
            _debugInfo.append("type is error");
        }
        return true;
    }
}
