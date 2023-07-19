package ru.bgcrm.model.config;

import org.bgerp.app.cfg.SimpleConfigMap;
import org.bgerp.model.config.IsolationConfig;
import org.bgerp.model.config.IsolationConfig.IsolationProcess;
import org.junit.Assert;
import org.junit.Test;

public class IsolationConfigTest {
    @Test
    public void testIsolationProcess() {
        var config = SimpleConfigMap.of("isolation.process", "group").getConfig(IsolationConfig.class);
        var isolation = config.getIsolationProcess();
        Assert.assertNotNull(isolation);
        Assert.assertEquals(IsolationProcess.GROUP, isolation);

        config = SimpleConfigMap.of("isolation.process", "executor").getConfig(IsolationConfig.class);
        isolation = config.getIsolationProcess();
        Assert.assertNotNull(isolation);
        Assert.assertEquals(IsolationProcess.EXECUTOR, isolation);

        config = SimpleConfigMap.of().getConfig(IsolationConfig.class);
        isolation = config.getIsolationProcess();
        Assert.assertNull(isolation);
    }

    @Test
    public void testIsolationProcessGroupType() {
        var config = SimpleConfigMap.of(
            "isolation.process", "group",
            "isolation.process.group.executor.typeIds", "1,2"
        ).getConfig(IsolationConfig.class);
        var isolation = config.getIsolationProcess();
        Assert.assertNotNull(isolation);
        Assert.assertEquals(IsolationProcess.GROUP, isolation);
        Assert.assertEquals("1, 2", isolation.getExecutorTypeIds());
    }
}
