package ru.bgcrm.model.config;

import org.bgerp.app.cfg.SimpleConfigMap;
import org.bgerp.model.process.config.IsolationConfig;
import org.bgerp.model.process.config.IsolationConfig.IsolationProcess;
import org.junit.Assert;
import org.junit.Test;

public class IsolationConfigTest {
    @Test
    public void testIsolationProcess() {
        var config = SimpleConfigMap.of("isolation.process", "group").getConfig(IsolationConfig.class);
        var isolation = config.getIsolationProcess();
        Assert.assertEquals(IsolationProcess.Type.GROUP, isolation.getType());

        config = SimpleConfigMap.of("isolation.process", "executor").getConfig(IsolationConfig.class);
        isolation = config.getIsolationProcess();
        Assert.assertEquals(IsolationProcess.Type.EXECUTOR, isolation.getType());

        config = SimpleConfigMap.of().getConfig(IsolationConfig.class);
        isolation = config.getIsolationProcess();
        Assert.assertEquals(IsolationProcess.EMPTY, isolation);
    }

    @Test
    public void testIsolationProcessGroupType() {
        var config = SimpleConfigMap.of(
            "isolation.process", "group",
            "isolation.process.group.executor.typeIds", "1,2"
        ).getConfig(IsolationConfig.class);
        var isolation = config.getIsolationProcess();
        Assert.assertNotNull(isolation);
        Assert.assertEquals(IsolationProcess.Type.GROUP, isolation.getType());
        Assert.assertEquals("1, 2", isolation.getExecutorTypeIds());
    }
}
