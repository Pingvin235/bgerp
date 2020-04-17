package ru.bgerp.plugin.workload.struts.action;

import java.sql.Connection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgerp.plugin.workload.dao.GroupLoadDAO;
import ru.bgerp.plugin.workload.model.GroupLoadConfig;

public class GroupLoadAction extends BaseAction {

    /**
     * processIds processTypeId
     * 
     * processTypeIds userGroupIds
     * 
     * @param mapping
     * @param form
     * @param con
     * @return
     * @throws Exception
     */
    public ActionForward show(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {

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

        Set<Integer> processTypeIds = form.getSelectedValues("processTypeIds");
        form.setResponseData("processTypeIds", processTypeIds);

        processTypeIds = allProcessTypeIds(form.getSelectedValues("processTypeIds"));
        if (config.getProcessTypeIds().size() > 0) {
            if (processTypeIds.size() > 0) {
                processTypeIds.retainAll(allProcessTypeIds(config.getProcessTypeIds()));
            } else {
                processTypeIds = allProcessTypeIds(config.getProcessTypeIds());
            }
        }

        Set<Integer> userGroupIds = form.getSelectedValues("userGroupIds");
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

        GroupLoadDAO groupLoadDAO = new GroupLoadDAO(con, form.getUser());
        List<Object[]> processList = groupLoadDAO.getProcessList(config, date, processTypeIds, userGroupIds, sort);

        form.setResponseData("processList", processList);

        form.setResponseData("configProcessTypeIds", config.getProcessTypeIds());
        form.setResponseData("configUserGroupIds", config.getUserGroupIds());

        return processUserTypedForward(con, mapping, form, "show");
    }

    /**
     * Получение ID типов процессов с учетом дочерних.
     * 
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
