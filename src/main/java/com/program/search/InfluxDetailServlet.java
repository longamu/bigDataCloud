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

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Serie;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator; 

final class InfluxDetailServlet extends HttpServlet {
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
       InfluxDB influxDB = 
               InfluxDBFactory.connect(ConfigUtil.getPropertieString("crawler.influxdb.adress"),
                                       "admin", "admin");
       String dbname = request.getParameter("influxdb");
       String dbcollection = request.getParameter("influxcollect");
       List<Serie> query = 
               influxDB.query(dbname, 
                              "select * from "+ dbcollection +" limit 200",
                              TimeUnit.MICROSECONDS);
       PrintWriter out = response.getWriter();
       out.println("<!DOCTYPE html>" +
                   "<script src=\"table.js\"></script>" +
                   "<html>" +
                   "    <head>" +
                   "        <title>Search result UI</title>" +
                   "    <script type=\"text/javascript\" src=\"table.js\">" +
                   "    </script>" +
                   "    </head>" +
                   "    <body>" +
                   "        <table width=\"1200\"" +
                   "               border=\"0\"" +
                   "               cellpadding=\"40\"" +
                   "               cellspacing=\"1\"" +
                   "               bgcolor=\"#999999\"> " +
                   "        <tr>" +
                   "            <th colspan=\"7\">Influxdb Data</th>" +
                   "        </tr>" +
                   "        <tbody id=\"tablelsw\">");
       for (Serie serie : query) {
           int length = serie.getRows().size();
           for (int i = 0; i < length; i++) {
               String valueInflux = 
                       PutDataToMap.putDataToMap(serie.getRows()
                                                      .get(i)
                                                      .get("value")
                                                      .toString());   
       out.println("        <tr>" +
                   "            <td bgcolor=\"#FFFFFF\">"+ (i + 1) +"</td>" +
                   "            <td bgcolor=\"#FFFFFF\">"+ valueInflux +"</td>" +
                   "        </tr>");
           }
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
                   "</html>");
   }
}


