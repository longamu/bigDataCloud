package com.program.filterUrl;

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

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

/**

 * @author wendy926

 */
final class SendFeedServlet extends HttpServlet {
    private static Logger logger =
            Log.getLog(SendFeedServlet.class.getName());
    private static String mode = "";
    private SendFeed _sendfeed = new SendFeed();
    private String _result = "";
    private ConnectionFactory _factory = null;
    private Channel _producerChannel = null;
    private boolean _signal = true;
    
    public SendFeedServlet() {
    }

    public SendFeedServlet(SendFeed sendfeed) {
        _sendfeed = sendfeed;
    }
   
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
     
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) 
            throws ServletException, IOException, NullPointerException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = null;
        String msg = ""; 
        String result = "";
        long timer = 0;
        mode = request.getParameter("mode");
        String jobId = request.getParameter("JobId");
        if (jobId == null || jobId == "") {
            timer = System.currentTimeMillis();
            jobId = String.valueOf(timer);
        }
        String timeOut = request.getParameter("TimeOut");
        if (timeOut == null || timeOut == "") {
            timeOut = "0";
        }
        String type = request.getParameter("type");
        if (type == null || type == "") {
            type = "";
        }
        String url = request.getParameter("url");
        if (url == null || url == "") {
            url = "";
        }
        String src = request.getParameter("source");
        if (src == null || src == "") {
            src = "";
        }
        InitMain.init(mode);
        ConfigUtil.reloadProperties();
        int priority_n =
                ConfigUtil.getPropertieInteger("fetcher.rabbitmq.level.number");
        int sign_n =
                ConfigUtil.getPropertieInteger("fetcher.rabbitmq.gradecount.number");
        String priority = request.getParameter("priority");
        if (priority == null || priority == "") {
            priority = "";
        }
        String sign = request.getParameter("sign");
        if (sign == null || sign == "") {
            sign = "";
        }
        logger.info("one mode is " + mode);
        if (jobId != null && jobId != "" && timeOut != null && timeOut != ""
                && type != null && type != "" && url != null && url != "" &&
                src != null && src != "" && priority != null && priority != ""
                && sign != null && sign != "" && mode != null && mode != "") {
            try {
                int time = Integer.parseInt(timeOut);
                result = _sendfeed.initUrl(jobId, url, time, type, src, 
                        mode, priority, sign);
            } catch (NumberFormatException e) {
                logger.error("parseInt error", e);
                result = "timeOut must be int";
            }
        }
        try {
            out = response.getWriter();
            out.println("<!DOCTYPE html>" +
                        "<html>" + 
                        "<head>" +
                        "    <title>GiveUrl Send</title>" +
                        "</head>" +
                        "<body>" +
                        "    <ul>" +
                        "        <li>JobId is built autoly</li>" +
                        "        <li>TimeOut number can be amplified 60 times</li>" +
                        "        <li>type should input type name</li>" +
                        "        <li>mode is ought to input local or default;" +
                        "            you can change the defaulted local to default</li>" +
                        "        <li>url is ought to input more than 4 letters</li>" +
                        "        <li>src can not be empty and you can input several" +
                        "            urls to be splited by 'enter'</li>" +
                        "        <li>priority is the important level of urls</li>" +
                        "        <li>sign is the importance level of urls</li>" +
                        "    </ul>" +
                        "    <form action=\"/GiveUrl\"" +
                        "          method=\"post\">" +
                        "        Mode type is:" +
                        "        <input type=\"text\"" +
                        "               name=\"mode\"" +
                        "               value=\"" + mode + "\">" +
                        "        </input>" +
                        "        <br/>" +
                        "        JobId:" +
                        "        <input type=\"text\"" +
                        "               name= \"JobId\" " +
                        "               value=\"" + jobId + "\">" +
                        "        </input>" +
                        "        TimeOut" +
                        "        <input type=\"text\"" +
                        "               name = \"TimeOut\"" +
                        "               value=\"" + timeOut + "\" >" +
                        "        </input>" +
                        "        type" +
                        "        <input type=\"text\"" +
                        "               name = \"type\" " +
                        "               value=\"" + type + "\">" +
                        "        </input>" +
                        "        Source" +
                        "        <input type=\"text\"" +
                        "               name=\"source\"" +
                        "               value=\"" + src + "\">" +
                        "        </input>" +
                        "        <br/>" +
                        "        priority" +
                        "        <select name=\"priority\">");
            for (int i = priority_n - 1; i >= 0; --i) {
                out.println(
                        "            <option value=\"" + i + "\">" + i + "</option>");
            }
                out.println(       
                        "        </select>" +
                        "        sign" +
                        "        <select name=\"sign\">");
            for (int j = sign_n - 1; j >= 0; --j) {
                String temp = String.format("%02d", j);
                out.println(
                        "            <option value=\"" + temp + "\">" + temp + "</option>");
            }
            out.println(
                        "        </select>" +
                        "        <br>" +
                        "        URL<br>" +
                        "        <textarea rows=\"15\" cols=\"120\"" +
                        "                  name = \"url\">" + url + "</textarea>" +
                        "        <br>" +
                        "        <input type=\"submit\"" +
                        "               value=\"submit\"" +
                        "               name=\"\">" +
                        "        </input>" +
                        "    </form>"+
                        "    <br>" + result +
                        "</body>" +
                        "</html>");
        } catch (NullPointerException e) {
            logger.info("NullPointerException: " + e.getMessage());
        }
    }
}



