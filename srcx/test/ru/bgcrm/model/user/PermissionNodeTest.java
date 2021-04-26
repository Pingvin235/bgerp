package ru.bgcrm.model.user;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ru.bgerp.l10n.Localization;
import ru.bgerp.l10n.Localizer;

public class PermissionNodeTest {
    @Test
    public void testKernelTree() {
        var pl = new org.bgerp.plugin.kernel.Plugin();
        var l = new Localizer(Localization.LANG_EN, Localization.getLocalization(pl));
        
        var node = new PermissionNode(null, l, pl.getXml(PermissionNode.FILE_NAME, null).getDocumentElement());
        var p = node.findPermissionNode("ru.bgcrm.struts.action.admin.ProcessAction:queueList");
        Assert.assertTrue(p.getDescription().contains("<b>allowedQueueIds</b> -"));
        
        p = node.findPermissionNode("ru.bgcrm.struts.action.admin.AppAction:status");
        Assert.assertEquals("Status", p.getTitle());
        Assert.assertEquals("Kernel -> Administration -> App -> Status", p.getTitlePath());

        Assert.assertEquals(
                List.of("ru.bgcrm.struts.action.admin.AppAction:status",
                        "ru.bgcrm.struts.action.admin.StateAction:null", "org.bgerp.action.admin.AppAction:status"),
                p.getActionList());
        Assert.assertEquals(p, node.findPermissionNode("ru.bgcrm.struts.action.admin.StateAction:null"));
        Assert.assertFalse(p.isAllowAll());
        Assert.assertFalse(p.isNotLogging());

        p = node.findPermissionNode("org.bgerp.action.TestAction:null");
        Assert.assertTrue(p.isAllowAll());
        Assert.assertTrue(p.isNotLogging());
    }

    @Test
    public void testTaskTree() {
        var pl = new ru.bgcrm.plugin.task.Plugin();
        var l = new Localizer(Localization.LANG_EN, Localization.getLocalization(pl));

        var node = new PermissionNode(null, l, pl.getXml(PermissionNode.FILE_NAME, null).getDocumentElement());
        Assert.assertEquals("Plugin Task", node.getTitle());
        Assert.assertEquals("Plugin Task", node.getTitlePath());
        Assert.assertEquals(3, node.getChildren().size());
    }

    @Test
    public void testBackupTree() {
        var pl = new org.bgerp.plugin.svc.backup.Plugin();
        var plk = new org.bgerp.plugin.kernel.Plugin();
        var l = new Localizer(Localization.LANG_EN, Localization.getLocalization(plk));

        var node = new PermissionNode(null, l, pl.getXml(PermissionNode.FILE_NAME, null).getDocumentElement());

        var p = node.findPermissionNode("org.bgerp.plugin.svc.backup.action.BackupAction:null");
        Assert.assertNotNull(p);
        Assert.assertEquals("Plugin Backup -> Backup", p.getTitlePath());
        Assert.assertEquals("", p.getDescription());

        p = node.findPermissionNode("org.bgerp.plugin.svc.backup.action.BackupAction:downloadFileBackup");
        Assert.assertNotNull(p);
        Assert.assertEquals("Download file", p.getTitle());
        Assert.assertEquals("", p.getDescription());
    }
}