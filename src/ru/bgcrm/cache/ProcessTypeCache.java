package ru.bgcrm.cache;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.event.SetupChangedEvent;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeTreeItem;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

/**
 * In-memory process types cache.
 *
 * @author Shamil Vakhitov
 */
public class ProcessTypeCache extends Cache<ProcessTypeCache> {
    private static Log log = Log.getLog();

    private static CacheHolder<ProcessTypeCache> holder = new CacheHolder<>(new ProcessTypeCache());

    public static Map<Integer, ProcessType> getProcessTypeMap() {
        return holder.getInstance().typeMap;
    }

    /**
     * Retrieves process type object.
     * @param id ID.
     * @return type instance or {@code null}.
     */
    public static ProcessType getProcessType(int id) {
        return holder.getInstance().typeMap.get(id);
    }

    /**
     * Gets process type by ID.
     * @param id ID.
     * @return
     * @throws NotFoundException
     */
    public static ProcessType getProcessTypeOrThrow(int id) throws NotFoundException {
        var result = getProcessType(id);
        if (result == null)
            throw new NotFoundException("Not found process type with ID: " + id);
        return result;
    }

    /**
     * Retrieves process type object null-safe.
     * @param id ID.
     * @return type instance or mock object with a title, generated out of {@code id}.
     */
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

    public static List<ProcessType> getTypeList(String objectType) throws Exception {
        List<ProcessType> result = new ArrayList<ProcessType>();

        var filter = new TypeFilter(objectType);
        for (ProcessType type : holder.getInstance().typeList) {
            if (filter.check(type))
                result.add(type);
        }

        return result;
    }

    //TODO: Возможно стоит создать Config класс, вынести в model.process.config.LinkedProcessCreateConfig, фильтр сделать на основе expression фильтра.
    private static class TypeFilter {
        private final String objectType;

        private TypeFilter(String objectType) {
            this.objectType = objectType;
        }

        private boolean check(ProcessType type) throws Exception {
            ParameterMap configMap = type.getProperties().getConfigMap();
            Set<String> createInObjectTypes = Utils.toSet(configMap.get("create.in.objectTypes", "*"));
            if (!createInObjectTypes.contains(objectType) && !createInObjectTypes.contains("*")) {
                return false;
            }

            /* Undocumented, remove later 31.01.2022.
            String paramFilter = configMap.get("create.in.filter");
            if (Utils.notBlankString(paramFilter)) {
                if (paramValueDao == null) {
                    paramValueDao = new ParamValueDAO(con);
                    paramValueCache = new HashMap<>();
                }

                if (!paramValueDao.paramValueFilter(paramFilter, objectId, paramValueCache)) {
                    return false;
                }
            }
            */

            return true;
        }
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

    // end of static

    private List<ProcessType> typeList;
    private Map<Integer, ProcessType> typeMap;

    private TypeTreeItem tree;
    private List<Status> statusList;
    private Map<Integer, Status> statusMap;

    @Override
    protected ProcessTypeCache newInstance() {
        ProcessTypeCache result = new ProcessTypeCache();

        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
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
            log.error(e);
        }

        // because process type configurations may include global configs
        EventProcessor.subscribe((conSet, e) -> {
            ProcessTypeCache.flush(null);
        }, SetupChangedEvent.class);

        return result;
    }
}
