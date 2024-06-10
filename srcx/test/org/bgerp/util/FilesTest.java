package org.bgerp.util;

import java.util.List;

import org.bgerp.action.base.Actions;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.app.servlet.file.Options;
import org.bgerp.app.servlet.file.Order;
import org.bgerp.plugin.svc.backup.Plugin;
import org.bgerp.plugin.svc.backup.action.admin.BackupAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilesTest {
    @Before
    public void init() {
        Actions.init(List.of(Plugin.INSTANCE));
    }

    @Test
    public void test() {
        var files = new Files(BackupAction.class, "fileBackup", "", new Options().withDownloadEnabled().withOrder(Order.LAST_MODIFIED_DESC), "");
        Assert.assertEquals("org.bgerp.plugin.svc.backup.action.admin.BackupAction:downloadFileBackup", files.getDownloadPermissionAction());
        Assert.assertEquals("/admin/plugin/backup/backup.do?method=downloadFileBackup", files.getDownloadURL());
    }
}
