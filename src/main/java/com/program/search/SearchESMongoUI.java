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
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import org.json.JSONObject;
import org.apache.http.HttpResponse;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.indices.aliases.GetAliases;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.indices.mapping.GetMapping;

/**
 
 * @author wendy926
 
 */
final class SearchESMongoUI extends HttpServlet {
    private static Logger logger =
            LogManager.getLogger(SearchESMongoUI.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
       response.setContentType("text/html;charset=UTF-8");
       ConfigUtil.reloadProperties();
       JestClientFactory factory = new JestClientFactory();
       factory.setHttpClientConfig(new HttpClientConfig.Builder(
               ConfigUtil.getPropertieString(
               "crawler.elasticsearch.search.host.port")).multiThreaded(
               true).build());
       JestClient client = factory.getObject();
       GetAliases getAliases = new GetAliases.Builder().build();
       JestResult result = null;
       try {
          result = client.execute(getAliases);
       } catch (Exception e) {
             e.printStackTrace();
       }
       String jsonString = result.getJsonString();
       JSONObject jsonObject = new JSONObject(jsonString);
       // mongodb
       Mongo mongo = new Mongo(
               ConfigUtil.getPropertieString("extractor.mongo.host"),
               ConfigUtil.getPropertieInteger("extractor.mongo.port"));
       List<String> databaseNames = mongo.getDatabaseNames();
       // influxdb
       InfluxDB influxDB =
               InfluxDBFactory.connect(
                       ConfigUtil.getPropertieString("crawler.influxdb.adress"),
                       ConfigUtil.getPropertieString("crawler.influxdb.useName"),
                       ConfigUtil.getPropertieString("crawler.influxdb.passWord"));
       List<Database> describeDatabases = influxDB.describeDatabases();
       JSONObject jsonObjectMap = null;
       JSONObject jsonObjectMapinner = null;
       JSONObject mappings = null;
       String indextype = "";
       PrintWriter out = response.getWriter();
       out.println("<!DOCTYPE html>" +
                   "<html>" +
                   "    <head>" +
                   "        <title>Search result UI</title>" +
                   "        <style type=\"text/css\">" +
                   "            a{text-decoration:underline;" +
                   "               cursor: pointer;}" +
                   "            }" +
                   "        </style>" +
                   "        <script>" +
                   "        function mongomethod(val, i) { " +
                   "            document.location.href =" +
                   "                '/MongoDetailServlet?collect=' + val + '&dbname=' +i;}" +

                   "        </script>" +
                   "        <script>" +
                   "        function influxmethod(val, i) {" +
                   "            document.location.href =" +
                   "                '/InfluxDetailServlet?influxcollect=' + val + '&influxdb=' +i;}" +
                   "        </script>" +
                   "        <script>" +
                   "        function elasticmethod(val, i) { " +
                   "            document.location.href =" +
                   "                '/ElasticDetailServlet?elasticcollect=' + val + '&elasticdbname=' +i;}" +
                   "        </script>" +
                   "    </head>" +
                   "    <body>" +
                   "        <form action=\"/MongoDetailServlet\" method=\"post\">" +
                   "            MongoDB search TOP100: " +
                   "            <ul>");
       for (String dbName : databaseNames) {
           out.println(
                   "                <li> "+ dbName +"" +
                   "                    <ul>");
           DB db = mongo.getDB(dbName);
           Set<String> collectionNames = db.getCollectionNames();
           for (String collection : collectionNames) {
               if (collection.contains("system") ||
                       collection.contains("html")) {
                   continue;
               }
               out.println(
                   "                        <li>" +
                   "                            <a onClick=" +
                   "                                    \"mongomethod(" +
                   "                                    '"+ collection +"'," +

                   "                                    '"+ dbName +"')\">" +
                   "                                    "+ collection +"" +
                   "                            </a>" +
                   "                        </li>");
           }
           out.println(
                   "                    </ul>" +
                   "                </li>");
       }
       out.println("            </ul>" +
                   "        </form>" +
                   "        <form action=\"/InfluxDetailServlet\" " +
                   "              method=\"post\">" +
                   "             InfluxSearch TOP100:" +
                   "            <ul>");
       for (Database db : describeDatabases) {
           String dbName =db.getName();
           out.println(
                   "                <li>"+ dbName +"" +
                   "                    <ul>");
           List<Serie> query = influxDB.query(db.getName(), "list series",
                                              TimeUnit.MILLISECONDS);
           for (Serie serie : query) {
               String serieName = serie.getName();
               if (serieName.contains("system") ||
                       serieName.contains("html")) {
                   continue;
               }
               out.println(
                   "                        <li>" +
                   "                            <a onClick=" +
                   "                                    \"influxmethod(" +

                   "                                    '"+ serieName +"'," +
                   "                                    '"+ dbName +"')\">" +
                   "                                    "+ serieName +"" +
                   "                            </a>" +
                   "                        </li>");
           }
           out.println(
                   "                    </ul>" +
                   "                </li>");
       }
       out.println("            </ul>");
       out.println("        </form>" +
                   "        <form action=\"/ElasticDetailServlet\"" +
                   "              method=\"post\">" +
                   "            ElasticSearch TOP100: " +
                   "            <ul>");
       Iterator<String> iterator = jsonObject.keys();
       while (iterator.hasNext()) {
            String dbName = iterator.next();
            if (dbName.contains(".marvel")) {
                continue;
            }
            out.println(
                   "               <li>"+ dbName +"");
            GetMapping getMapping =
                    new GetMapping.Builder().addIndex(dbName).build();
            JestResult mapresult = null;
            try {
                mapresult = client.execute(getMapping);
            } catch (Exception e) {
                  e.printStackTrace();

            }
            String deletype = mapresult.getJsonString();
            jsonObjectMap = new JSONObject(deletype);
            if(jsonObjectMap.isNull(dbName)) continue;
            jsonObjectMapinner =jsonObjectMap.getJSONObject(dbName);
            mappings = jsonObjectMapinner.getJSONObject("mappings");
            Iterator<String> iteratormap = mappings.keys();
            while (iteratormap.hasNext()) {
                indextype = iteratormap.next();
                if (indextype.contains("system") || indextype.contains("html")) {
                    continue;
                }
                out.println(
                   "                    <ul>" +
                   "                        <li>" +
                   "                            <a onClick=" +
                   "                                    \"elasticmethod(" +
                   "                                    '"+ indextype +"'," +
                   "                                    '"+ dbName +"')\">" +
                   "                                    "+ indextype +"" +
                   "                            </a>" +
                   "                        </li>" +
                   "                    </ul>");
            }
            out.println(
                   "                </li>");
       }
       out.println("            </ul>" +
                   "        </form>" +
                   "    </body>" +
                   "</html>");
   }

