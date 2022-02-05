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
    public void addProcess() throws Exception {
        processId = ProcessHelper.addProcess(ProcessTest.processTypeTestId, UserTest.USER_ADMIN_ID, TITLE + " " + getText()).getId();
    }

    @Test(dependsOnMethods = { "addProcess" })
    public void addMessage() throws Exception {
        MessageHelper.addNoteMessage(processId, UserTest.USER_ADMIN_ID, Duration.ZERO, TITLE, getText());
    }

    private String getText() throws Exception {
        return ResourceHelper.getResource(this, "html.txt");
    }
}
