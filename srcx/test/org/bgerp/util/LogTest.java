package org.bgerp.util;

import org.junit.Assert;
import org.junit.Test;

public class LogTest {
    private static final Log log = Log.getLog();

    @Test
    public void testFormat() {
        Assert.assertEquals("Test with 1 and 2", Log.format("Test with {} and {}", "1", 2));
        Assert.assertEquals("Test with 1 and {}", Log.format("Test with {} and {}", "1"));
        Assert.assertEquals("Use String.format 3 and 'bb'", Log.format("Use String.format %s and '%s'", 3, "bb"));
    }

    /**
     * Test of exploit existence:<br>
     * https://security-tracker.debian.org/tracker/CVE-2021-44228
     * https://www.opennet.ru/opennews/art.shtml?num=56319
     */
    @Test
    public void testJNDIVariable() {
        log.info("JNDI1: {}", "${jndi:ldap://localhost/a}");
        log.info("JNDI2: {}", "jndi:ldap://localhost/a");
    }
}
