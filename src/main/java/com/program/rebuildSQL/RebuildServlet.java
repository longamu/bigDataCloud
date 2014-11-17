package com.program.rebuildSQL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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

/**

 * @author wendy926

 */
final class RebuildServlet extends HttpServlet { 
    private Logger logger =
            LogManager.getLogger(RebuildServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        //Mongodb dele
        String dbname = request.getParameter("MongoDeleDbname");
        String msg = "";
        if (dbname == null || dbname == "") {
            msg += "Dbname can not be empty;";
            dbname = "";
        }
        String delecollection = request.getParameter("MongoDeleCollection");
        if (delecollection == null || delecollection == "") {
            msg += "collection can not be empty;";
            delecollection = "";
        }
        //Elastic dele db/table
        String indexname = request.getParameter("ElasticDeleDbname");
        if (indexname == null || indexname == "") {
            msg += "IndexName can not be empty;";
            indexname = "";
        }
        String indextype = request.getParameter("ElasticDeleCollection");
        if (indextype == null || indextype == "") {
            msg += "type can not be empty;";
            indextype = "";
        }
        //all mode
        String mode = request.getParameter("mode");
        if (mode == null || mode == "") {
            msg += "mode can not be empty;";
            mode = "";
        }
        //Mongodb read
        String collection = request.getParameter("MongoCollection");
        if (collection == null || collection == "") {
            msg += "collection can not be empty;";
            collection = "";
        }
        String MongoDbname = request.getParameter("MongoDbname");
        if (MongoDbname == null || MongoDbname == "") {
            msg += "IndexName can not be empty;";
            MongoDbname = "";
        }
        //Elastic create
        String mapping = request.getParameter("ElasticCollection");
        if(mapping == null || mapping == ""){
           msg += "Mapping can not be empty;";
           mapping = "";
        }
         String Elasticname = request.getParameter("ElasticDbname");
        if (Elasticname == null || Elasticname == "") {
            msg += "IndexName can not be empty;";
            Elasticname = "";
        }

        //Influxdb read
        String Influxcollection = request.getParameter("InfluxdbCollection");
        if (Influxcollection == null || Influxcollection == "") {
            msg += "collection can not be empty;";
            Influxcollection = "";
        }
        String InDBname = request.getParameter("InfluxDbname");
        if (InDBname == null || InDBname == "") {
            msg += "collection can not be empty;";
            InDBname = "";
        }

        //Influxdb dele
        String InfluxDelecollection = request.getParameter("InfluxDeleCollection");
        if (InfluxDelecollection == null || InfluxDelecollection == "") {
            msg += "collection can not be empty;";
            InfluxDelecollection = "";
        }
        String InfluxDBname = request.getParameter("InfluxDeleDbname");
        if (InfluxDBname == null || InfluxDBname == "") {
            msg += "collection can not be empty;";
            InfluxDBname = "";
        }
        //HDFS read
        String HDFScollection = request.getParameter("HDFSCollection");
        if (HDFScollection == null || HDFScollection == "") {
            msg += "collection can not be empty;";
            HDFScollection = "";
        }
        String HDFSDBname = request.getParameter("HDFSDbname");
        if (HDFSDBname == null || HDFSDBname == "") {
            msg += "collection can not be empty;";
            HDFSDBname = "";
        }


        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>" +
        "            <html>" +
        "            <head>" +
        "                <title>Rebuild Servelet</title>" +
        "            </head>" +
        "            <script type=\"text/javascript\">"+
        "                function care() {"+
        "                      alert(\"Are you sure choice default\");"+
        "                     }"+
        "            </script>"+
        "            <body>" +
        //Redis Html
        "                <br/>" +
        "                <form action=\"/RedisHtml\" method=\"post\">" +
        "                    Redis:" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\">local</option>" +
        "                        <option onclick=\"care()\""+
        "                            value=\"default\">default</option>" +
        "                    </select>" +
        "                    <input type=\"submit\"" +
        "                           value=\"empty\"" +
        "                           name=\"\">" +
        "                    <br>" +
        "                </form>" +
        //Mongodb read Html
        "                    <br>" +
        "                <form action=\"/MongodbHtml\" method=\"post\">" +
        "                    Mongodb Create:dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"MongoDbname\"" +
        "                           value=\""+ MongoDbname +"\">" +
        "                    Collection:<input type=\"text\"" +
        "                    name = \"MongoCollection\" value=\""+ collection +"\">" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\" >local</option>" +
        "                        <option onclick=\"care()\""+
        "                               value=\"default\">default</option>" +
        "                    </select>" +
        "                    <br>" +
        "                    <input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
        
        //Mongodb Dele
        "                <br>" +
        "                <form action=\"/MongodbDeleHtml\" method=\"post\">" +
        "                    Mongodb Delete:dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"MongoDeleDbname\"" +
        "                           value=\"" + dbname +"\">" +
        "                    Collection:" +
        "                    <input type=\"text\"" +
        "                           name=\"MongoDeleCollection\"" +
        "                           value=\"" + delecollection +"\">" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\">local</option>" +
        "                        <option onclick=\"care()\""+
        "                                value=\"default\">default</option>" +
        "                    </select>" +
        "                    <br><input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
       
        //Elastic read
        "                <br/>" +
        "                <form action=\"/ElasticsearchHtml\" method=\"post\">" +
        "                    Elasticsearch create: dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"ElasticDbname\"" +
        "                           value=\"" + Elasticname +"\">" +
        "                    Collection:<input type=\"text\"" +
                                        "name = \"ElasticCollection\"" +
        "                                value=\""+ mapping +"\"></input>" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\" >local</option>" +
        "                        <option onclick=\"care()\""+
        "                                value=\"default\">default</option>" +
        "                    </select>" +
        "                    <br><input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
      
        //Elastic Dele
        "                <br/>" +
        "                <form action=\"/ElasticsearchDeleHtml\" method=\"post\">" +
        "                    Elasticsearch Delete: dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"ElasticDeleDbname\"" +
        "                           value=\"" + indexname +"\">" +
        "                    Collection" +
        "                    <input type=\"text\"" +
        "                           name=\"ElasticDeleCollection\"" +
        "                           value=\"" + indextype +"\">" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\">local</option>" +
        "                        <option onclick=\"care()\""+
        "                                value=\"default\">default</option>" +
        "                    </select>" +
        "                <br>" +
        "                    <input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
        
        //influx read
        "                <br/>" +
        "                <form action=\"/InfluxdbHtml\" method=\"post\">" +
        "                    Influxdb Create: dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"InfluxDbname\"" +
        "                           value=\"" + InDBname +"\">" +
        "                    Collection:" +
        "                    <input type=\"text\"" +
        "                           name = \"InfluxdbCollection\"" +
        "                           value=\"" + Influxcollection +"\">" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\" >local</option>" +
        "                        <option onclick=\"care()\""+
        "                                value=\"default\">default</option>" +
        "                    </select>" +
        "                    <br><input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
        
        //influx dele
        "                <br/>" +
        "                <form action=\"/InfluxdbDeleHtml\" method=\"post\">" +
        "                    Influxdb Delete:dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"InfluxDeleDbname\"" +
        "                           value=\"" + InfluxDBname +"\">" +
        "                    Collection" +
        "                    <input type=\"text\"" +
        "                           name=\"InfluxDeleCollection\"" +
        "                           value=\""+ InfluxDelecollection +"\">" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\">local</option>" +
        "                        <option onclick=\"care()\""+
        "                                value=\"default\">default</option>" +
        "                    </select>" +
        "                    <br><input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
        
        //HDFS read
        "                <br/>" +
        "                <form action=\"/HDFSdbHtml\" method=\"post\">" +
        "                    HDFS Create:dbname" +
        "                    <input type=\"text\"" +
        "                           name=\"HDFSDbname\"" +
        "                           value=\"" + HDFSDBname +"\">" +
        "                    Collection:" +
        "                    <input type=\"text\"" +
        "                           name = \"HDFSCollection\"" +
        "                           value=\"" + HDFScollection +"\">" +
        "                    mode" +
        "                    <select name=\"mode\">" +
        "                        <option value=\"local\" >local</option>" +
        "                        <option onclick=\"care()\""+
        "                                value=\"default\">default</option>" +
        "                    </select>" +
        "                    <br><input type=\"submit\" value=\"submit\" name=\"\">" +
        "                </form>" +
        "            </body>" +
        "         </html>");
    }
}
