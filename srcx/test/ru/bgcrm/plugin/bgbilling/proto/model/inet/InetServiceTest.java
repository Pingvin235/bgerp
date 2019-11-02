package ru.bgcrm.plugin.bgbilling.proto.model.inet;

import org.junit.Assert;
import org.junit.Test;

public class InetServiceTest {
    
    @Test
    public void testMacSet() {
        InetService serv = new InetService();
        serv.setMacAddressStr("01:02:03:04:05:06");
        Assert.assertEquals("01:02:03:04:05:06", serv.getMacAddressStr());
    }

}
