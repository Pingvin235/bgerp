package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.ProcessLinkProcessAction;
import org.bgerp.app.cfg.SimpleConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.process.Order;
import org.bgerp.dao.process.ProcessLinkSearchDAO;
import org.bgerp.dao.process.ProcessQueueDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.config.ProcessCreateInConfig;
import org.bgerp.model.process.queue.filter.Filter;
import org.bgerp.model.process.queue.filter.FilterLinkObject;
import org.bgerp.model.process.queue.filter.FilterList;
import org.bgerp.model.process.queue.filter.FilterOpenClose;
import org.bgerp.model.process.queue.filter.FilterProcessType;
import org.bgerp.util.Log;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.client.ProcessOpenEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.config.ProcessLinkModesConfig;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/process/link")
public class ProcessLinkAction extends ProcessAction {
    private static final Log log = Log.getLog();

    private static final String PATH_JSP = PATH_JSP_USER + "/process/process/link";

    // processes with a link
    public ActionForward linkedProcessList(DynActionForm form, ConnectionSet conSet) throws Exception {
        restoreRequestParams(conSet.getConnection(), form, true, true, "open");

        String objectType = form.getParam("objectType");
        int id = form.getId();

        Boolean open = form.getParamBoolean("open", null);
        Set<Integer> typeIds = form.getParamValues("typeId");

        Queue queue = ProcessQueueCache.getQueue(setup.getInt(objectType + ".processes.queue"));
        if (queue != null) {
            queue = queue.clone();

            FilterList filters = queue.getFilterList();
            filters.add(new FilterLinkObject(0, SimpleConfigMap.of(Filter.VALUES, String.valueOf(id)), objectType, FilterLinkObject.WHAT_FILTER_ID));

            if (open != null) {
                filters.add(new FilterOpenClose(0, SimpleConfigMap.of(Filter.VALUES, open ? FilterOpenClose.OPEN : FilterOpenClose.CLOSE)));
            }

            if (!typeIds.isEmpty()) {
                filters.add(new FilterProcessType(0, SimpleConfigMap.of(Filter.ON_EMPTY_VALUES, Utils.toString(typeIds))));
            }

            Pageable<Object[]> searchResult = new Pageable<>(form);

            new ProcessQueueDAO(conSet.getConnection(), form).searchProcess(searchResult, null, queue, form);

            final List<Object[]> list = searchResult.getList();

            HttpServletRequest request = form.getHttpRequest();
            request.setAttribute("columnList", queue.getMediaColumnList(Queue.MEDIA_HTML));
            queue.replaceRowsForMedia(form, Queue.MEDIA_HTML, list);
            request.setAttribute("queue", queue);
        } else {
            form.setRequestAttribute("customerLinkRoleConfig", setup.getConfig(ProcessLinkModesConfig.class));

            new ProcessLinkSearchDAO(conSet.getConnection(), form)
                .withLinkObjectTypeLike(LikePattern.START.get(objectType))
                .withLinkObjectId(id)
                .withType(typeIds)
                .withOpen(open)
                .order(Order.CREATE_DT_DESC)
                .searchWithLinkObjectTypes(new Pageable<Pair<Process, String>>(form));

            var processes = new Pageable<Process>().withoutPagination();
            new ProcessLinkSearchDAO(conSet.getSlaveConnection(), form)
                .withLinkObjectTypeLike(LikePattern.START.get(objectType))
                .withLinkObjectId(id)
                .search(processes);
            form.setResponseData("types", processTypes(processes.getList()));
        }

        // type tree for creation
        var typeList = processTypeIsolationFilter(ProcessTypeCache.getTypeList("linked", objectType, null), form);
        form.setRequestAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot().sub(typeList));

        return html(conSet, form, PATH_JSP + "/linked_process_list.jsp");
    }

    // создание процесса с привязанной сущностью
    public ActionForward linkedProcessCreate(DynActionForm form, Connection con) throws Exception {
        String objectType = form.getParam("objectType");
        int id = form.getId();
        String objectTitle = form.getParam("objectTitle");

        Process process = ProcessAction.processCreateAndGet(form, con);

        CommonObjectLink link = new CommonObjectLink(Process.OBJECT_TYPE, process.getId(), objectType, id, objectTitle);

        EventProcessor.processEvent(new LinkAddingEvent(form, link), new SingleConnectionSet(con));

        new ProcessLinkDAO(con).addLink(link);

        EventProcessor.processEvent(new LinkAddedEvent(form, link), new SingleConnectionSet(con));

        ProcessType type = ProcessTypeCache.getProcessTypeOrThrow(form.getParamInt("typeId", 0));
        ProcessCreateInConfig config = type.getProperties().getConfigMap().getConfig(ProcessCreateInConfig.class);

        new ParamValueDAO(con).copyParams(id, process.getId(), config.getCopyParams());

        if (config.openCreated(objectType)
                || (type.getProperties().getWizard() != null && type.getProperties().getWizard().getCreateStepList().size() > 0))
            form.getResponse().addEvent(new ProcessOpenEvent(process.getId()));

        return json(con, form);
    }

    @Deprecated
    public static Process linkProcessCreate(Connection con, DynActionForm form, Process linkedProcess, int typeId, String objectType,
            int createTypeId, String description, int groupId) throws Exception {
        log.warnd("linkProcessCreate", "ProcessLinkProcessAction.linkProcessCreate");
        return ProcessLinkProcessAction.linkProcessCreate(con, form, linkedProcess, typeId, objectType, createTypeId, description, groupId);
    }
}
