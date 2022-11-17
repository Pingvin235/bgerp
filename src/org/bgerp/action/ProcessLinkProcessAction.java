package org.bgerp.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.model.Pageable;
import org.bgerp.model.process.config.LinkProcessCreateConfig;
import org.bgerp.model.process.config.LinkProcessCreateConfigItem;

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
import ru.bgcrm.model.IfaceState;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessLinkProcess;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.servlet.ActionServlet.Action;
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

        new ProcessLinkDAO(conSet.getConnection(), form).searchLinkedProcessList(pageable, Process.OBJECT_TYPE + "%", form.getId(), null, null, null,
                null, form.getParamBoolean("open", null));

        return html(conSet, form, PATH_JSP + "/linked_list.jsp");
    }

    public ActionForward linkProcessList(DynActionForm form, ConnectionSet conSet) throws Exception {
        Pageable<Pair<String, Process>> pageable = new Pageable<>(form);

        new ProcessLinkDAO(conSet.getConnection(), form).searchLinkProcessList(pageable, form.getId(), form.getParamBoolean("open", null));

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

    public ActionForward linkedProcessCreate(DynActionForm form, Connection con) throws Exception {
        // int id = form.getId();

        //

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
