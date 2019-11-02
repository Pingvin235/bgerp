package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeTreeItem;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class ProcessTypeCache extends Cache<ProcessTypeCache> {
    private static Logger log = Logger.getLogger(ProcessTypeCache.class);

    private static CacheHolder<ProcessTypeCache> holder = new CacheHolder<ProcessTypeCache>(new ProcessTypeCache());

    public static Map<Integer, ProcessType> getProcessTypeMap() {
        return holder.getInstance().typeMap;
    }

    public static ProcessType getProcessType(int id) {
        return holder.getInstance().typeMap.get(id);
    }

    public static ProcessType getProcessTypeSafe(int id) {
        return Utils.maskNull(getProcessType(id), new ProcessType(id, "??? [" + id + "]"));
    }

    public static List<ProcessType> getTypeList(Set<Integer> ids) {
        List<ProcessType> result = new ArrayList<ProcessType>();

        for (ProcessType type : holder.getInstance().typeList) {
            if (ids.contains(type.getId())) {
                result.add(type);
            }
        }

        return result;
    }

    //TODO: Возможно стоит создать Config класс, вынести в model.process.config.LinkedProcessCreateConfig, фильтр сделать на основе expression фильтра. 
    @SuppressWarnings("rawtypes")
    public static List<ProcessType> getTypeList(Connection con, String objectType, int objectId) throws BGException {
        List<ProcessType> result = new ArrayList<ProcessType>();

        ParamValueDAO paramValueDao = null;
        Map paramValueCache = null;

        for (ProcessType type : holder.getInstance().typeList) {
            ParameterMap configMap = type.getProperties().getConfigMap();

            Set<String> createInObjectTypes = Utils.toSet(configMap.get("create.in.objectTypes", ""));
            if (!createInObjectTypes.contains(objectType)) {
                continue;
            }

            //TODO: Переделать на JEXL
            String paramFilter = configMap.get("create.in.filter");
            if (Utils.notBlankString(paramFilter)) {
                if (paramValueDao == null) {
                    paramValueDao = new ParamValueDAO(con);
                    paramValueCache = new HashMap();
                }

                if (!paramValueDao.paramValueFilter(paramFilter, objectId, paramValueCache)) {
                    continue;
                }
            }

            result.add(type);
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    public static Set<Integer> getTypeSet(Connection con, String objectType, int objectId) throws BGException {
        Set<Integer> result = new HashSet<Integer>();

        ParamValueDAO paramValueDao = null;
        Map paramValueCache = null;

        for (ProcessType type : holder.getInstance().typeList) {
            ParameterMap configMap = type.getProperties().getConfigMap();

            Set<String> createInObjectTypes = Utils.toSet(configMap.get("create.in.objectTypes", ""));
            if (!createInObjectTypes.contains(objectType)) {
                continue;
            }

            String paramFilter = configMap.get("create.in.filter");
            if (Utils.notBlankString(paramFilter)) {
                if (paramValueDao == null) {
                    paramValueDao = new ParamValueDAO(con);
                    paramValueCache = new HashMap();
                }

                if (!paramValueDao.paramValueFilter(paramFilter, objectId, paramValueCache)) {
                    continue;
                }
            }

            result.add(type.getId());
        }

        return result;
    }

    public static TypeTreeItem getTypeTreeRoot() {
        return holder.getInstance().tree;
    }

    public static List<Status> getTypeStatusList(ProcessType type, int currentStatusId) {
        List<Status> result = new ArrayList<Status>();

        Set<Integer> allowedStatusSet = type.getProperties().getAllowedStatusSet(currentStatusId);
        allowedStatusSet.add(currentStatusId);

        List<Status> statusList = Utils.getObjectList(ProcessTypeCache.getStatusMap(), type.getProperties().getStatusIds());
        for (Status status : statusList) {
            if (allowedStatusSet.contains(status.getId())) {
                result.add(status);
            }
        }

        return result;
    }

    public static List<Status> getStatusList() {
        return holder.getInstance().statusList;
    }

    public static Map<Integer, Status> getStatusMap() {
        return holder.getInstance().statusMap;
    }

    public static Status getStatusSafe(int statusId) {
        return Utils.maskNull(getStatusMap().get(statusId), new Status(statusId, "??? [" + statusId + "]"));
    }

    public static List<ProcessType> getTypePath(int id) {
        List<ProcessType> result = new ArrayList<ProcessType>();

        ProcessType type = new ProcessType();
        type.setParentId(id);

        while (type.getParentId() != 0) {
            final int parentId = type.getParentId();

            type = holder.getInstance().typeMap.get(parentId);
            if (type == null) {
                type = new ProcessType();
                type.setTitle("??? [" + parentId + "]");
            }
            result.add(0, type);
        }

        return result;
    }

    public static void flush(Connection con) {
        holder.flush(con);
    }

    // конец статической части

    private List<ProcessType> typeList;
    private Map<Integer, ProcessType> typeMap;

    private TypeTreeItem tree;
    private List<Status> statusList;
    private Map<Integer, Status> statusMap;

    @Override
    protected ProcessTypeCache newInstance() {
        ProcessTypeCache result = new ProcessTypeCache();

        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            ConfigDAO configDao = new ConfigDAO(con);
            ProcessTypeDAO typeDAO = new ProcessTypeDAO(con);

            result.typeList = typeDAO.getFullProcessTypeList();
            result.typeMap = new HashMap<Integer, ProcessType>();

            for (ProcessType type : result.typeList) {
                if (!type.isUseParentProperties())
                    type.getProperties().setConfigMap(Preferences.processIncludes(configDao, type.getProperties().getConfig(), false));
                result.typeMap.put(type.getId(), type);
            }

            result.tree = typeDAO.getTypeTreeRoot();

            result.statusList = new StatusDAO(con).getStatusList();
            result.statusMap = new HashMap<Integer, Status>();
            for (Status status : result.statusList)
                result.statusMap.put(status.getId(), status);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }

        return result;
    }
}
