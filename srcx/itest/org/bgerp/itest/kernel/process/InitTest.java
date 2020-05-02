package org.bgerp.itest.kernel.process;

import org.bgerp.itest.helper.ProcessHelper;
import org.testng.annotations.Test;

@Test(groups = "processInit", dependsOnGroups = "dbInit")
public class InitTest {
    public static volatile int statusOpen;
    public static volatile int statusToDo;
    public static volatile int statusProgress;
    public static volatile int statusWait;
    public static volatile int statusDoc;
    public static volatile int statusDone;
    public static volatile int statusRejected;

    @Test
    public void addStatuses() throws Exception {
        int pos = 0;
        statusOpen = ProcessHelper.addStatus("Open", pos += 2);
        statusToDo = ProcessHelper.addStatus("ToDo (postponed)", pos += 2);
        statusProgress = ProcessHelper.addStatus("Progress", pos += 2);
        statusWait = ProcessHelper.addStatus("Wait for..", pos += 2);
        statusDoc = ProcessHelper.addStatus("Doc", pos += 2);
        statusDone = ProcessHelper.addStatus("Done", pos += 2);
        statusRejected = ProcessHelper.addStatus("Rejected", pos += 2);
    }

    @Test
    public void addTypes() throws Exception {
        
    }

}
