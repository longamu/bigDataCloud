package com.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.junit.Before;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**

 * @author wendy926

 */
@PowerMockIgnore({"javax.management.*", "javax.net.*", "org.apache.http.conn.ssl.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({RebuildService.class})
final class TestElasticReadServlet extends HttpServlet {
    private Logger logger =
            LogManager.getLogger(ElasticReadServlet.class.getName());
    private ElasticReadServlet _servlet = null;
    private String _indexname = "yjwang";
    private String _indextype = "type";
    private RebuildService _rebuildserv = PowerMockito.mock(RebuildService.class);
    private static Class<ElasticReadServlet> reflect = ElasticReadServlet.class;

    @Before
    public void setup() {
        _servlet = new ElasticReadServlet(_rebuildserv);
    }

    @Test
    public void testInit() throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method =
                reflect.getDeclaredMethod("init", String.class, String.class);
        method.setAccessible(true);
        PowerMockito.when(_rebuildserv.rebuildElastic(
                _indexname, _indextype)).thenReturn(true);
        method.invoke(_servlet, _indexname, _indextype);
        verify(_rebuildserv, times(1)).rebuildElastic(
                _indexname, _indextype);  
    }
}

