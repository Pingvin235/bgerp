package org.bgerp.action.open;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.bgerp.Interface;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.dao.message.MessageSearchDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.SecretExpression;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.message.Message;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/open/process")
public class ProcessAction extends BaseAction {
    private static final String PATH_JSP =  PATH_JSP_OPEN + "/process";

    /**
     * Configuration for open processes.
     */
    public static class Config extends ru.bgcrm.util.Config {
        /**
         * Process types IDs allowed to be shown. Not {@code null} value.
         */
        private final Set<Integer> processTypeIds;
        private final List<Integer> showParamIds;
        private final Set<Integer> showMessagesTagIds;
        private final boolean showLinkCustomer;
        private final SecretExpression secret;

        protected Config(ParameterMap setup, boolean validate) throws Exception {
            super(setup);
            processTypeIds = Utils.toIntegerSet(setup.get("process.open.typeIds"));
            initWhen(CollectionUtils.isNotEmpty(processTypeIds));

            secret = new SecretExpression(setup.get("process.open.secret.expression"));

            showParamIds = Utils.toIntegerList(setup.get("process.open.show.paramIds"));
            if (showParamIds.isEmpty())
                throwValidationException("Param ID list is not defined");

            if (ParameterCache.getParameterList(showParamIds).size() != showParamIds.size())
                throwValidationException("Some of param IDs do not exist");

            var messageTagIds = setup.get("process.open.show.message.tagIds");
            showMessagesTagIds = "*".equals(messageTagIds) ? Collections.emptySet() : Utils.toIntegerSet(messageTagIds);
            showLinkCustomer = setup.getBoolean("process.open.show.link.customer", false);
        }

        /**
         * Checks if process can be open. Used in 'used' interface only.
         * @param process
         * @return
         */
        public boolean isOpenForUser(Process process) {
            return process != null && processTypeIds.contains(process.getTypeId());
        }

        /**
         * Checks if process can be open respecting secrets.
         * @param process
         * @param form
         * @return
         */
        public boolean isOpen(Process process, DynActionForm form) {
            return isOpenForUser(process) && secret.check(process, form);
        }

        /**
         * Process accessing URL.
         * @param process
         * @return
         */
        public String url(Process process) {
            return Interface.getUrlOpen(setup) + "/process/" + process.getId() + secret.queryString(process);
        }

        /**
         * Process param IDs allowed to be shown.
         * @return not {@code null} value.
         */
        public List<Integer> getShowParamIds() {
            return showParamIds;
        }

        /**
         * Message tag IDs allowed to be shown.
         * @return {@code null} - message are hidden, empty set - all messages are shown.
         */
        public Set<Integer> getShowMessagesTagIds() {
            return showMessagesTagIds;
        }

        /**
         * @return show customer links.
         */
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
        if (config == null || !config.isOpen(process, form))
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
        if (config == null || !config.isOpen(process, form) || config.getShowMessagesTagIds() == null)
            return null;

        var result = new SearchResult<Message>(form);
        result.getPage().setPageIndex(Page.PAGE_INDEX_NO_PAGING);

        new MessageSearchDAO(conSet.getSlaveConnection())
            .withProcessIds(Set.of(form.getId()))
            .withTagIds(config.getShowMessagesTagIds())
            .search(result);

        return html(conSet, null, PATH_JSP + "/messages.jsp");
    }
}