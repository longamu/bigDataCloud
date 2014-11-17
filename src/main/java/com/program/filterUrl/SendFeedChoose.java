package com.program.filterUrl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.HttpResponse;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**

 * @author wendy926

 */
final class SendFeedChoose extends HttpServlet {
    private static Logger logger =
            LogManager.getLogger(SendFeedChoose.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <title>SendFeed Choose</title>" +
                    "</head>" +
                    "<body>" +
                    "    <form action=\"/SendFeed\" method=\"post\">" +
                    "        <select name=\"mode\">" +
                    "            <option selected>choose</option>" +
                    "            <option value=\"local\">local</option>" +
                    "            <option value=\"default\">default</option>" +
                    "        </select>" +
                    "        <input type=\"submit\"" +
                    "               value=\"submit\"" +
                    "               name=\"\">" +
                    "        </input>" +
                    "    </form>" +
                    "</body>" +
                    "</html>");
    }
}




