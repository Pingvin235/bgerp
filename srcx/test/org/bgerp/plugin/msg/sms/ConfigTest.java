package org.bgerp.plugin.msg.sms;

import org.bgerp.app.cfg.SimpleConfigMap;
import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {
    @Test
    public void testConfig() {
        var map = SimpleConfigMap.of(
            "sms:type", "mts",
            "sms:login", "login",
            "sms:password", "password",
            "sms:1.type", "smsc",
            "sms:1.login", "login",
            "sms:1.password", "password"
        );

        var config = map.getConfig(Config.class);
        var senders = config.getSenders();
        Assert.assertEquals(2, senders.size());
        Assert.assertEquals(SenderMTS.class, senders.get(0).getClass());
        Assert.assertEquals(SenderSMSC.class, senders.get(1).getClass());
    }
}
