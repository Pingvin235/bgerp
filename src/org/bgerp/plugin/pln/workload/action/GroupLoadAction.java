package org.bgerp.plugin.pln.workload.action;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.plugin.pln.workload.Plugin;
import org.bgerp.plugin.pln.workload.dao.GroupLoadDAO;
import org.bgerp.plugin.pln.workload.model.GroupLoadConfig;

import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;

@Action(path = "/user/plugin/workload/groupload")
public class GroupLoadAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward show(DynActionForm form, Connection con) throws Exception {
        final int processTypeId = form.getParamInt("processTypeId");
        final ProcessType processType = ProcessTypeCache.getProcessType(processTypeId);

        if (processType == null) {
            log.error("ProcessType not found with ID=" + processTypeId);
            throw new BGMessageException("Не найден тип процесса с ID=" + processTypeId);
        }

        final GroupLoadConfig config = processType.getProperties()
                .getConfigMap()
                .getConfig(GroupLoadConfig.class);

        Date date = form.getParamDate("date");
        if (date == null) {
            date = new Date();
        }

        form.setResponseData("date", TimeUtils.format(date, TimeUtils.FORMAT_TYPE_YMD));

        Set<Integer> processTypeIds = form.getParamValues("processTypeIds");
        form.setResponseData("processTypeIds", processTypeIds);

        processTypeIds = allProcessTypeIds(form.getParamValues("processTypeIds"));
        if (config.getProcessTypeIds().size() > 0) {
            if (processTypeIds.size() > 0) {
                processTypeIds.retainAll(allProcessTypeIds(config.getProcessTypeIds()));
            } else {
                processTypeIds = allProcessTypeIds(config.getProcessTypeIds());
            }
        }

        Set<Integer> userGroupIds = form.getParamValues("userGroupIds");
        form.setResponseData("userGroupIds", userGroupIds);

        if (config.getUserGroupIds().size() > 0) {
            if (userGroupIds.size() > 0) {
                userGroupIds.retainAll(config.getUserGroupIds());
            } else {
                userGroupIds = config.getUserGroupIds();
            }
        }

        String sort = form.getParam("sort");
        form.setResponseData("sort", sort);

        GroupLoadDAO groupLoadDAO = new GroupLoadDAO(con, form);
        List<Object[]> processList = groupLoadDAO.getProcessList(config, date, processTypeIds, userGroupIds, sort);

        form.setResponseData("processList", processList);

        form.setResponseData("configProcessTypeIds", config.getProcessTypeIds());
        form.setResponseData("configUserGroupIds", config.getUserGroupIds());

        return html(con, form, PATH_JSP + "/groupload/show.jsp");
    }

    /**
     * Получение ID типов процессов с учетом дочерних.
     * @param processTypeIds
     * @return
     */
    private Set<Integer> allProcessTypeIds(Set<Integer> processTypeIds) {
        Set<Integer> result = new HashSet<Integer>();

        for (Integer processTypeId : processTypeIds) {
            ProcessType processType = ProcessTypeCache.getProcessType(processTypeId);
            if (processType == null) {
                continue;
            }

            result.add(processTypeId);
            result.addAll(processType.getAllChildIds());
        }

        return result;
    }

}
