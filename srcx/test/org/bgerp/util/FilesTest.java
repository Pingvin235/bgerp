package org.bgerp.util;

import org.bgerp.plugin.svc.backup.action.BackupAction;
import org.bgerp.servlet.file.Files;
import org.bgerp.servlet.file.Options;
import org.bgerp.servlet.file.Order;
import org.junit.Assert;
import org.junit.Test;

public class FilesTest {
    @Test
    public void test() {
        var files = new Files(BackupAction.class, "fileBackup", "", "", new Options().withDownloadEnabled().withOrder(Order.LAST_MODIFIED_DESC));
        Assert.assertEquals("org.bgerp.plugin.svc.backup.action.BackupAction:downloadFileBackup",
                files.getDownloadPermissionAction());
        Assert.assertEquals("/admin/plugin/backup/backup.do?action=downloadFileBackup", files.getDownloadURL());
    }
}
