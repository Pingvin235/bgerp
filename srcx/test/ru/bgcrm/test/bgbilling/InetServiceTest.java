package ru.bgcrm.test.bgbilling;

import org.junit.Test;

import junit.framework.Assert;
import ru.bgcrm.plugin.bgbilling.proto.model.inet.InetService;

public class InetServiceTest {
    
    @Test
    public void testMacSet() {
        InetService serv = new InetService();
        serv.setMacAddressStr("01:02:03:04:05:06");
        Assert.assertEquals("01:02:03:04:05:06", serv.getMacAddressStr());
    }

}
