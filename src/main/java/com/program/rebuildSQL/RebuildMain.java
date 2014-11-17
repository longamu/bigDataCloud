package com.jiufubankcloud.crawler.service;

import java.net.InetSocketAddress;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**

 * @author wendy926

 */
final class RebuildMain {
    public static void main(String[] args) {
      //Log.init();
    try{
        InitMain.init("local");
        InetSocketAddress inet = new InetSocketAddress("0.0.0.0",
                          ConfigUtil.getPropertieInteger("crawler.empty.servlet.port"));
        Server server = new Server(inet);
        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new RebuildServlet()), "/");
        context.addServlet(new ServletHolder(new RedisServlet()), "/RedisHtml");
        context.addServlet(new ServletHolder(new MongodbReadServlet()), "/MongodbHtml");
        context.addServlet(new ServletHolder(new MongodbDeleServlet()),"/MongodbDeleHtml");
        context.addServlet(new ServletHolder(new ElasticReadServlet()), "/ElasticsearchHtml");
        context.addServlet(new ServletHolder(new ElasticDeleServlet()),"/ElasticsearchDeleHtml");
        context.addServlet(new ServletHolder(new InfluxDeleServlet()),"/InfluxdbDeleHtml");
        context.addServlet(new ServletHolder(new InfluxReadServlet()), "/InfluxdbHtml");
        context.addServlet(new ServletHolder(new HDFSServlet()),"/HDFSdbHtml");
        server.start();
     
       }catch(Exception e) {
            System.out.println("this is rebuild main error"+e);
        }
    }
}
