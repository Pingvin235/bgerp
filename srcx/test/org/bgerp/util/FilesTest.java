package org.bgerp.util;

import org.bgerp.plugin.svc.backup.action.BackupAction;
import org.junit.Assert;
import org.junit.Test;

public class FilesTest {
    @Test
    public void test() {
        var files = new Files(BackupAction.class, "fileBackup", "", "");
        Assert.assertEquals("org.bgerp.plugin.svc.backup.action.BackupAction:downloadFileBackup",
                files.getDownloadPermissionAction());
        Assert.assertEquals("/admin/plugin/backup/backup.do?action=downloadFileBackup", files.getDownloadURL());
    }
}
