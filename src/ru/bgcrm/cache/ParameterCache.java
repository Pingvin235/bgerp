package ru.bgcrm.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bgerp.model.base.IdTitleTree;
import org.bgerp.util.Log;

import ru.bgcrm.dao.ParamDAO;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;
import ru.bgcrm.util.sql.TableChangeMonitor;

public class ParameterCache extends Cache<ParameterCache> {
    private static final Log log = Log.getLog();

    private static CacheHolder<ParameterCache> holder = new CacheHolder<>(new ParameterCache());

    public static Parameter getParameter(int id) {
        return holder.getInstance().parameterMap.get(id);
    }

    /**
     * Parameters for {@code objectType}, position sorted.
     * @param objectType {@link ru.bgcrm.model.process.Process#OBJECT_TYPE}, {@link ru.bgcrm.model.customer.Customer#OBJECT_TYPE}, {@link ru.bgcrm.model.user.User#OBJECT_TYPE}, {@link ru.bgcrm.model.param.address.AddressHouse#OBJECT_TYPE}
     * @return
     */
    public static List<Parameter> getObjectTypeParameterList(String objectType) {
        List<Parameter> result = new ArrayList<Parameter>();

        List<Parameter> paramList = holder.getInstance().objectTypeParameters.get(objectType);
        if (paramList != null) {
            result.addAll(paramList);
        }

        return result;
    }


    /**
     * Parameters for {@code objectType}, position sorted.
     * @param objectType {@link ru.bgcrm.model.process.Process#OBJECT_TYPE}, {@link ru.bgcrm.model.customer.Customer#OBJECT_TYPE}, {@link ru.bgcrm.model.user.User#OBJECT_TYPE}, {@link ru.bgcrm.model.param.address.AddressHouse#OBJECT_TYPE}
     * @return
     */
    public static List<Parameter> getObjectTypeParameterList(String objectType, int parameterGroupId) {
        List<Parameter> result = new ArrayList<Parameter>();

        List<Parameter> paramList = holder.getInstance().objectTypeParameters.get(objectType);
        if (paramList != null) {
            Set<Integer> paramIds = holder.getInstance().paramGroupParams.get(parameterGroupId);
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

        List<Parameter> paramList = holder.getInstance().objectTypeParameters.get(objectType);
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
        List<Parameter> result = new ArrayList<Parameter>();

        for (Integer paramId : pids) {
            result.add(holder.getInstance().parameterMap.get(paramId));
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
        final ParameterCache instance = holder.getInstance();

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
                            List<IdTitle> listValues = new ArrayList<IdTitle>();

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
            listValues = new ArrayList<IdTitle>();
        }

        return listValues;
    }

    public static IdTitleTree getTreeParamValues(final Parameter param) {
        return holder.getInstance().treeParamValues.get(param.getId());
    }

    public static Map<Integer, Parameter> getParameterMap() {
        return holder.getInstance().parameterMap;
    }

    public static void flush(Connection con) {
        holder.flush(con);
    }

    // конец статической части

    private Map<Integer, Parameter> parameterMap;
    private Map<String, List<Parameter>> objectTypeParameters;
    private Map<Integer, Set<Integer>> paramGroupParams;
    private Map<Integer, List<IdTitle>> listParamValues;
    private Map<Integer, IdTitleTree> treeParamValues;
    private Map<Integer, List<IdTitle>> listParamValuesFromDir;

    @Override
    protected ParameterCache newInstance() {
        ParameterCache result = new ParameterCache();

        Connection con = Setup.getSetup().getDBConnectionFromPool();
        try {
            ParamDAO paramDAO = new ParamDAO(con);

            result.objectTypeParameters = paramDAO.getParameterMapByObjectType();
            result.paramGroupParams = paramDAO.getParameterIdsByGroupIds();

            result.parameterMap = new HashMap<Integer, Parameter>();
            for (List<Parameter> paramList : result.objectTypeParameters.values()) {
                for (Parameter p : paramList) {
                    result.parameterMap.put(p.getId(), p);
                }
            }

            result.listParamValues = paramDAO.getListParamValuesMap();
            result.listParamValuesFromDir = new ConcurrentHashMap<Integer, List<IdTitle>>();

            result.treeParamValues = paramDAO.getTreeParamValuesMap();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SQLUtils.closeConnection(con);
        }

        return result;
    }
}
