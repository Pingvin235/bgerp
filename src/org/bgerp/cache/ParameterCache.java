package org.bgerp.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bgerp.app.cfg.Setup;
import org.bgerp.dao.param.ParamDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.base.tree.IdStringTitleTreeItem;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Log;
import org.bgerp.util.sql.TableChangeMonitor;

import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class ParameterCache extends Cache<ParameterCache> {
    private static final Log log = Log.getLog();

    private static final CacheHolder<ParameterCache> HOLDER = new CacheHolder<>(new ParameterCache());

    public static Parameter getParameter(int id) {
        return HOLDER.getInstance().parameterMap.get(id);
    }

    /**
     * Parameters for {@code objectType}, position sorted.
     * @param objectType {@link ru.bgcrm.model.process.Process#OBJECT_TYPE}, {@link ru.bgcrm.model.customer.Customer#OBJECT_TYPE}, {@link ru.bgcrm.model.user.User#OBJECT_TYPE}, {@link ru.bgcrm.model.param.address.AddressHouse#OBJECT_TYPE}
     * @return
     */
    public static List<Parameter> getObjectTypeParameterList(String objectType) {
        return HOLDER.getInstance().objectTypeParameters.getOrDefault(objectType, List.of());
    }


    /**
     * Parameters for {@code objectType}, position sorted.
     * @param objectType {@link ru.bgcrm.model.process.Process#OBJECT_TYPE}, {@link ru.bgcrm.model.customer.Customer#OBJECT_TYPE}, {@link ru.bgcrm.model.user.User#OBJECT_TYPE}, {@link ru.bgcrm.model.param.address.AddressHouse#OBJECT_TYPE}
     * @return
     */
    public static List<Parameter> getObjectTypeParameterList(String objectType, int parameterGroupId) {
        List<Parameter> result = new ArrayList<>();

        List<Parameter> paramList = HOLDER.getInstance().objectTypeParameters.get(objectType);
        if (paramList != null) {
            Set<Integer> paramIds = HOLDER.getInstance().paramGroupParams.get(parameterGroupId);
            if (paramIds != null) {
                for (Parameter p : paramList) {
                    if (paramIds.contains(p.getId())) {
                        result.add(p);
                    }
                }
            } else {
                result.addAll(paramList);
            }
        }

        return result;
    }

    /**
     * Parameter IDs for {@code objectType}, position sorted.
     * @param objectType {@link ru.bgcrm.model.process.Process#OBJECT_TYPE}, {@link ru.bgcrm.model.customer.Customer#OBJECT_TYPE}, {@link ru.bgcrm.model.user.User#OBJECT_TYPE}, {@link ru.bgcrm.model.param.address.AddressHouse#OBJECT_TYPE}
     * @return
     */
    public static List<Integer> getObjectTypeParameterIds(String objectType) {
        List<Integer> result = Collections.emptyList();

        List<Parameter> paramList = HOLDER.getInstance().objectTypeParameters.get(objectType);
        if (paramList != null)
            result = paramList.stream().map(Parameter::getId).collect(Collectors.toList());

        return result;
    }

    /**
     * Parameters with defined IDs.
     * @param pids - IDs.
     * @return
     */
    public static List<Parameter> getParameterList(List<Integer> pids) {
        List<Parameter> result = new ArrayList<>();

        for (Integer paramId : pids) {
            result.add(HOLDER.getInstance().parameterMap.get(paramId));
        }

        return result;
    }

    /**
     * Map of list parameter values. Key - value ID, value - value.
     * @param paramId
     * @return
     */
    public static Map<Integer, IdTitle> getListParamValuesMap(int paramId) {
        return getListParamValues(paramId).stream().collect(Collectors.toMap(IdTitle::getId, Function.identity()));
    }

    /**
     * List of values for parameter with type 'list'.
     * @param param
     * @return
     */
    public static List<IdTitle> getListParamValues(int paramId) {
        var param = getParameter(paramId);
        if (param == null)
            throw new IllegalArgumentException("Parameter not found: " + paramId);
        return getListParamValues(param);
    }

    /**
     * List of values for parameter with type 'list'.
     * @param param
     * @return
     */
    public static List<IdTitle> getListParamValues(final Parameter param) {
        final ParameterCache instance = HOLDER.getInstance();

        List<IdTitle> listValues = null;

        final String tableName = param.getConfigMap().get(Parameter.LIST_PARAM_USE_DIRECTORY_KEY);
        if (tableName != null) {
            listValues = instance.listParamValuesFromDir.get(param.getId());

            if (listValues == null) {
                String innerJoinFilter = param.getConfigMap().get(Parameter.LIST_PARAM_AVAILABLE_VALUES_INNER_JOIN_FILTER_KEY, "");

                final StringBuilder joinFilterTableName = new StringBuilder();
                final StringBuilder joinFilterQuery = new StringBuilder();

                if (Utils.notBlankString(innerJoinFilter)) {
                    String[] tokens = innerJoinFilter.split(";");
                    if (tokens.length != 3) {
                        log.error("Incorrect inner join filter: " + innerJoinFilter);
                    } else {
                        joinFilterTableName.append(tokens[0]);
                        joinFilterQuery.append(
                                " INNER JOIN " + joinFilterTableName + " ON dir.id=" + joinFilterTableName + "." + tokens[1] + " AND " + tokens[2]);
                    }
                }

                Runnable paramExtractor = new Runnable() {
                    @Override
                    public void run() {
                        log.debug("Extracting param values: {}", param.getId());

                        Connection con = Setup.getSetup().getDBConnectionFromPool();
                        try {
                            List<IdTitle> listValues = new ArrayList<>();

                            Set<Integer> availableValues = Utils
                                    .toIntegerSet(param.getConfigMap().get(Parameter.LIST_PARAM_AVAILABLE_VALUES_KEY, ""));

                            StringBuilder query = new StringBuilder("SELECT DISTINCT dir.id, dir.title FROM " + tableName + " AS dir ");
                            if (joinFilterQuery.length() > 0) {
                                query.append(joinFilterQuery);
                            }
                            if (availableValues.size() > 0) {
                                query.append(" WHERE dir.id IN (" + Utils.toString(availableValues) + ")");
                            }
                            query.append(" ORDER BY title");

                            PreparedStatement ps = con.prepareStatement(query.toString());
                            ResultSet rs = ps.executeQuery();
                            while (rs.next()) {
                                listValues.add(new IdTitle(rs.getInt(1), rs.getString(2)));
                            }
                            ps.close();

                            instance.listParamValuesFromDir.put(param.getId(), listValues);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        } finally {
                            SQLUtils.closeConnection(con);
                        }
                    }
                };

                paramExtractor.run();

                listValues = instance.listParamValuesFromDir.get(param.getId());

                // кэллбаки на изменение таблиц со значениями для перечитывания значений списка
                TableChangeMonitor.subscribeOnChange("param-list:" + param.getId(), tableName, paramExtractor);
                if (joinFilterTableName.length() > 0) {
                    TableChangeMonitor.subscribeOnChange("param-list-jf:" + param.getId(), joinFilterTableName.toString(), paramExtractor);
                }
            }
        } else {
            listValues = instance.listParamValues.get(param.getId());
        }

        if (listValues == null) {
            listValues = new ArrayList<>();
        }

        return listValues;
    }

    public static IdStringTitleTreeItem getTreeParamRootNode(Parameter param) {
        return HOLDER.getInstance().treeParamRootNodes.get(param.getId());
    }

    /**
     * Map of tree parameter values. Key - value ID, value - title.
     * @param paramId the parameter ID.
     * @return a map with entries sorted by keys.
     */
    public static Map<String, String> getTreeParamValues(int paramId) {
        // TODO: Cache??
        var param = getParameter(paramId);
        if (param == null)
            throw new IllegalArgumentException("Parameter not found: " + paramId);

        List<IdStringTitleTreeItem> list = new ArrayList<>(100);

        IdStringTitleTreeItem node = getTreeParamRootNode(param);
        if (node != null)
            treeValuesPut(list, node);

        list.sort(IdStringTitleTreeItem.COMPARATOR);

        var result = new LinkedHashMap<String, String>(list.size());
        for (var item : list)
            result.put(item.getId(), item.getTitle());

        return Collections.unmodifiableMap(result);
    }

    private static void treeValuesPut(List<IdStringTitleTreeItem> result, IdStringTitleTreeItem node) {
        result.add(node);
        if (node.getChildren() != null)
            for (var child : node.getChildren())
                treeValuesPut(result, child);
    }

    public static Map<Integer, Parameter> getParameterMap() {
        return HOLDER.getInstance().parameterMap;
    }

    public static void flush(Connection con) {
        HOLDER.flush(con);
    }

    // конец статической части

    private Map<Integer, Parameter> parameterMap;
    private Map<String, List<Parameter>> objectTypeParameters;
    private Map<Integer, Set<Integer>> paramGroupParams;
    private Map<Integer, List<IdTitle>> listParamValues;
    private Map<Integer, List<IdTitle>> listParamValuesFromDir;
    private Map<Integer, IdStringTitleTreeItem> treeParamRootNodes;

    @Override
    protected ParameterCache newInstance() {
        ParameterCache result = new ParameterCache();

        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            ParamDAO paramDAO = new ParamDAO(con);

            result.objectTypeParameters = paramDAO.getParameterMapByObjectType();
            result.paramGroupParams = paramDAO.getParameterIdsByGroupIds();

            result.parameterMap = new HashMap<>();
            for (List<Parameter> paramList : result.objectTypeParameters.values()) {
                for (Parameter p : paramList) {
                    result.parameterMap.put(p.getId(), p);
                }
            }

            result.listParamValues = paramDAO.getListParamValuesMap();
            result.listParamValuesFromDir = new ConcurrentHashMap<>();

            result.treeParamRootNodes = paramDAO.getTreeParamRootNodes();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }

        return result;
    }
}
