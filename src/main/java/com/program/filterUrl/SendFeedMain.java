package com.program.filterUrl;

import java.net.InetSocketAddress;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**

 * @author wendy926

 */
final class SendFeedMain {
    public static void main(String[] args) throws Exception {
        InetSocketAddress inet = new InetSocketAddress("0.0.0.0",Config.port);
        Server server = new Server(inet);
        ServletContextHandler context = new ServletContextHandler(
                ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        context.addServlet(new ServletHolder(new GiveUrlChoose()), "/");
        context.addServlet(new ServletHolder(new GiveUrlServlet()), "/GiveUrl");
        server.start();
    }

}


