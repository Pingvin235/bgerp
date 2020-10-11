package org.bgerp.itest.kernel.process;

import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;

@Test(groups = "process", dependsOnGroups = "dbInit")
public class ProcessTest {
    public static volatile int statusOpen;
    public static volatile int statusToDo;
    public static volatile int statusProgress;
    public static volatile int statusWait;
    public static volatile int statusDoc;
    public static volatile int statusDone;
    public static volatile int statusRejected;

    public static volatile int paramProcessNextDateId;
    public static volatile int paramProcessDeadlineDateId;

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
    public void addParams() throws Exception {
        int pos = 0;
        // TODO: Make date chooser configuration.
        paramProcessNextDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Next date", pos += 2, "", "");
        paramProcessDeadlineDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Deadline", pos += 2, "", "");
    }

    @Test
    public void addTypes() throws Exception {
        
    }
}
