package org.bgerp.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.app.exception.BGMessageExceptionWithoutL10n;
import org.bgerp.dao.param.ParamValueDAO;
import org.bgerp.dao.process.Order;
import org.bgerp.dao.process.ProcessLinkProcessSearchDAO;
import org.bgerp.dao.process.ProcessSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.link.ProcessLinkProcess;
import org.bgerp.model.process.link.config.ProcessCreateLinkConfig;
import org.bgerp.model.process.link.config.ProcessLinkCategoryConfig;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.link.LinkRemovedEvent;
import ru.bgcrm.event.link.LinkRemovingEvent;
import ru.bgcrm.event.process.ProcessCreatedAsLinkEvent;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.IfaceState;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.LinkAction;
import ru.bgcrm.struts.action.ProcessLinkAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.SingleConnectionSet;

@Action(path = "/user/process/link/process")
public class ProcessLinkProcessAction extends ProcessLinkAction {
    private static final String PATH_JSP = PATH_JSP_USER + "/process/process/link/process";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        restoreRequestParams(conSet.getConnection(), form, true, true, "open");

        IfaceState currentState = new IfaceState(form);
        if (Utils.notBlankString(currentState.getIfaceId())) {
            Pair<Integer, Integer> counts = new ProcessLinkDAO(conSet.getSlaveConnection()).getLinkedProcessesCounts(form.getId());

            IfaceState newState = new IfaceState(Process.OBJECT_TYPE, form.getId(), form,
                    String.valueOf(counts.getFirst()),
                    String.valueOf(counts.getSecond()));
            new IfaceStateDAO(conSet.getConnection()).compareAndUpdateState(currentState, newState, form);
        }

        Process process = getProcess(new ProcessDAO(conSet.getSlaveConnection()), form.getId());
        var processType = getProcessType(process.getTypeId());

        form.setRequestAttribute("config", processType.getProperties().getConfigMap().getConfig(ProcessLinkCategoryConfig.class));

        var createTypeList = processType.getProperties().getConfigMap()
            .getConfig(ProcessCreateLinkConfig.class)
            .getItemList(form, conSet.getSlaveConnection(), process);

        form.setRequestAttribute("createTypeList", createTypeList);

        return html(conSet, form, PATH_JSP + "/default.jsp");
    }

    public ActionForward showCategory(DynActionForm form, ConnectionSet conSet) throws Exception {
        var processType = getProcessType(getProcess(new ProcessDAO(conSet.getSlaveConnection()), form.getId()).getTypeId());
        int categoryId = form.getParamInt("categoryId", Utils::isPositive);
        var category = processType.getProperties().getConfigMap().getConfig(ProcessLinkCategoryConfig.class).getCategories().get(categoryId);

        var pageable = new Pageable<Pair<String, Process>>(form);

        ProcessLinkProcessSearchDAO dao = new ProcessLinkProcessSearchDAO(conSet.getConnection(), form)
            .withType(category.getProcessTypeIds())
            .withOpen(form.getParamBoolean("open", null))
            .withLinkType(Set.of(category.getLinkType()));
        dao.search(pageable, category.isLink(), form.getId());

        form.setRequestAttribute("category", category);

        return html(conSet, form, PATH_JSP + "/show_category.jsp");
    }

    public ActionForward addCreated(DynActionForm form, ConnectionSet conSet) throws Exception {
        Process process = getProcess(new ProcessDAO(conSet.getSlaveConnection()), form.getId());
        ProcessType processType = getProcessType(process.getTypeId());

        var createTypeList = processType.getProperties().getConfigMap()
            .getConfig(ProcessCreateLinkConfig.class)
            .getItemList(form, conSet.getSlaveConnection(), process);

        form.setRequestAttribute("createTypeList", createTypeList);

        return html(conSet, form, PATH_JSP + "/add_created.jsp");
    }

    public ActionForward linkProcessCreate(DynActionForm form, Connection con) throws Exception {
        int id = form.getId();

        // process type + link type, the way is not really used
        int typeId = form.getParamInt("typeId", -1);
        String objectType = form.getParam("objectType", "");

        // ID from configuration
        int createTypeId = form.getParamInt("createTypeId", -1);

        String description = Utils.maskNull(form.getParam("description"));

        Process linkedProcess = getProcess(new ProcessDAO(con), id);
        linkProcessCreate(con, form, linkedProcess, typeId, objectType, createTypeId, description, form.getParamInt("groupId", -1));

        return json(con, form);
    }

    public static Process linkProcessCreate(Connection con, DynActionForm form, Process linkedProcess, int typeId, String linkObjectType,
            int createTypeId, String description, int groupId) throws Exception {
        final ProcessLinkDAO linkDao = new ProcessLinkDAO(con);

        int linkedId = linkedProcess.getId();

        Process process = new Process();
        if (createTypeId > 0) {
            ProcessType linkedType = getProcessType(linkedProcess.getTypeId());

            var itemPair = linkedType.getProperties().getConfigMap()
                .getConfig(ProcessCreateLinkConfig.class)
                .getItem(form, con, linkedProcess,createTypeId);
            if (itemPair == null)
                throw new BGMessageException("Не найдено правило с ID: {}", createTypeId);

            final var item = itemPair.getFirst();

            if (!itemPair.getSecond())
                throw new BGMessageExceptionWithoutL10n(item.getCheckErrorMessage());

            linkObjectType = item.getLinkType();

            process.setTypeId(item.getProcessTypeId());
            process.setDescription(description);

            processCreate(form, con, process, groupId);

            String copyParams = item.getCopyParamsMapping();
            if ("all".equals(copyParams)) {
                ProcessType type = getProcessType(process.getTypeId());
                List<Integer> paramIds = type.getProperties().getParameterIds();
                List<Integer> linkedParamIds = linkedType.getProperties().getParameterIds();
                List<Integer> paramIdsBothHave = new ArrayList<>(linkedParamIds);
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

        linkDao.addLink(new ProcessLinkProcess(linkedId, linkObjectType, process.getId()));

        EventProcessor.processEvent(new ProcessCreatedAsLinkEvent(form, linkedProcess, process), new SingleConnectionSet(con));

        return process;
    }

    public ActionForward addExisting(DynActionForm form, ConnectionSet conSet) throws Exception {
        var processType = getProcessType(getProcess(new ProcessDAO(conSet.getSlaveConnection()), form.getId()).getTypeId());
        int categoryId = form.getParamInt("categoryId", Utils::isPositive);
        var category = processType.getProperties().getConfigMap().getConfig(ProcessLinkCategoryConfig.class).getCategories().get(categoryId);
        Set<Integer> bufferProcessIds = form.getParamValues("bufferProcessId");

        var processes = new Pageable<Process>(form);
        processes.getPage().setPageSize(100);

        Set<Integer> excludeIds = alreadyLinked(form.getId(), conSet.getSlaveConnection(), category.isLink(), category.getLinkType());

        new ProcessSearchDAO(conSet.getSlaveConnection(), form)
            .withType(category.getProcessTypeIds())
            .withOpen(form.getParamBoolean("open", null))
            .withStatus(category.getAddProcessStatusIds())
            .withExcludeIds(excludeIds)
            .withIdOrDescriptionLike(LikePattern.SUB.get(form.getParam("filter")))
            .order(category.getAddProcessStatusIds().isEmpty() ? Order.DESCRIPTION : new Order.StatusesDescription(category.getAddProcessStatusIds()))
            .search(processes);

        var listBuffer = new ArrayList<>(bufferProcessIds.size());
        form.setResponseData("listBuffer", listBuffer);

        var listIt = processes.getList().iterator();
        while (listIt.hasNext()) {
            var p = listIt.next();
            if (bufferProcessIds.contains(p.getId())) {
                listBuffer.add(p);
                listIt.remove();
            }
        }

        return html(conSet, form, PATH_JSP + "/add_existing.jsp");
    }

    public ActionForward linkProcessExisting(DynActionForm form, ConnectionSet conSet) throws Exception {
        var processType = getProcessType(getProcess(new ProcessDAO(conSet.getSlaveConnection()), form.getId()).getTypeId());
        int categoryId = form.getParamInt("categoryId", Utils::isPositive);
        var category = processType.getProperties().getConfigMap().getConfig(ProcessLinkCategoryConfig.class).getCategories().get(categoryId);

        ProcessLinkDAO dao = new ProcessLinkDAO(conSet.getConnection());

        for (int processId : form.getParamValuesList("processId")) {
            var l = category.isLink() ?
                new ProcessLinkProcess(form.getId(), category.getLinkType(), processId) :
                new ProcessLinkProcess(processId, category.getLinkType(), form.getId());

            LinkAction.addLink(form, conSet.getConnection(), l);

            if (dao.checkCycles(l.getObjectId()))
                throw new BGMessageException(form.l.l("Циклическая зависимость"));
        }

        return json(conSet, form);
    }

    private Set<Integer> alreadyLinked(int processId, Connection con, boolean link, String linkObjectType) throws SQLException {
        var excluded = new Pageable<Pair<String, Process>>().withoutPagination();
        new ProcessLinkProcessSearchDAO(con).search(excluded, link, processId);

        return excluded.getList().stream()
            .filter(pair -> pair.getFirst().equals(linkObjectType))
            .map(pair -> pair.getSecond().getId())
            .collect(Collectors.toSet());
    }

    public ActionForward linkProcessDelete(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = new CommonObjectLink(Process.OBJECT_TYPE, form.getId(),
                form.getParam("linkObjectType", form.getParam("linkedObjectType")),
                form.getParamInt("linkObjectId", form.getParamInt("linkedObjectId")),
                "");
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() == 0 || Utils.isBlankString(link.getLinkObjectType())
                || link.getLinkObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        Connection con = conSet.getConnection();

        EventProcessor.processEvent(new LinkRemovingEvent(form, link), new SingleConnectionSet(con));

        new ProcessLinkDAO(con).deleteLink(link);

        EventProcessor.processEvent(new LinkRemovedEvent(form, link), new SingleConnectionSet(con));

        return json(conSet, form);
    }
}
