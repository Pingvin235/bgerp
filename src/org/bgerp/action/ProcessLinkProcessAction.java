package org.bgerp.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.dao.process.Order;
import org.bgerp.dao.process.ProcessLinkProcessSearchDAO;
import org.bgerp.dao.process.ProcessSearchDAO;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.config.CommonAvailableConfig;
import org.bgerp.model.process.config.LinkAvailableConfig;
import org.bgerp.model.process.config.LinkProcessCreateConfig;
import org.bgerp.model.process.config.LinkProcessCreateConfigItem;
import org.bgerp.model.process.config.LinkedAvailableConfig;

import ru.bgcrm.dao.IfaceStateDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.link.LinkRemovedEvent;
import ru.bgcrm.event.link.LinkRemovingEvent;
import ru.bgcrm.event.process.ProcessCreatedAsLinkEvent;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.IfaceState;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessLinkProcess;
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

        if (Utils.notBlankString(form.getParam(IfaceState.REQUEST_PARAM_IFACE_ID))) {
            Pair<Integer, Integer> counts = new ProcessLinkDAO(conSet.getSlaveConnection()).getLinkedProcessesCounts(form.getId());

            IfaceState currentState = new IfaceState(form);
            IfaceState newState = new IfaceState(Process.OBJECT_TYPE, form.getId(), form,
                    String.valueOf(counts.getFirst()),
                    String.valueOf(counts.getSecond()));
            new IfaceStateDAO(conSet.getConnection()).compareAndUpdateState(currentState, newState, form);
        }

        return html(conSet, form, PATH_JSP + "/default.jsp");
    }

    public ActionForward linkedProcessList(DynActionForm form, ConnectionSet conSet) throws Exception {
        Pageable<Pair<String, Process>> pageable = new Pageable<>(form);

        new ProcessLinkProcessSearchDAO(conSet.getConnection(), form)
            .withOpen(form.getParamBoolean("open", null))
            .order(Order.DESCRIPTION)
            .search(pageable, false, form.getId());

        setReferences(form, conSet.getSlaveConnection(), pageable);
        // TODO: Attributes.

        return html(conSet, form, PATH_JSP + "/linked_list.jsp");
    }

    public ActionForward linkProcessList(DynActionForm form, ConnectionSet conSet) throws Exception {
        Pageable<Pair<String, Process>> pageable = new Pageable<>(form);

        new ProcessLinkProcessSearchDAO(conSet.getConnection(), form)
            .withOpen(form.getParamBoolean("open", null))
            .order(Order.DESCRIPTION)
            .search(pageable, true, form.getId());

        setReferences(form, conSet.getSlaveConnection(), pageable);
        setRequestAttributes(form, conSet.getSlaveConnection());

        return html(conSet, form, PATH_JSP + "/link_list.jsp");
    }

    private void setReferences(DynActionForm form, Connection con, Pageable<Pair<String, Process>> pageable) {
        for (var pair : pageable.getList())
            setProcessReference(con, form, pair.getSecond(), form.getAction());
    }

    private void setRequestAttributes(DynActionForm form, Connection con) throws Exception {
        Process process = getProcess(new ProcessDAO(con), form.getId());
        ProcessType type = getProcessType(process.getTypeId());

        form.setRequestAttribute("processType", type);

        var createTypeList = type.getProperties().getConfigMap()
            .getConfig(LinkProcessCreateConfig.class)
            .getItemList(con, process);

        form.setRequestAttribute("createTypeList", createTypeList);
    }

    public ActionForward linkedProcessAvailable(DynActionForm form, Connection con) throws Exception {
        return linkOrLinkedProcessAvailable(form, con, false);
    }

    public ActionForward linkProcessAvailable(DynActionForm form, Connection con) throws Exception {
        return linkOrLinkedProcessAvailable(form, con, true);
    }

    private ActionForward linkOrLinkedProcessAvailable(DynActionForm form, Connection con, boolean link) throws Exception {
        String linkObjectType = form.getParam("objectType", Utils::notBlankString);

        Set<Integer> excludedIds = alreadyLinked(form.getId(), con, link, linkObjectType);

        Stream<IdTitle> buffer = form.getSelectedValuesStr("process").stream()
            .map(idTitle -> new IdTitle(
                Utils.parseInt(StringUtils.substringBefore(idTitle, ":")),
                StringUtils.substringAfter(idTitle, ":")
            ));

        Stream<IdTitle> available = available(form.getId(), con, link, linkObjectType);

        List<IdTitle> list = Stream.concat(buffer, available)
            .filter(item -> item.getId() != form.getId() && !excludedIds.contains(item.getId()))
            // .sorted((i1, i2) -> i1.getTitle().compareTo(i2.getTitle()))
            .collect(Collectors.toList());

        form.setResponseData("list", list);

        return html(con, form, PATH_JSP + "/add_existing_available.jsp");
    }

    private Set<Integer> alreadyLinked(int processId, Connection con, boolean link, String linkObjectType) throws SQLException {
        var excluded = new Pageable<Pair<String, Process>>().withoutPagination();
        new ProcessLinkProcessSearchDAO(con).search(excluded, link, processId);

        return excluded.getList().stream()
            .filter(pair -> pair.getFirst().equals(linkObjectType))
            .map(pair -> pair.getSecond().getId())
            .collect(Collectors.toSet());
    }

    private Stream<IdTitle> available(int processId, Connection con, boolean link, String linkObjectType) throws Exception {
        var processType = getProcess(new ProcessDAO(con), processId).getType();
        var configMap = processType.getProperties().getConfigMap();

        CommonAvailableConfig config = link ? configMap.getConfig(LinkAvailableConfig.class) : configMap.getConfig(LinkedAvailableConfig.class);
        if (config == null)
            return Stream.empty();

        var processes = new Pageable<Process>().withoutPagination();

        for (var rule : config.rules(linkObjectType)) {
            new ProcessSearchDAO(con)
                .withOpen(rule.open)
                .withType(rule.typeIds)
                .withStatus(rule.statusIds)
                .order(Order.DESCRIPTION)
                .search(processes);
        }

        return processes.getList().stream().map(p -> new IdTitle(p.getId(), p.getDescription()));
    }

    public ActionForward linkedProcessAdd(DynActionForm form, Connection con) throws Exception {
        return linkOrLinkedProcessAdd(form, con, false);
    }

    public ActionForward linkProcessAdd(DynActionForm form, Connection con) throws Exception {
        return linkOrLinkedProcessAdd(form, con, true);
    }

    private ActionForward linkOrLinkedProcessAdd(DynActionForm form, Connection con, boolean link) throws Exception {
        int id = form.getId();
        String linkObjectType = form.getParam("objectType", Utils::notBlankString);

        ProcessLinkDAO dao = new ProcessLinkDAO(con);

        for (int processId : form.getSelectedValuesList("processId")) {
            var l = link ? new ProcessLinkProcess(id, linkObjectType, processId) : new ProcessLinkProcess(processId, linkObjectType, id);

            LinkAction.addLink(form, con, l);

            if (dao.checkCycles(l.getObjectId()))
                throw new BGMessageException(form.l.l("Циклическая зависимость"));
        }

        return json(con, form);
    }

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

    public static Process linkProcessCreate(Connection con, DynActionForm form, Process linkedProcess, int typeId, String linkObjectType,
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

            linkObjectType = item.getLinkType();

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

        linkDao.addLink(new ProcessLinkProcess(linkedId, linkObjectType, process.getId()));

        EventProcessor.processEvent(new ProcessCreatedAsLinkEvent(form, linkedProcess, process), new SingleConnectionSet(con));

        return process;
    }

    public ActionForward linkProcessDelete(DynActionForm form, ConnectionSet conSet) throws Exception {
        CommonObjectLink link = new CommonObjectLink(Process.OBJECT_TYPE, form.getId(), form.getParam("linkedObjectType"),
                form.getParamInt("linkedObjectId"), "");
        if (Utils.isBlankString(link.getObjectType()) || link.getObjectId() == 0 || Utils.isBlankString(link.getLinkedObjectType())
                || link.getLinkedObjectId() <= 0) {
            throw new BGIllegalArgumentException();
        }

        Connection con = conSet.getConnection();

        EventProcessor.processEvent(new LinkRemovingEvent(form, link), new SingleConnectionSet(con));

        new ProcessLinkDAO(con).deleteLink(link);

        EventProcessor.processEvent(new LinkRemovedEvent(form, link), new SingleConnectionSet(con));

        return json(conSet, form);
    }
}
