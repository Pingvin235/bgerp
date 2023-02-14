package ru.bgcrm.dao;

import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_QUARTER;
import static ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT_VALUE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST_VALUE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TREE;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TREE_VALUE;

import java.util.Set;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class ParamValueSelect {
    public static final String PARAM_ADDRESS_FIELD_QUARTER = "quarter";
    public static final String PARAM_ADDRESS_FIELD_STREET = "street";
    public static final Set<String> PARAM_ADDRESS_FIELDS = Set.of(PARAM_ADDRESS_FIELD_QUARTER, PARAM_ADDRESS_FIELD_STREET);

    /**
     * Добавляет в запрос выборку параметра.
     *
     * @param paramRef
     * @param selectPart
     * @param joinPart
     * @param addColumnValueAlias - добавляет в запрос алиас колонки, например в
     *                            запросе param_79.value AS param_79_value добавит
     *                            строку " AS param_79_value"
     */
    public static void paramSelectQuery(String paramRef, String linkColumn, StringBuilder selectPart,
            StringBuilder joinPart, boolean addColumnValueAlias) {
        String[] tokens = paramRef.split(":");
        if (tokens.length >= 2) {
            int paramId = Utils.parseInt(tokens[1].trim());
            String afterParamId = "";
            if (tokens.length > 2) {
                afterParamId = tokens[2].trim();
            }

            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null) {
                selectPart.append("'PARAM NOT FOUND:" + paramId + "' ");
            } else {
                boolean isMultiple = param.getConfigMap().getBoolean(Parameter.PARAM_MULTIPLE_KEY, false);

                String type = param.getType();

                String tableAlias = "param_" + paramId;
                if (Utils.notBlankString(afterParamId)) {
                    tableAlias += "_" + afterParamId;
                }

                String columnValueAlias = "";
                if (addColumnValueAlias) {
                    columnValueAlias = " AS param_" + paramId + "_val";
                }

                if (Parameter.TYPE_ADDRESS.equals(type)) {
                    if (Utils.notBlankString(afterParamId) && !PARAM_ADDRESS_FIELDS.contains(afterParamId)) {
                        selectPart.append(
                                "\n( SELECT GROUP_CONCAT( CONCAT( CAST( house_id AS CHAR ), ':', flat, ':', room, ':', CAST( pod AS CHAR ), ':', CAST( floor AS CHAR ), ':', comment ) SEPARATOR '|') ");
                        selectPart.append("FROM " + TABLE_PARAM_ADDRESS + " AS param ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    } else {
                        if (isMultiple) {
                            selectPart.append("\n( SELECT GROUP_CONCAT(param.value SEPARATOR '; ') ");
                            selectPart.append("FROM " + TABLE_PARAM_ADDRESS + " AS param ");
                            selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                    + " GROUP BY param.id");
                            selectPart.append(") " + columnValueAlias + " ");
                        } else {
                            if (PARAM_ADDRESS_FIELDS.contains(afterParamId)) {
                                String joinQuery = " LEFT JOIN param_" + type + " AS " + tableAlias + " ON "
                                        + tableAlias + ".id=" + linkColumn + " AND " + tableAlias + ".param_id="
                                        + paramId;

                                addIfNotContains(joinPart, joinQuery);

                                String houseTableAlias = tableAlias + "_house";

                                joinQuery = " LEFT JOIN " + TABLE_ADDRESS_HOUSE + " AS " + houseTableAlias + " ON "
                                        + houseTableAlias + ".id=" + tableAlias + ".house_id";

                                addIfNotContains(joinPart, joinQuery);

                                if (PARAM_ADDRESS_FIELD_QUARTER.equals(afterParamId)) {
                                    String quarterTableAlias = tableAlias + "_quarter";

                                    joinQuery = " LEFT JOIN " + TABLE_ADDRESS_QUARTER + " AS " + quarterTableAlias
                                            + " ON " + quarterTableAlias + ".id=" + houseTableAlias + ".quarter_id";
                                    joinPart.append(joinQuery);

                                    selectPart.append(quarterTableAlias + ".title ");
                                } else if (PARAM_ADDRESS_FIELD_STREET.equals(afterParamId)) {
                                    String streetTableAlias = tableAlias + "_street ";

                                    joinQuery = " LEFT JOIN " + TABLE_ADDRESS_STREET + " AS " + streetTableAlias
                                            + " ON " + streetTableAlias + ".id=" + houseTableAlias + ".street_id";
                                    joinPart.append(joinQuery);

                                    selectPart.append(streetTableAlias + ".title ");
                                }
                            } else {
                                addParamValueJoin(linkColumn, joinPart, paramId, type, tableAlias);
                                selectPart.append(tableAlias);
                                selectPart.append(".value ");
                            }
                        }
                    }
                }
                // TODO: Для списков с одним значением достаточно делать JOIN, будет быстрее..
                else if (Parameter.TYPE_LIST.equals(type)) {
                    String tableName = param.getConfigMap().get(Parameter.LIST_PARAM_USE_DIRECTORY_KEY);
                    if (Utils.notBlankString(tableName)) {
                        selectPart.append(
                                "\n(SELECT GROUP_CONCAT(CONCAT(val.title, IF(param.comment, CONCAT(' [',param.comment,']'), '')) SEPARATOR ', ') ");
                        selectPart.append("FROM " + TABLE_PARAM_LIST + " AS param LEFT JOIN " + tableName
                                + " AS val ON param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    } else {
                        selectPart.append(
                                "\n(SELECT GROUP_CONCAT(CONCAT(val.title, IF(param.comment != '', CONCAT(' [',param.comment,']'), '')) SEPARATOR ', ') ");
                        selectPart.append("FROM " + TABLE_PARAM_LIST + " AS param LEFT JOIN " + TABLE_PARAM_LIST_VALUE
                                + " AS val ON param.param_id=val.param_id AND param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    }
                } else if (Parameter.TYPE_LISTCOUNT.equals(type)) {
                    String tableName = param.getConfigMap().get(Parameter.LIST_PARAM_USE_DIRECTORY_KEY);
                    if (Utils.notBlankString(tableName)) {
                        selectPart.append("\n( SELECT GROUP_CONCAT(val.title,val.count SEPARATOR ', ') ");
                        selectPart.append("FROM " + TABLE_PARAM_LISTCOUNT + " AS param LEFT JOIN " + tableName
                                + " AS val ON param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    } else {
                        selectPart.append(
                                "\n( SELECT GROUP_CONCAT( CONCAT(val.title,':',CAST(param.count AS CHAR)) SEPARATOR ', ') ");
                        selectPart.append(
                                "FROM " + TABLE_PARAM_LISTCOUNT + " AS param LEFT JOIN " + TABLE_PARAM_LISTCOUNT_VALUE
                                        + " AS val ON param.param_id=val.param_id AND param.value=val.id ");
                        selectPart.append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId
                                + " GROUP BY param.id");
                        selectPart.append(") " + columnValueAlias + " ");
                    }
                } else if (Parameter.TYPE_TREE.equals(type)) {
                    selectPart.append("\n( SELECT GROUP_CONCAT(val.title SEPARATOR ', ') ");
                    selectPart.append("FROM " + TABLE_PARAM_TREE + " AS param LEFT JOIN " + TABLE_PARAM_TREE_VALUE
                            + " AS val ON param.param_id=val.param_id AND param.value=val.id ");
                    selectPart.append(
                            "WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id");
                    selectPart.append(") " + columnValueAlias + " ");
                } else {
                    // TODO: Унифицировать код с ProcessDAO.addDateTimeParam
                    if ((Parameter.TYPE_DATE.equals(type) || Parameter.TYPE_DATETIME.equals(type))
                            && !"value".equals(afterParamId)) {
                        String format = SQLUtils.javaDateFormatToSql(param.getDateParamFormat());

                        selectPart.append("DATE_FORMAT(");
                        selectPart.append(tableAlias);
                        selectPart.append(".value, '");
                        selectPart.append(format);
                        selectPart.append("') " + columnValueAlias + " ");
                    } else {
                        selectPart.append(tableAlias);
                        selectPart.append(".value " + columnValueAlias + " ");
                    }

                    addParamValueJoin(linkColumn, joinPart, paramId, type, tableAlias);
                }
            }
        }
    }

    private static void addParamValueJoin(String linkColumn, StringBuilder joinPart, int paramId, String type,
            String tableAlias) {
        joinPart.append(" LEFT JOIN param_" + type);
        joinPart.append(" AS ");
        joinPart.append(tableAlias);
        joinPart.append(" ON ");
        joinPart.append(tableAlias);
        joinPart.append(".id=");
        joinPart.append(linkColumn);
        joinPart.append(" AND ");
        joinPart.append(tableAlias);
        joinPart.append(".param_id=");
        joinPart.append(paramId);
    }

    private static void addIfNotContains(StringBuilder joinPart, String joinQuery) {
        if (joinPart.indexOf(joinQuery) < 0) {
            joinPart.append(joinQuery);
        }
    }
}
