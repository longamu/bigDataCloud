package com.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

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
import org.junit.Assert;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RebuildService.class})

/**
 
 * @author wendy926

 */
public class TestRedisServlet extends HttpServlet {
    private MockHttpServletRequest _request = null;
    private MockHttpServletResponse _response = null;
    private RedisServlet _servlet = null;
    private String _indexname = "yjwang";
    private String _indextype = "type";
    private RebuildService _rebuildserv = PowerMockito.mock(RebuildService.class);
    private static Class<RedisServlet> reflect = RedisServlet.class;

    @Before
    public void setup() {
        ConfigUtil.init("local");
        _servlet = new RedisServlet();
        _request = new MockHttpServletRequest();
        _response = new MockHttpServletResponse();
    }

    @Test
    public void testServlet() throws ServletException, IOException {
        _request.setMethod("POST");
        _servlet.doPost(_request, _response);
        Assert.assertEquals("text/html;charset=UTF-8", _response.getContentType());
    }

    @Test
    public void testClear() throws NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Method method =
                reflect.getDeclaredMethod("clear", RebuildService.class);
        method.setAccessible(true);
        method.invoke(_servlet, _rebuildserv);
        verify(_rebuildserv, times(1)).clearRedis(); 
    }
}

