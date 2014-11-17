package com.program.search;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;

/**
 
 * @author wendy926

 */
final class SearchUIServlet extends HttpServlet {
    private static Logger logger = LogManager.getLogger(SearchUIServlet.class
            .getName());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", "ElasticSearchDev").build();
        Client client = new TransportClient(settings)
                .addTransportAddress(
                new InetSocketTransportAddress(ConfigUtil("es.hostname"), ConfigUtil("es.port")));
        SearchResponse responsibal = client.prepareSearch("taobao")
                                           .setTypes("tmall_item")
                                           .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                                           .setFrom(0)
                                           .setSize(99)
                                           .setExplain(true)
                                           .execute()
                                           .actionGet();
        SearchHits hits = responsibal.getHits();
        System.out.println(hits.getTotalHits());
        Mongo mongo = new Mongo(Config("mongo.hostname"), ConfigUtil("mongo.port"));
        DB db = mongo.getDB("ecommerce");
        DBCollection users = db.getCollection("tmall.product");
        DBCursor cur = users.find().skip(1).limit(100);
        InfluxDB influxDB = InfluxDBFactory.connect(ConfigUtil("influxdb.address"),
                                                    "admin", 
                                                    "admin");
        List<Serie> query = influxDB.query("taobao", 
                                           "select * from tmall_item limit 100",
                                           TimeUnit.MICROSECONDS);
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>" +
                    "<html>" +
                    "    <head>" +
                    "        <title>Search result UI</title>" +
                    "    </head>" +
                    "    <body>" +
                    "        <form action=\"/\" method=\"post\">" +
                    "            <label> MongoDB search TOP100: </label>" +
                    "            <select name=\"Mongodb_detail\">");
       	while (cur.hasNext()) {
            out.print(
                    "            <option value=\"i_Mongo\">" +
                                     cur.next() +
                    "            </option>");
        }
        out.println("           </select>" +
                    "           <br><label> ElasticSearch TOP100: </label>" +
                    "             <select name=\"Elastic_detail\">");
        for (int i = 0; i < hits.getHits().length; i++) {
            out.println(
                    "             <option value=\"i_Elasticdetail\">" +
                                      hits.getHits()[i].getSourceAsString() +
                    "             </option>");
        }
        out.println("            </select>" +
                    "            <br><label> InfluxSearch TOP100: </labal>" +
                    "            <select name=\"InfluxSearchdetail\">");
        for (Serie serie : query) {
            int length = serie.getRows().size();
            for (int i= 0; i<length; i++) {
                String value = 
                        serie.getRows().get(i).get("value").toString();
                out.println(
                    "            <option value=\"InfluxDB\">" +
                                            value +
                    "            </option>");
            }
        }
        out.println("            </select>" +
                    "        </form>" +
                    "    </body>" +
                    "</html>");
    }
}
