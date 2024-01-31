package ru.bgcrm.dao;

import java.util.Set;

import org.bgerp.dao.param.Tables;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

public class ParamValueSelect {
    public static final String PARAM_ADDRESS_FIELD_QUARTER = "quarter";
    public static final String PARAM_ADDRESS_FIELD_STREET = "street";
    public static final Set<String> PARAM_ADDRESS_FIELDS = Set.of(PARAM_ADDRESS_FIELD_QUARTER, PARAM_ADDRESS_FIELD_STREET);

    /**
     * Adds parameter selection query parts.
     * @param variable the parameter macros, e.g. {@code param:12}, optionally ended with {@code :value} or date format suffixes.
     * @param selectPart SELECT part of the query.
     * @param joinPart JOIN part of the query.
     * @param addColumnValueAlias adds the column alias in the query, e.g. {@code AS param_79_value}.
     */
    public static void paramSelectQuery(String variable, String linkColumn, StringBuilder selectPart, StringBuilder joinPart,
            boolean addColumnValueAlias) {
        String[] tokens = variable.split(":");
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

                String columnValueAlias = "";
                if (addColumnValueAlias) {
                    columnValueAlias = " AS param_" + paramId + "_val";
                }

                switch (Parameter.Type.of(type)) {
                    case ADDRESS -> {
                        if (Utils.notBlankString(afterParamId) && !PARAM_ADDRESS_FIELDS.contains(afterParamId)) {
                            selectPart
                                    .append("\n( SELECT GROUP_CONCAT( CONCAT( CAST( house_id AS CHAR ), ':', flat, ':', room, ':', CAST( pod AS CHAR ), ':', CAST( floor AS CHAR ), ':', comment ) SEPARATOR '|') ")
                                    .append("FROM " + Tables.TABLE_PARAM_ADDRESS + " AS param ")
                                    .append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id")
                                    .append(") " + columnValueAlias + " ");
                        } else {
                            if (isMultiple) {
                                selectPart
                                        .append("\n( SELECT GROUP_CONCAT(param.value SEPARATOR '; ') ")
                                        .append("FROM " + Tables.TABLE_PARAM_ADDRESS + " AS param ")
                                        .append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id")
                                        .append(") " + columnValueAlias + " ");
                            } else {
                                if (PARAM_ADDRESS_FIELDS.contains(afterParamId)) {
                                    String joinQuery = " LEFT JOIN param_" + type + " AS " + tableAlias + " ON "
                                            + tableAlias + ".id=" + linkColumn + " AND " + tableAlias + ".param_id="
                                            + paramId;

                                    addIfNotContains(joinPart, joinQuery);

                                    String houseTableAlias = tableAlias + "_house";

                                    joinQuery = " LEFT JOIN " + Tables.TABLE_ADDRESS_HOUSE + " AS " + houseTableAlias + " ON "
                                            + houseTableAlias + ".id=" + tableAlias + ".house_id";

                                    addIfNotContains(joinPart, joinQuery);

                                    if (PARAM_ADDRESS_FIELD_QUARTER.equals(afterParamId)) {
                                        String quarterTableAlias = tableAlias + "_quarter";

                                        joinQuery = " LEFT JOIN " + Tables.TABLE_ADDRESS_QUARTER + " AS " + quarterTableAlias
                                                + " ON " + quarterTableAlias + ".id=" + houseTableAlias + ".quarter_id";
                                        joinPart.append(joinQuery);

                                        selectPart.append(quarterTableAlias + ".title ");
                                    } else if (PARAM_ADDRESS_FIELD_STREET.equals(afterParamId)) {
                                        String streetTableAlias = tableAlias + "_street ";

                                        joinQuery = " LEFT JOIN " + Tables.TABLE_ADDRESS_STREET + " AS " + streetTableAlias
                                                + " ON " + streetTableAlias + ".id=" + houseTableAlias + ".street_id";
                                        joinPart.append(joinQuery);

                                        selectPart.append(streetTableAlias + ".title ");
                                    }
                                } else {
                                    addParamValueJoin(linkColumn, joinPart, paramId, type, tableAlias);
                                    selectPart.append(tableAlias).append(".value ");
                                }
                            }
                        }
                    }
                    case LIST -> {
                        String tableName = param.getConfigMap().get(Parameter.LIST_PARAM_USE_DIRECTORY_KEY);
                        if (Utils.notBlankString(tableName)) {
                            selectPart
                                    .append("\n(SELECT GROUP_CONCAT(CONCAT(val.title, IF(param.comment, CONCAT(' [',param.comment,']'), '')) SEPARATOR ', ') ")
                                    .append("FROM " + Tables.TABLE_PARAM_LIST + " AS param LEFT JOIN " + tableName + " AS val ON param.value=val.id ")
                                    .append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id")
                                    .append(") " + columnValueAlias + " ");
                        } else {
                            selectPart
                                    .append("\n(SELECT GROUP_CONCAT(CONCAT(val.title, IF(param.comment != '', CONCAT(' [',param.comment,']'), '')) SEPARATOR ', ') ")
                                    .append("FROM " + Tables.TABLE_PARAM_LIST + " AS param LEFT JOIN " + Tables.TABLE_PARAM_LIST_VALUE
                                            + " AS val ON param.param_id=val.param_id AND param.value=val.id ")
                                    .append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id")
                                    .append(") " + columnValueAlias + " ");
                        }
                    }
                    case LISTCOUNT -> {
                        selectPart
                                .append("\n( SELECT GROUP_CONCAT( CONCAT(val.title,':',CAST(param.count AS CHAR)) SEPARATOR ', ') ")
                                .append("FROM " + Tables.TABLE_PARAM_LISTCOUNT + " AS param LEFT JOIN " + Tables.TABLE_PARAM_LISTCOUNT_VALUE
                                        + " AS val ON param.param_id=val.param_id AND param.value=val.id ")
                                .append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id")
                                .append(") " + columnValueAlias + " ");
                    }
                    case TREE -> {
                        selectPart
                                .append("\n( SELECT GROUP_CONCAT(val.title SEPARATOR ', ') ")
                                .append("FROM " + Tables.TABLE_PARAM_TREE + " AS param LEFT JOIN " + Tables.TABLE_PARAM_TREE_VALUE
                                        + " AS val ON param.param_id=val.param_id AND param.value=val.id ")
                                .append("WHERE param.id=" + linkColumn + " AND param.param_id=" + paramId + " GROUP BY param.id")
                                .append(") " + columnValueAlias + " ");
                    }
                    default -> {
                        // TODO: Унифицировать код с ProcessDAO.addDateTimeParam
                        if ((Parameter.TYPE_DATE.equals(type) || Parameter.TYPE_DATETIME.equals(type)) && !"value".equals(afterParamId)) {
                            String format = SQLUtils.javaDateFormatToSql(param.getDateParamFormat());

                            selectPart
                                    .append("DATE_FORMAT(").append(tableAlias).append(".value, '").append(format)
                                    .append("') " + columnValueAlias + " ");
                        } else {
                            selectPart.append(tableAlias).append(".value " + columnValueAlias + " ");
                        }

                        addParamValueJoin(linkColumn, joinPart, paramId, type, tableAlias);
                    }
                }
            }
        }
    }

    private static void addParamValueJoin(String linkColumn, StringBuilder joinPart, int paramId, String type, String tableAlias) {
        String joinQuery = " LEFT JOIN param_" + type + " AS " + tableAlias + " ON " + tableAlias + ".id=" + linkColumn + " AND " + tableAlias +  ".param_id=" + paramId;
        addIfNotContains(joinPart, joinQuery);
    }

    private static void addIfNotContains(StringBuilder joinPart, String joinQuery) {
        if (joinPart.indexOf(joinQuery) < 0) {
            joinPart.append(joinQuery);
        }
    }
}
