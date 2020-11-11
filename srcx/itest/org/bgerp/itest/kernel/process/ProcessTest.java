package org.bgerp.itest.kernel.process;

import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.testng.annotations.Test;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.Process;

@Test(groups = "process", dependsOnGroups = "dbInit")
public class ProcessTest {
    public static volatile int statusOpenId;
    public static volatile int statusProgressId;
    public static volatile int statusWaitId;
    public static volatile int statusDoneId;
    public static volatile int statusRejectId;

    public static volatile int paramProcessNextDateId;
    public static volatile int paramProcessDeadlineDateId;

    @Test
    public void addStatuses() throws Exception {
        int pos = 0;
        statusOpenId = ProcessHelper.addStatus("Open", pos += 2);
        statusProgressId = ProcessHelper.addStatus("Progress", pos += 2);
        statusWaitId = ProcessHelper.addStatus("Wait", pos += 2);
        statusDoneId = ProcessHelper.addStatus("Done", pos += 2);
        statusRejectId = ProcessHelper.addStatus("Reject", pos += 2);
    }
    
    public static volatile int posParam = 0;

    @Test
    public void addParams() throws Exception {
        // TODO: Make date chooser configuration.
        paramProcessNextDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Next date", posParam += 2, "", "");
        paramProcessDeadlineDateId = ParamHelper.addParam(Process.OBJECT_TYPE, Parameter.TYPE_DATE, "Deadline", posParam += 2, "", "");
    }

    @Test
    public void addTypes() throws Exception {
        
    }
}
