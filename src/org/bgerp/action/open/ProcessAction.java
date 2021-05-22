package org.bgerp.action.open;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.message.MessageDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class ProcessAction extends BaseAction {
    private static final String PATH_JSP =  PATH_JSP_OPEN + "/process";

    /**
     * Configuration for open processes.
     */
    public static class Config extends ru.bgcrm.util.Config {
        private final Set<Integer> processTypeIds;
        private final List<Integer> showParamIds;
        private final Set<Integer> showMessagesTagIds;
        private final boolean showLinkCustomer;

        protected Config(ParameterMap setup, boolean validate) throws Exception {
            super(setup);
            processTypeIds = Utils.toIntegerSet(setup.get("process.open.typeIds"));
            initWhen(CollectionUtils.isNotEmpty(processTypeIds));

            showParamIds = Utils.toIntegerList(setup.get("process.open.show.paramIds"));
            if (showParamIds.isEmpty())
                throwValidationException("Param ID list is not defined");

            if (ParameterCache.getParameterList(showParamIds).size() != showParamIds.size())
                throwValidationException("Some of param IDs do not exist");
            
            showMessagesTagIds = Utils.toIntegerSet(setup.get("process.open.show.message.tagIds"));
            showLinkCustomer = setup.getBoolean("process.open.show.link.customer", false);
        }

        public boolean isOpen(Process process) {
            return process != null && processTypeIds.contains(process.getTypeId());
        }

        public Set<Integer> getProcessTypeIds() {
            return processTypeIds;
        }

        public List<Integer> getShowParamIds() {
            return showParamIds;
        }

        public Set<Integer> getShowMessagesTagIds() {
            return showMessagesTagIds;
        }

        public boolean isShowLinkCustomer() {
            return showLinkCustomer;
        }
    }

    public ActionForward show(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        var config = setup.getConfig(Config.class);
        if (config == null) 
            return null;

        var con = conSet.getSlaveConnection();

        var process = new ProcessDAO(con).getProcess(form.getId());
        if (config == null || !config.isOpen(process))
            return null;

        var type = ru.bgcrm.struts.action.ProcessAction.getProcessType(process.getTypeId());
        var refConfig = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
        process.setReference(refConfig.getReference(con, form, process, "open.processCard"));

        form.getHttpRequest().setAttribute("config", config);
        form.setResponseData("process", process);

        return html(conSet, null, PATH_JSP + "/show.jsp");
    }

    public ActionForward messages(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        var config = setup.getConfig(Config.class);
        var process = new ProcessDAO(conSet.getSlaveConnection()).getProcess(form.getId());
        if (config == null || !config.isOpen(process)
            || config.getShowMessagesTagIds().isEmpty()) 
            return null;

        var dao = new MessageDAO(conSet.getSlaveConnection());

        var result = new SearchResult<Message>(form);
        result.getPage().setPageIndex(Page.PAGE_INDEX_NO_PAGING);

        dao.searchMessageList(result,
                form.getId(), null, null, null, null,
                null, null, null, true, config.getShowMessagesTagIds());
        
        return html(conSet, null, PATH_JSP + "/messages.jsp");
    }
}