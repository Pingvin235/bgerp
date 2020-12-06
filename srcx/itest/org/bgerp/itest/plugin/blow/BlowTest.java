package org.bgerp.itest.plugin.blow;

import org.bgerp.itest.configuration.department.development.DevelopmentTest;
import org.bgerp.itest.configuration.department.sales.SalesTest;
import org.bgerp.itest.configuration.department.support.SupportTest;
import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.process.ProcessTest;
import org.testng.annotations.Test;

@Test(groups = "blow", priority = 100, dependsOnGroups = { "config", "depDev", "depSupport", "depSales" })
public class BlowTest {
    public static volatile int configId;

    @Test
    public void initConfig() throws Exception {
        configId = ConfigHelper.addIncludedConfig("Plugin Blow", ResourceHelper.getResource(this, "config.txt"));
    }

    @Test (dependsOnMethods = "initConfig")
    public void addBoardDev() throws Exception {
        ConfigHelper.addToConfig(BlowTest.configId, 
            ConfigHelper.generateConstants(
                "PROCESS_QUEUE_ID", DevelopmentTest.queueTasksId, 
                "COL_STATUS_CHANGED", 20,
                "COL_MESSAGES", 10,
                "COL_MESSAGES_UNREAD", 12,
                "PROCESS_STATUS_PROGRESS_ID", ProcessTest.statusProgressId,
                "PROCESS_STATUS_WAIT_ID", ProcessTest.statusWaitId,
                "PROCESS_TYPE_TASK_ID", DevelopmentTest.processTypeTaskId,
                "PROCESS_TYPE_SUPPORT_ID", SupportTest.processTypeSupportId,
                "PROCESS_TYPE_SALES_ID", SalesTest.processTypeSaleId,
                "GROUP_ID", DevelopmentTest.groupId) +
            ResourceHelper.getResource(this, "config.board.dev.txt")
        );
    }
}