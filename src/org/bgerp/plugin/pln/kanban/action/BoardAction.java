package org.bgerp.plugin.pln.kanban.action;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.base.BaseAction;
import org.bgerp.cache.ProcessQueueCache;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.plugin.pln.kanban.Plugin;
import org.bgerp.plugin.pln.kanban.config.Config;
import org.bgerp.plugin.pln.kanban.dao.KanbanBoardDAO;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/kanban/board", pathId = true)
public class BoardAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER + "/board";

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        Connection con = conSet.getConnection();

        List<Queue> queues = ProcessQueueCache.getUserQueueList(form.getUser());
        form.setResponseData("queues", queues);

        int queueId = form.getParamInt("queueId", 0);
        if (queueId <= 0)
            queueId = form.getUser().getPers().getInt("kanbanQueueLastSelected", 0);
        if (queueId <= 0 && !queues.isEmpty())
            queueId = queues.get(0).getId();

        final int resolvedQueueId = queueId;
        Queue queue = ProcessQueueCache.getQueue(queueId, form.getUser());

        Config config = setup.getConfig(Config.class);
        form.setResponseData("previewEnabled", config.isPreviewEnabled());
        form.setRequestAttribute("kanbanConfig", config);

        if (queue != null) {
            form.setResponseData("queue", queue);

            List<ProcessType> types = ProcessTypeCache.getTypeList().stream()
                .filter(type -> queue.getProcessTypeIds().contains(type.getId()))
                .collect(Collectors.toList());
            form.setResponseData("types", types);

            int typeId = form.getParamInt("typeId");
            if (!queue.getProcessTypeIds().contains(typeId))
                typeId = types.size() == 1 ? types.get(0).getId() : 0;
            form.setParam("typeId", String.valueOf(typeId));

            if (typeId > 0) {
                ProcessType type = ProcessTypeCache.getProcessType(typeId);

                List<Status> columns = Utils.getObjectList(ProcessTypeCache.getStatusMap(), type.getProperties().getStatusIds());
                form.setResponseData("columns", columns);

                List<Process> processes = new KanbanBoardDAO(con, form).getProcesses(queue, typeId);

                Map<Integer, List<Process>> cardsByStatus = new LinkedHashMap<>();
                for (Status status : columns)
                    cardsByStatus.put(status.getId(), new ArrayList<>());
                for (Process process : processes) {
                    var list = cardsByStatus.get(process.getStatusId());
                    if (list != null)
                        list.add(process);
                }
                form.setResponseData("cardsByStatus", cardsByStatus);
            }

            String selectedFilters = form.getParam("selectedFilters");
            updatePersonalization(form, con, map -> {
                map.put("kanbanQueueLastSelected", String.valueOf(resolvedQueueId));
                if (selectedFilters != null)
                    map.put("kanbanSelectedFilters" + resolvedQueueId, selectedFilters);
            });
        }

        return html(con, form, PATH_JSP + "/show.jsp");
    }
}
