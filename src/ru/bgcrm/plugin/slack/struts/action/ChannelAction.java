package ru.bgcrm.plugin.slack.struts.action;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.plugin.slack.DefaultProcessorFunctions;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;

public class ChannelAction extends ru.bgcrm.struts.action.BaseAction {
    
    public ActionForward addProcessChannelLink(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Process process = new ProcessDAO(conSet.getConnection()).getProcess(form.getParamInt("processId"));
        if (process == null)
            throw new BGIllegalArgumentException();

        new DefaultProcessorFunctions().linkChannel(process);

        return status(conSet, form);
    }
}