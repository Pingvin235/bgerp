package ru.bgcrm.plugin.report.struts.action;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dynamic.DynamicClassManager;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.plugin.report.dao.ReportDAO;
import ru.bgcrm.plugin.report.model.Config;
import ru.bgcrm.plugin.report.model.Report;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ReportAction extends BaseAction {
    public ReportAction() {
        super();
    }

    public ActionForward get(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Report report = setup.getConfig(Config.class).getReportMap().get(form.get("reportId"));
        if (report == null) 
            throw new BGMessageException("Отчёт не найден.");
        
        if (StringUtils.isNotBlank(report.getDaoClass())) {
            log.debug("Creating Java DAO class: %s", report.getDaoClass());
            
            ReportDAO dao = DynamicClassManager.newInstance(report.getDaoClass());
            dao.get(form);
            form.setForwardFile(dao.getJspFile());
            
            return data(conSet, mapping, form);
        }
        
        form.setForwardFile(report.getJspFile());
        return data(conSet, mapping, form);
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        form.getResponse().setData("allowedReports", Utils.toSet(form.getPermission().get("allowedReports")));

        return mapping.findForward(FORWARD_DEFAULT);
    }
}
