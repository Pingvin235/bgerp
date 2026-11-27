package org.bgerp.plugin.svc.backup.action.admin;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.dist.Scripts;
import org.bgerp.plugin.svc.backup.Config;
import org.bgerp.plugin.svc.backup.Plugin;
import org.bgerp.app.servlet.file.Files;
import org.bgerp.app.servlet.file.Options;
import org.bgerp.app.servlet.file.Order;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/backup/backup")
public class BackupAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    public static final Files FILE_BACKUP = new Files(BackupAction.class, "fileBackup", "backup",
            new Options().withOrder(Order.LAST_MODIFIED_DESC).withDeletionEnabled().withDownloadEnabled(), "*");

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        form.setRequestAttribute("config", setup.getConfig(Config.class));
        return html(conSet, form, PATH_JSP + "/backup.jsp");
    }

    public ActionForward backup(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().backup(form.getParamBoolean("db"));
        return json(conSet, form);
    }

    public ActionForward restore(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().backupRestore(form.getParam("name", Utils::notBlankString)).restart(true);
        return json(conSet, form);
    }

    public ActionForward downloadFileBackup(DynActionForm form, ConnectionSet conSet) throws Exception {
        return FILE_BACKUP.download(form);
    }

    public ActionForward deleteFileBackup(DynActionForm form, ConnectionSet conSet) throws Exception {
        return json(conSet, FILE_BACKUP.delete(form));
    }
}
