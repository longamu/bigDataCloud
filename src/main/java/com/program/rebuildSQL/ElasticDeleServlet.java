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

import com.jiufubankcloud.crawler.util.ConfigUtil;
import com.jiufubankcloud.crawler.service.impl.RebuildService;

/**

 * @author wendy926

 */
final class ElasticDeleServlet extends HttpServlet {
    private Logger logger =
            LogManager.getLogger(ElasticDeleServlet.class
            .getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String indexname = request.getParameter("ElasticDeleDbname");
        String indextype = request.getParameter("ElasticDeleCollection");
        String mode = request.getParameter("mode");
        RebuildService rebuildService = new RebuildService(mode);
        boolean dele = rebuildService.delete("elasticSearch",indexname,indextype);
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>" +
        "            <html>" +
        "            <head>" +
        "                <title>Elastic Servlet</title>" +
        "            </head>" +
        "            <body>");
        if(dele == true) {
            out.println(
        "            <br>" + indexname +
        "            <br>" + indextype +
        "            <br>" + mode);
        }
        else {
            out.println("RebuildService.getErrorMsg()");
        }
        out.println("</body>" +
        "            </html>");
    }
}
