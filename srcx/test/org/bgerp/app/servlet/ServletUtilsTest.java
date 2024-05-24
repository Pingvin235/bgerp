package org.bgerp.app.servlet;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.mock.MockHttpServletRequest;
import org.bgerp.app.servlet.util.ServletUtils;
import org.junit.Assert;
import org.junit.Test;

public class ServletUtilsTest {
    @Test
    public void testGetRequestURI() {
        HttpServletRequest request = new MockHttpServletRequest() {
            @SuppressWarnings("unused")
            private String requestDispatcherPath = "/user/path1";

            @Override
            public String getRequestURI() {
                return null;
            }
        };
        Assert.assertEquals("/user/path1", ServletUtils.getRequestURI(request));

        request = new MockHttpServletRequest() {
            @Override
            public String getRequestURI() {
                return "/user/plugin/path2";
            }
        };
        Assert.assertEquals("/user/plugin/path2", ServletUtils.getRequestURI(request));
    }
}
