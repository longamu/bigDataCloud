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
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Iterator; 

/**
 
 * @author wendy926
  
 */ 
final class MongoDetailServlet extends HttpServlet {
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
       //mongodb
       MongodbRead readMongo = new MongodbRead();
       Mongo mongo = readMongo.MongoConnect();
       String dbname = request.getParameter("dbname");
       String dbcollection = request.getParameter("collect");
       DB db = mongo.getDB(dbname);
       DBCollection users = db.getCollection(dbcollection);
       DBCursor cur = users.find().limit(200);
       PrintWriter out = response.getWriter();
       out.println("<!DOCTYPE html>" +
                   "<html>" +
                   "    <head>" +
                   "        <title>Search result UI</title>" +
                   "        <script src=\"table.js\"></script>" +
                   "    </head>" +
                   "    <body>" +
                   "        <table width=\"1200\"" +
                   "               border=\"0\"" +
                   "               cellpadding=\"40\"" +
                   "               cellspacing=\"1\"" +
                   "               bgcolor=\"#999999\"> " +
                   "        <tr>" +
                   "            <th colspan=\"7\">Mongodb Data</th>" +
                   "        </tr>" +
                   "        <tbody id=\"tablelsw\">");
       for (int i = 0; i < cur.count(); i++) {
           String valueMongo = PutDataToMap.putDataToMap(cur.next().toString());
           out.println(
                   "        <tr>" +
                   "            <td bgcolor=\"#FFFFFF\">"+ (i + 1) +"</td>" +
                   "            <td bgcolor=\"#FFFFFF\">"+ valueMongo +"</td>" +
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


