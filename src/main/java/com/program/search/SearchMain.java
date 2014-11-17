package com.program.search;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

final class SearchMain {
    public static void main(String[] args) throws Exception {
        InetSocketAddress inet = new InetSocketAddress(ConfigUtil("hostname"), ConfigUtil("port"));
        Server server = new Server(inet);
        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new SearchESMongoUI()), "/");
        server.start();

    }
}
