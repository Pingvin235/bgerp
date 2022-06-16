package ru.bgcrm.struts.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.client.ProcessOpenEvent;
import ru.bgcrm.event.link.LinkAddedEvent;
import ru.bgcrm.event.link.LinkAddingEvent;
import ru.bgcrm.event.process.ProcessCreatedAsLinkEvent;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.IfaceState;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessLinkProcess;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.config.LinkProcessCreateConfig;
import ru.bgcrm.model.process.config.LinkProcessCreateConfigItem;
import ru.bgcrm.model.process.config.ProcessReferenceConfig;
import ru.bgcrm.model.process.queue.Filter;
import ru.bgcrm.model.process.queue.FilterLinkObject;
import ru.bgcrm.model.process.queue.FilterList;
import ru.bgcrm.model.process.queue.FilterOpenClose;
import ru.bgcrm.model.process.queue.FilterProcessType;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/process/link")
public class ProcessLinkAction extends ProcessAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/process/process";

    // процессы, к которым привязана сущность
    public ActionForward linkedProcessList(DynActionForm form, Connection con) throws Exception {
        ProcessLinkDAO processLinkDAO = new ProcessLinkDAO(con, form.getUser());

        restoreRequestParams(con, form, true, true, "open");

        String objectType = form.getParam("objectType");
        int id = form.getId();

        Boolean paramOpen = form.getParamBoolean("open", null);
        Set<Integer> paramProcessTypeId = form.getSelectedValues("typeId");

        Queue queue = ProcessQueueCache.getQueue(setup.getInt(objectType + ".processes.queue"));
        if (queue != null) {
            queue = queue.clone();

            FilterList filters = queue.getFilterList();
            filters.add(new FilterLinkObject(0, ParameterMap.of(Filter.VALUES, String.valueOf(id)), objectType, FilterLinkObject.WHAT_FILTER_ID));

            if (paramOpen != null) {
                filters.add(new FilterOpenClose(0, ParameterMap.of(Filter.VALUES, paramOpen ? FilterOpenClose.OPEN : FilterOpenClose.CLOSE)));
            }

            if (!paramProcessTypeId.isEmpty()) {
                filters.add(new FilterProcessType(0, ParameterMap.of(Filter.ON_EMPTY_VALUES, Utils.toString(paramProcessTypeId))));
            }

            Pageable<Object[]> searchResult = new Pageable<Object[]>(form);

            ProcessDAO processDAO = new ProcessDAO(con, form.getUser());
            processDAO.searchProcess(searchResult, null, queue, form);

            final List<Object[]> list = searchResult.getList();

            HttpServletRequest request = form.getHttpRequest();
            request.setAttribute("columnList", queue.getMediaColumnList(Queue.MEDIA_HTML));
            queue.processDataForMedia(form, Queue.MEDIA_HTML, list);
            request.setAttribute("queue", queue);
        } else {
            Pageable<Pair<String, Process>> searchResult = new Pageable<Pair<String, Process>>(form);
            processLinkDAO.searchLinkedProcessList(searchResult, CommonDAO.getLikePatternStart(objectType), id, null,
                    paramProcessTypeId, form.getSelectedValues("statusId"), form.getParam("paramFilter"),
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

    private void setProcessReference(Connection con, DynActionForm form, Process process, String objectType) {
        try {
            ProcessType type = ProcessTypeCache.getProcessType(process.getTypeId());
            if (type != null) {
                ProcessReferenceConfig config = type.getProperties().getConfigMap().getConfig(ProcessReferenceConfig.class);
                process.setReference(config.getReference(con, form, process, objectType));
            }
        } catch (Exception e) {
            process.setReference(e.getMessage());
            log.error(e);
        }
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
        ParameterMap configMap = type.getProperties().getConfigMap();

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

    // процессы, привязанные к процессу
    public ActionForward linkProcessList(DynActionForm form, Connection con) throws Exception {
        HttpServletRequest request = form.getHttpRequest();
        ProcessLinkDAO processLinkDao = new ProcessLinkDAO(con, form.getUser());

        restoreRequestParams(con, form, true, true, "open");

        int id = form.getId();
        Boolean open = form.getParamBoolean("open", null);

        Process process = getProcess(new ProcessDAO(con), id);
        ProcessType type = getProcessType(process.getTypeId());

        request.setAttribute("processType", type);

        // жёстко указанные в конфигурации типы процессов, с указанными видами привязки, фильтры по параметру процесса и т.п.
        final List<LinkProcessCreateConfigItem> createTypeList = type.getProperties().getConfigMap().getConfig(LinkProcessCreateConfig.class)
                .getItemList(con, process);

        request.setAttribute("createTypeList", createTypeList);

        // список процессов, к которым привязан данный процесс
        Pageable<Pair<String, Process>> searchResultLinked = new Pageable<>();
        processLinkDao.searchLinkedProcessList(searchResultLinked, Process.OBJECT_TYPE + "%", id, null, null, null, null, open);
        form.getResponse().setData("linkedProcessList", searchResultLinked.getList());

        // генерация описаний процессов
        for (Pair<String, Process> pair : searchResultLinked.getList()) {
            setProcessReference(con, form, pair.getSecond(), form.getParam("linkedReferenceName"));
        }

        // привязанные к процессу процессы
        Pageable<Pair<String, Process>> searchResultLink = new Pageable<Pair<String, Process>>(form);
        processLinkDao.searchLinkProcessList(searchResultLink, id, open);

        // генерация описаний процессов
        for (Pair<String, Process> pair : searchResultLink.getList()) {
            setProcessReference(con, form, pair.getSecond(), form.getParam("linkReferenceName"));
        }

        // проверка и обновление статуса вкладки, если нужно
        if (Strings.isNotBlank(form.getParam(IfaceState.REQUEST_PARAM_IFACE_ID))) {
            IfaceState ifaceState = new IfaceState(form);
            IfaceState currentState = new IfaceState(Process.OBJECT_TYPE, id, form,
                    String.valueOf(searchResultLinked.getPage().getRecordCount()),
                    String.valueOf(searchResultLink.getPage().getRecordCount()));
            new IfaceStateDAO(con).compareAndUpdateState(ifaceState, currentState, form);
        }

        return html(con, form, PATH_JSP + "/link_process_list.jsp");
    }

    // создание процесса, привязанного к процессу
    public ActionForward linkProcessCreate(DynActionForm form, Connection con) throws Exception {
        int id = form.getId();

        // либо тип процесса + тип отношений
        int typeId = form.getParamInt("typeId", -1);
        String objectType = form.getParam("objectType", "");

        // либо код из конфигурации
        int createTypeId = form.getParamInt("createTypeId", -1);

        String description = Utils.maskNull(form.getParam("description"));

        Process linkedProcess = getProcess(new ProcessDAO(con), id);
        linkProcessCreate(con, form, linkedProcess, typeId, objectType, createTypeId, description, form.getParamInt("groupId", -1));

        return json(con, form);
    }

    public static Process linkProcessCreate(Connection con, DynActionForm form, Process linkedProcess, int typeId, String linkType,
            int createTypeId, String description, int groupId) throws Exception {
        final ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        int linkedId = linkedProcess.getId();

        Process process = new Process();
        if (createTypeId > 0) {
            ProcessType linkedType = getProcessType(linkedProcess.getTypeId());

            LinkProcessCreateConfigItem item = linkedType.getProperties().getConfigMap().getConfig(LinkProcessCreateConfig.class)
                    .getItem(createTypeId);
            if (item == null) {
                throw new BGMessageException("Не найдено правило с ID: %s", createTypeId);
            }

            linkType = item.getLinkType();

            process.setTypeId(item.getProcessTypeId());
            process.setDescription(description);

            processCreate(form, con, process, groupId);

            String copyParams = item.getCopyParamsMapping();
            if ("all".equals(copyParams)) {
                ProcessType type = getProcessType(process.getTypeId());
                List<Integer> paramIds = type.getProperties().getParameterIds();
                List<Integer> linkedParamIds = linkedType.getProperties().getParameterIds();
                List<Integer> paramIdsBothHave = new ArrayList<Integer>(linkedParamIds);
                paramIdsBothHave.retainAll(paramIds);

                new ParamValueDAO(con).copyParams(linkedId, process.getId(), StringUtils.join(paramIdsBothHave, ","));
            } else {
                new ParamValueDAO(con).copyParams(linkedId, process.getId(), copyParams);
            }

            String copyLinks = item.getCopyLinks();

            // пока копирование сразу всех привязок
            if (Utils.notBlankString(copyLinks)) {
                if (copyLinks.equals("1")) {
                    linkDao.copyLinks(linkedId, process.getId(), null, Process.OBJECT_TYPE + "%");
                } else {
                    linkDao.copyLinks(linkedId, process.getId(), copyLinks, Process.OBJECT_TYPE + "%");
                }
            }
        } else {
            process.setTypeId(typeId);
            process.setDescription(description);

            processCreate(form, con, process, -1);
        }

        linkDao.addLink(new ProcessLinkProcess(linkedId, linkType, process.getId()));

        EventProcessor.processEvent(new ProcessCreatedAsLinkEvent(form, linkedProcess, process), new SingleConnectionSet(con));

        return process;
    }

}
