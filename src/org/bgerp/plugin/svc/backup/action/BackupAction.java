package org.bgerp.plugin.svc.backup.action;

import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.svc.backup.Plugin;
import org.bgerp.servlet.file.Files;
import org.bgerp.servlet.file.Options;
import org.bgerp.servlet.file.Order;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.distr.Scripts;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/admin/plugin/backup/backup")
public class BackupAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_ADMIN;

    public static final Files FILE_BACKUP = new Files(BackupAction.class, "fileBackup", "backup", "*",
            new Options().withOrder(Order.LAST_MODIFIED_DESC).withDownloadEnabled());

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return html(conSet, form, PATH_JSP + "/backup.jsp");
    }

    public ActionForward backup(DynActionForm form, ConnectionSet conSet) throws Exception {
        new Scripts().backup(form.getParamBoolean("db"));
        return json(conSet, form);
    }

    public ActionForward downloadFileBackup(DynActionForm form, ConnectionSet conSet) throws Exception {
        return FILE_BACKUP.download(form);
    }
}
