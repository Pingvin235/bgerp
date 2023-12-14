package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForward;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.SimpleConfigMap;
import org.bgerp.dao.process.ProcessQueueDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.queue.filter.Filter;
import org.bgerp.model.process.queue.filter.FilterLinkObject;
import org.bgerp.model.process.queue.filter.FilterList;
import org.bgerp.model.process.queue.filter.FilterOpenClose;
import org.bgerp.model.process.queue.filter.FilterProcessType;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.client.ProcessOpenEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/process/link")
public class ProcessLinkAction extends ProcessAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/process/process/link";

    // процессы, к которым привязана сущность
    public ActionForward linkedProcessList(DynActionForm form, Connection con) throws Exception {
        ProcessLinkDAO processLinkDAO = new ProcessLinkDAO(con, form);

        restoreRequestParams(con, form, true, true, "open");

        String objectType = form.getParam("objectType");
        int id = form.getId();

        Boolean paramOpen = form.getParamBoolean("open", null);
        Set<Integer> paramProcessTypeId = form.getParamValues("typeId");

        Queue queue = ProcessQueueCache.getQueue(setup.getInt(objectType + ".processes.queue"));
        if (queue != null) {
            queue = queue.clone();

            FilterList filters = queue.getFilterList();
            filters.add(new FilterLinkObject(0, SimpleConfigMap.of(Filter.VALUES, String.valueOf(id)), objectType, FilterLinkObject.WHAT_FILTER_ID));

            if (paramOpen != null) {
                filters.add(new FilterOpenClose(0, SimpleConfigMap.of(Filter.VALUES, paramOpen ? FilterOpenClose.OPEN : FilterOpenClose.CLOSE)));
            }

            if (!paramProcessTypeId.isEmpty()) {
                filters.add(new FilterProcessType(0, SimpleConfigMap.of(Filter.ON_EMPTY_VALUES, Utils.toString(paramProcessTypeId))));
            }

            Pageable<Object[]> searchResult = new Pageable<Object[]>(form);

            new ProcessQueueDAO(con, form).searchProcess(searchResult, null, queue, form);

            final List<Object[]> list = searchResult.getList();

            HttpServletRequest request = form.getHttpRequest();
            request.setAttribute("columnList", queue.getMediaColumnList(Queue.MEDIA_HTML));
            queue.replaceRowsForMedia(form, Queue.MEDIA_HTML, list);
            request.setAttribute("queue", queue);
        } else {
            Pageable<Pair<String, Process>> searchResult = new Pageable<Pair<String, Process>>(form);
            processLinkDAO.searchLinkedProcessList(searchResult, LikePattern.START.get(objectType), id, null,
                    paramProcessTypeId, form.getParamValues("statusId"), form.getParam("paramFilter"),
                    paramOpen);

            // generate references
            for (Pair<String, Process> pair : searchResult.getList())
                setProcessReference(con, form, pair.getSecond(), objectType);
        }

        // filter type list
        form.getResponse().setData("typeList", processLinkDAO.getLinkedProcessTypeIdList(objectType, id));

        // type tree for creation
        var typeList = ProcessTypeCache.getTypeList(objectType);
        applyProcessTypePermission(typeList, form);
        form.getHttpRequest().setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot().sub(typeList));

        return html(con, form, PATH_JSP + "/linked_process_list.jsp");
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

        // копирование параметров
        ProcessType type = ProcessTypeCache.getProcessType(form.getParamInt("typeId", 0));
        ConfigMap configMap = type.getProperties().getConfigMap();

        new ParamValueDAO(con).copyParams(id, process.getId(), configMap.get("create.in.copyParams"));

        final String key = "create.in." + objectType + ".openCreated";
        if ("wizard".equals(configMap.get(key)) ||
            configMap.getBoolean("create.in." + objectType + ".wizardCreated", false)) {
            form.getResponse().setData("wizard", 1);
            form.getResponse().getEventList().clear();
        } else if (configMap.getBoolean(key, true)) {
            form.getResponse().addEvent(new ProcessOpenEvent(process.getId()));
        }

        return json(con, form);
    }
}
