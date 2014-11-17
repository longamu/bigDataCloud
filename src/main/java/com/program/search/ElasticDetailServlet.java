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
import org.elasticsearch.search.SearchException;
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
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator; 
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**

 * @author wendy926

 */
final class ElasticDetailServlet extends HttpServlet {
    private static Logger logger =
            Log.getLog(SearchUIServlet.class.getName());  

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
       response.setContentType("text/html;charset=UTF-8");
       ConfigUtil.reloadProperties();
       String dbname = request.getParameter("elasticdbname");
       String dbcollection = request.getParameter("elasticcollect");
       System.out.println(dbname);
       System.out.println(dbcollection);
       //elastic
       ElasticsearchRead readES = new ElasticsearchRead();
       JestClientFactory factory = new JestClientFactory();
       factory.setHttpClientConfig(
               new HttpClientConfig.Builder(
                       ConfigUtil.getPropertieString(
                       "crawler.elasticsearch.search.host.port")).multiThreaded(true).build());
       JestClient jestclient = factory.getObject();
       String query = "{\"query\":{ \"match_all\" : { }}}";
       Search search = 
               new Search.Builder(query).addIndex(dbname).addType(dbcollection).build();
       JestResult result = null;
       try {
          result = jestclient.execute(search);
       } catch (Exception e) {
           logger.error("error message", e);
       }
       JsonObject m = result.getJsonObject();
       System.out.println("result: " + m);
       if ( ! m.isJsonNull()) {
           JsonArray jsonhits = m.getAsJsonObject("hits").getAsJsonArray("hits");
           if( ! jsonhits.equals("[]")) {
       System.out.println("json: " + m);
       PrintWriter out = response.getWriter();
       out.println("<!DOCTYPE html>" +
                   "<html>" +
                   "    <head>" +
                   "        <title>Search result UI</title>" +
                   "    </head>" +
                   "    <script src=\"table.js\"></script>" +
                   "    <body>");
       out.println(
                   "        <table width=\"1200\"" +
                   "               border=\"0\"" + 
                   "               cellpadding=\"40\"" +
                   "               cellspacing=\"1\"" +
                   "               bgcolor=\"#999999\"> " +
                   "        <tr>" +
                   "            <th colspan=\"7\">ElasticSearch Data</th>" +
                   "        </tr>" +
                   "        <tbody id=\"tablelsw\">");
       for (int i = 0; i < 200; i++) {
           String valueElastic = 
                   PutDataToMap.putDataToMap(jsonhits.get(i).toString());
           out.println(
                   "        <tr>" +
                   "            <td bgcolor=\"#FFFFFF\">"+ (i + 1) +"</td>" +
                   "            <td bgcolor=\"#FFFFFF\">"+ valueElastic +"</td>" +
                   "        </tr>");
       }
       out.println("        </tbody>" +
                   "        </table>" +
                   "        <span id=\"spanFirst\">首页</span>" +
                   "        <span id='spanPre'>上一页</span>" +
                   "        <span id=\"spanNext\">下一页</span>" +
                   "        <span id=\"spanLast\">尾页</span>" +
                   "        第<span id=\"spanPageNum\"></span>页/共" +
                   "        <span id=\"spanTotalPage\"></span>页" +
                   "    </body>" +
                   "</html>" +
                   "<script src=\"table.js\"></script>");
           }
       }
   }
}


