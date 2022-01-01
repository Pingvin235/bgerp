package org.bgerp.itest.kernel.process;

import org.bgerp.itest.helper.MessageHelper;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.kernel.user.UserTest;
import org.testng.annotations.Test;

@Test(groups = "processText", dependsOnGroups = { "process", "message" })
public class ProcessTextTest {
    private static final String TITLE = "Kernel Process Text";

    private int processId1;
    private int processId2;

    @Test
    public void addProcess() throws Exception {
        processId1 = ProcessHelper.addProcess(ProcessTest.processTypeTestId, UserTest.USER_ADMIN_ID, TITLE + " 1").getId();
        processId2 = ProcessHelper.addProcess(ProcessTest.processTypeTestId, UserTest.USER_ADMIN_ID, TITLE + "\n" + getText()).getId();
    }

    @Test(dependsOnMethods = { "addProcess" })
    public void addMessage() throws Exception {
        MessageHelper.addNoteMessage(processId2, UserTest.USER_ADMIN_ID, 0, "Indents, links", getText());
    }

    private String getText() {
        return new StringBuilder()
                .append("The link has to be recognized and clickable: https://bgerp.org\n")
                .append("The link to an existing process has to be clickable: #" + processId1 + "\n")
                .append("Text with indention 2 - 4:\n")
                .append("  2 indented\n")
                .append("    4 indented\n")
                .append("Empty line after:\n")
                .append("\n")
                .append("Block of JSP code with indents:\n")
                .append("<ui:toggle-button-group onChange=\"bla-bla\" hiddenName=\"\" value=\"1\">\n")
                .append("    <button style=\"btn-white\" value=\"1\"></button>\n")
                .append("    <button style=\"btn-white\" value=\"2\"></button>\n")
                .append("</ui:toggle-button-group>\n")
                .toString();
    }
}
