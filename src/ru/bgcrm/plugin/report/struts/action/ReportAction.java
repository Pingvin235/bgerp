package ru.bgcrm.plugin.report.struts.action;

import java.sql.Connection;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class ReportAction extends BaseAction {
    public ReportAction() {
        super();
    }

    public ActionForward doReport(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        return processUserTypedForward(con, mapping, form, null);
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        form.getResponse().setData("allowedReports", Utils.toSet(form.getPermission().get("allowedReports")));

        return mapping.findForward(FORWARD_DEFAULT);
    }
}
