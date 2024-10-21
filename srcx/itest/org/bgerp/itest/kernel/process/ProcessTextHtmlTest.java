package org.bgerp.itest.kernel.process;

import java.time.Duration;

import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

@Test(groups = "processTextHtml", dependsOnGroups = { "process", "message" })
public class ProcessTextHtmlTest {
    private static final String TITLE = "Kernel Process Text HTML";

    private int processId;

    @Test
    public void process() throws Exception {
        processId = ProcessHelper.addProcess(ProcessTest.processTypeTestId, TITLE + " " + text()).getId();
    }

    @Test(dependsOnMethods = { "process" })
    public void message() throws Exception {
        MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ZERO, TITLE, text());
    }

    private String text() throws Exception {
        return ResourceHelper.getResource(this, "html.txt");
    }
}
