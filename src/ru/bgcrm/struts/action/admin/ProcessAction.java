package ru.bgcrm.struts.action.admin;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.bgcrm.cache.ProcessQueueCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.dao.ParamDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.QueueDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.model.ArrayHashMap;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;

public class ProcessAction extends BaseAction {
    // статусы
    public ActionForward statusList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new StatusDAO(con).searchStatus(new SearchResult<Status>(form));

        return data(con, mapping, form, "statusList");
    }

    public ActionForward statusUseProcess(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Integer statusId = Utils.parseInt(form.getParam("statusId"));
        List<String> containProcess = new ArrayList<String>();
        Map<Integer, ProcessType> processTypeMap = ProcessTypeCache.getProcessTypeMap();

        for (int i = 0; i < processTypeMap.size(); i++) {
            ProcessType pType = (ProcessType) processTypeMap.values().toArray()[i];

            if (!pType.isUseParentProperties()) {
                List<Integer> parameters = pType.getProperties().getStatusIds();
                if (parameters.contains(statusId)) {
                    containProcess.add(pType.getTitle());
                }
            }
        }

        form.getResponse().setData("containProcess", containProcess);

        return data(con, mapping, form, "statusUseProcess");
    }

    public ActionForward statusDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        new StatusDAO(con).deleteStatus(form.getId());

        ProcessTypeCache.flush(con);

        return status(con, form);
    }

    public ActionForward statusGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        StatusDAO statusDAO = new StatusDAO(con);

        Status status = statusDAO.getStatus(form.getId());
        if (status != null) {
            form.getResponse().setData("status", status);
        }

        return data(con, mapping, form, "statusUpdate");
    }

    public ActionForward statusUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        StatusDAO statusDAO = new StatusDAO(con);

        Status status = new Status();
        status.setId(form.getId());
        status.setTitle(form.getParam("title"));
        status.setPos(Utils.parseInt(form.getParam("pos")));

        statusDAO.updateStatus(status);

        ProcessTypeCache.flush(con);

        return status(con, form);
    }

    // типы
    public ActionForward typeList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        HttpServletRequest request = form.getHttpRequest();
        ArrayHashMap paramMap = form.getParam();
        int parentId = Utils.parseInt(paramMap.get("parentTypeId"), 0);

        ProcessTypeDAO processTypeDAO = new ProcessTypeDAO(con);
        processTypeDAO.searchProcessType(new SearchResult<ProcessType>(form), parentId, null,
                CommonDAO.getLikePatternSub(form.getParam("filter", "")));

        if (parentId >= 0) {
            request.setAttribute("typePath", ProcessTypeCache.getTypePath(parentId));
        }

        // смотрим что помечено
        int id = Utils.parseInt(paramMap.get("markType"), -1);
        if (id != -1) {
            ProcessType type = processTypeDAO.getProcessType(id);
            if (type != null) {
                request.setAttribute("markTypeString", type.getTitle());
            }
        }

        return data(con, mapping, form, "typeList");
    }

    public ActionForward typeGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessTypeDAO processTypeDAO = new ProcessTypeDAO(con);

        ProcessType type = processTypeDAO.getProcessType(form.getId());
        if (type != null) {
            form.getResponse().setData("type", type);
        }

        return data(con, mapping, form, "typeUpdate");
    }

    public ActionForward typeUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ProcessTypeDAO processTypeDAO = new ProcessTypeDAO(con);

        ProcessType type = new ProcessType();
        type.setId(form.getId());
        type.setTitle(form.getParam("title"));
        type.setParentId(Utils.parseInt(form.getParam("parentTypeId")));
        type.setUseParentProperties(Utils.parseBoolean(form.getParam("useParent")));

        if (Utils.isBlankString(type.getTitle())) {
            throw new BGMessageException("Пустое имя.");
        }
        if (!processTypeDAO.checkType(type.getId(), type.getParentId(), type.getTitle())) {
            throw new BGMessageException("Такое имя уже существует в данной ветке.");
        }

        processTypeDAO.updateProcessType(type, form.getUserId());
        ProcessTypeCache.flush(con);

        return status(con, form);
    }

    public ActionForward typeDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ArrayHashMap paramMap = form.getParam();
        int id = Utils.parseInt(paramMap.get("id"), -1);
        ProcessTypeDAO typeDAO = new ProcessTypeDAO(con);

        if (!typeDAO.checkProcessTypeForDelete(id)) {
            throw new BGMessageException("Невозможно удалить тип, пока он имеет потомков.");
        }

        typeDAO.deleteProcessType(id);
        ProcessTypeCache.flush(con);

        return status(con, form);
    }

    public ActionForward typeInsertMark(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        ArrayHashMap paramMap = form.getParam();
        int parentId = Utils.parseInt(paramMap.get("parentTypeId"), 0);
        int id = Utils.parseInt(paramMap.get("markType"), -1);

        if (id != -1) {
            ProcessTypeDAO typeDAO = new ProcessTypeDAO(con);
            ProcessType type = typeDAO.getProcessType(id);

            if (!typeDAO.checkType(0, parentId, type.getTitle())) {
                throw new BGMessageException("Такое имя уже существует в данной ветке.");
            }

            if (parentId == id) {
                throw new BGMessageException("Нельзя копировать в самого себя.");
            }

            type.setParentId(parentId);
            typeDAO.updateProcessType(type, form.getUserId());
            ProcessTypeCache.flush(con);
            paramMap.put("markType", "0");
        }
        //form.setAction( "typeList" );
        return status(con, form);
    }

    public ActionForward typeUsed(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int typeId = form.getParamInt("typeId", 0);
        if (typeId <= 0)
            throw new BGIllegalArgumentException();

        List<String> queueList = ProcessQueueCache.getQueueList().stream()
            .filter(q -> q.getProcessTypeIds().contains(typeId)).map(Queue::getTitle)
            .collect(Collectors.toList());
        form.getResponse().setData("queueTitleList", queueList);

        return data(con, mapping, form);
    }

    public ActionForward typeCopy(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        int typeId = form.getId();
        if (typeId <= 0)
            throw new BGIllegalArgumentException();
        
        var dao = new ProcessTypeDAO(con);

        int fromTypeId = form.getParamInt("fromId");
        if (fromTypeId > 0) {
            dao.copyTypeProperties(fromTypeId, typeId);
            return status(con, form);
        }

        var types = dao.getTypeChildren(form.getParamInt("parentId", 0), Collections.singleton(typeId));
        form.setResponseData("types", types);

        return data(con, mapping, form);
    }

    // очереди
    public ActionForward queueList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Set<Integer> queueIds = Utils.toIntegerSet(form.getPermission().get("allowedQueueIds"));

        new QueueDAO(con).searchQueue(new SearchResult<Queue>(form), queueIds, form.getParam("filter"));

        return data(con, mapping, form, "queueList");
    }

    public ActionForward queueGet(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        HttpServletRequest request = form.getHttpRequest();

        checkAllowedQueueIds(form);

        QueueDAO queueDAO = new QueueDAO(con);

        int id = form.getId();

        Queue queue = queueDAO.getQueue(id);
        if (queue != null) {
            queue.setProcessTypeIds(queueDAO.getQueueProcessTypeIds(id));
            form.getResponse().setData("queue", queue);
        }

        request.setAttribute("typeTreeRoot", ProcessTypeCache.getTypeTreeRoot());

        return data(con, mapping, form, "queueUpdate");
    }

    public ActionForward queueUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        checkAllowedQueueIds(form);

        QueueDAO queueDAO = new QueueDAO(con);

        Queue queue = new Queue();
        queue.setId(form.getId());
        queue.setTitle(form.getParam("title"));
        queue.setConfig(form.getParam("config"));
        queue.setProcessTypeIds(form.getSelectedValues("type"));
        queue.setLastModify(new LastModify(form.getUserId(), new Date()));

        Queue oldQueue = queueDAO.getQueue(form.getId());
        checkModified(oldQueue == null ? new LastModify() : oldQueue.getLastModify(), form);

        queueDAO.updateQueue(queue, form.getUserId());

        form.getResponse().setData("queue", queue);

        ProcessQueueCache.flush(con);

        return status(con, form);
    }

    public ActionForward queueDelete(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        checkAllowedQueueIds(form);

        new QueueDAO(con).delete(form.getId());

        ProcessQueueCache.flush(con);

        return status(con, form);
    }

    public ActionForward properties(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        HttpServletRequest request = form.getHttpRequest();

        checkAllowedQueueIds(form);

        ProcessTypeDAO typeDAO = new ProcessTypeDAO(con);
        ParamDAO paramDAO = new ParamDAO(con, form.getUserId());
        UserPermsetDAO groupDAO = new UserPermsetDAO(con);

        ProcessType type = typeDAO.getProcessType(form.getId());
        if (type != null) {
            form.getResponse().setData("properties", type.getProperties());
            form.getResponse().setData("config", type.getProperties().getConfig());

            request.setAttribute("statusList", Utils.getObjectList(ProcessTypeCache.getStatusMap(), type.getProperties().getStatusIds()));
            request.setAttribute("processType", type);

            SearchResult<Permset> groupList = new SearchResult<Permset>();
            groupDAO.searchPermset(groupList);
            request.setAttribute("groupList", groupList);

            request.setAttribute("parameterList", paramDAO.getParameterList(Process.OBJECT_TYPE, 0));
        }

        return data(con, mapping, form, "typeProperties");
    }

    public ActionForward propertiesUpdate(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        checkAllowedQueueIds(form);

        ProcessTypeDAO typeDAO = new ProcessTypeDAO(con);
        ConfigDAO configDao = new ConfigDAO(con);

        ProcessType type = typeDAO.getProcessType(form.getId());
        if (type != null) {
            TypeProperties properties = type.getProperties();

            String[] beginGroupArr = form.getParamArray("beginGroupRole");
            String[] allowedGroupArr = form.getParamArray("allowedGroupRole");

            properties.setCreateStatus(Utils.parseInt(form.getParam("create_status")));
            properties.setCloseStatusIds(Utils.toIntegerSet(form.getParam("close_status", "")));
            properties.setScriptName(Utils.maskNull(form.getParam("script_name", "")));
            properties.setStatusIds(form.getSelectedValuesList("status"));
            properties.setParameterIds(form.getSelectedValuesList("param"));
            properties.setAllowedGroups(ProcessGroup.parseStringArray(allowedGroupArr));
            properties.setConfig(form.getParam("config"));
            properties.setGroups(ProcessGroup.parseStringArray(beginGroupArr));
            
            // при заполнении всплывающей формы авторизации после редиректа приходит форма без POST параметров
            if (properties.getConfig() == null) {
                throw new BGException("Попытка сохранения пустой конфигурации.");
            }
            
            checkModified(properties.getLastModify(), form);
            
            Preferences.processIncludes(configDao, properties.getConfig(), true);

            checkAllowedQueueIds(form);

            updateTransactions(form, type);

            typeDAO.updateTypeProperties(type);
            ProcessTypeCache.flush(con);
        }

        return status(con, form);
    }

    private void updateTransactions(DynActionForm form, ProcessType type) throws Exception {
        String[] matrixParamArray = form.getParamArray("matrix");
        if (matrixParamArray != null) {
            var properties = type.getProperties();
            var anyEnabled = false;
            
            for (String transaction : matrixParamArray) {
                String[] paramArray = transaction.split("-");

                int fromStatus = Utils.parseInt(paramArray[0]);
                int toStatus = Utils.parseInt(paramArray[1]);
                boolean enabled = Utils.parseBoolean(paramArray[2]);

                anyEnabled |= enabled;
                
                properties.setTransactionProperties(fromStatus, toStatus, enabled);
            }

            if (!anyEnabled)
                properties.clearTransactionProperties();
        }
    }

    private void checkAllowedQueueIds(DynActionForm form) throws BGMessageException {
        Set<Integer> queueIds = Utils.toIntegerSet(form.getPermission().get("allowedQueueIds"));
        if (CollectionUtils.isNotEmpty(queueIds) && !queueIds.contains(form.getId())) {
            throw new BGMessageException("Работа с данной очередью запрещена.");
        }
    }
}
