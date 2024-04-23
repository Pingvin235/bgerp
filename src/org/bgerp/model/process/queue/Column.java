package org.bgerp.model.process.queue;

import static org.bgerp.dao.process.ProcessQueueDAO.LINKED_PROCESS_JOIN;
import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_PROCESS_MESSAGE_STATE;
import static ru.bgcrm.dao.process.ProcessDAO.LINKED_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_TYPE;
import static ru.bgcrm.dao.user.Tables.TABLE_USER;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.event.EventProcessor;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ParameterCache;
import org.bgerp.cache.UserCache;
import org.bgerp.dao.param.Tables;
import org.bgerp.event.process.queue.QueueColumnEvent;
import org.bgerp.model.param.Parameter;
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import ru.bgcrm.dao.ParamValueSelect;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SQLUtils;

@Dynamic
public class Column {
    private static final Log log = Log.getLog();

    public static Column of(String id, ConfigMap config) throws Exception {
        Column result = new Column(id, config);

        var event = new QueueColumnEvent(result);
        EventProcessor.processEvent(event, null);
        if (event.getColumn() != null)
            result = event.getColumn();

        return result;
    }

    private final String id;
    private final ConfigMap config;

    protected Column(String id, ConfigMap config) {
        this.id = id;
        this.config = config;
    }

    protected Column(Column column) {
        this.id = column.id;
        this.config = column.config;
    }

    public String getId() {
        return id;
    }

    public String getValue() {
        return config.get("value");
    }

    public String getAggregate() {
        return config.get("aggregate");
    }

    private String getConvert() {
        return config.get("convert");
    }

    public String getTitle() {
        return config.get("title");
    }

    public int getTitleIfMore() {
        return config.getInt("titleIfMore");
    }

    public boolean isFormatToHtml() {
        return config.getBoolean("formatToHtml");
    }

    public int getCutIfMore() {
        return config.getInt("cutIfMore");
    }

    public String getShowAsLink() {
        return config.get("showAsLink");
    }

    public String getStyle() {
        var result = new StringBuilder(config.get("style", ""));

        if (config.getBoolean("nowrap"))
            Utils.addSeparated(result, "; ", "white-space: nowrap");

        final String align = config.get("align");
        if (Utils.notBlankString(align))
            Utils.addSeparated(result, "; ",  "text-align: " + align);

        return result.toString();
    }

    /**
     * @return {@code process} by default, or {@code linked} for parent linked process.
     */
    public String getProcess() {
        return config.get("process", "process");
    }

    public void addQuery(StringBuilder selectPart, StringBuilder joinPart) throws Exception {
        String value = getValue();
        if (value == null) {
            throw new BGException(".value not defined, column: " + getId());
        }

        String target = getProcess();

        if (LINKED_PROCESS.equals(target) && joinPart.indexOf(LINKED_PROCESS_JOIN) < 0) {
            joinPart.append(LINKED_PROCESS_JOIN);
        }

        Pair<String, String> modifiers = getModifiers();
        selectPart.append(modifiers.getFirst());

        if ("check".equals(value) || "id".equals(value)) {
            selectPart.append(target + ".id ");
        } else if ("type_title".equals(value)) {
            String alias = "type_" + target;

            selectPart.append(alias + ".title ");
            joinPart.append(" LEFT JOIN " + TABLE_PROCESS_TYPE + " AS " + alias + " ON " + target + ".type_id=" + alias
                    + ".id");
        } else if (value.startsWith("status_")) {
            String alias = addProcessStatusJoin(target, joinPart);

            if ("status_title".equals(value) || "status_pos".equals(value)) {
                String aliasPst = "process_status_title_" + target;

                if ("status_title".equals(value))
                    selectPart.append(aliasPst + ".title ");
                else
                    selectPart.append(aliasPst + ".pos ");

                if (joinPart.indexOf(aliasPst) < 0)
                    joinPart.append(" LEFT JOIN process_status_title AS " + aliasPst + " ON " + target + ".status_id=" + aliasPst + ".id");
            } else if (value.startsWith("status_dt")) {
                addDateTimeParam(selectPart, alias + ".dt", value);
            } else if ("status_user".equals(value)) {
                String aliasPsu = "status_user_" + target;

                selectPart.append(aliasPsu + ".title ");
                joinPart.append(" LEFT JOIN user AS " + aliasPsu + " ON " + aliasPsu + ".id=" + alias + ".user_id ");
            } else if ("status_comment".equals(value)) {
                selectPart.append(alias + ".comment");
            } else {
                log.error("Incorrect column value macros: " + value);
                selectPart.append("'0' ");
            }
        } else if ("priority".equals(value)) {
            selectPart.append(target + ".priority ");
        } else if (value.startsWith("create_dt")) {
            addDateTimeParam(selectPart, target + ".create_dt", value);
        } else if ("create_user".equals(value) || "creator".equals(value)) {
            String alias = "create_user_" + target;

            selectPart.append(alias + ".title ");
            joinPart.append(" LEFT JOIN user AS " + alias + " ON " + alias + ".id=" + target + ".create_user_id ");
        } else if (value.startsWith("close_dt")) {
            addDateTimeParam(selectPart, target + ".close_dt", value);
        } else if ("close_user".equals(value)) {
            String alias = "close_user_" + target;

            selectPart.append(alias + ".title ");
            joinPart.append(" LEFT JOIN user AS " + alias + " ON " + alias + ".id=" + target + ".close_user_id ");
        } else if (value.startsWith("description")) {
            selectPart.append(target + ".description ");
        }
        // TODO: Warning when using deprecated 'text_param'
        else if (value.startsWith("param") || value.startsWith("text_param")) {
            ParamValueSelect.paramSelectQuery(value, target + ".id", selectPart, joinPart, false);
        } else if (value.startsWith("ifListParam:")) {
            String[] parts = StringUtils.substringAfter(value, "ifListParam:").split(":");

            if (parts.length > 1) {
                String existVal = "✓";
                String notExistVal = "✗";
                int paramId = Utils.parseInt(parts[0]);
                int paramValue = Utils.parseInt(parts[1]);

                Parameter param = ParameterCache.getParameter(paramId);
                if (param != null && Parameter.TYPE_LIST.equals(param.getType())) {
                    String alias = "param_" + paramId + "_list_value_" + paramValue;

                    if (parts.length > 2)
                        existVal = parts[2];
                    if (parts.length > 3)
                        notExistVal = parts[3];

                    selectPart.append(" IF(" + alias + ".value,'" + existVal + "','" + notExistVal + "') ");
                    joinPart.append(" LEFT JOIN " + Tables.TABLE_PARAM_LIST + " AS " + alias + " ON " + target + ".id= "
                            + alias + ".id AND " + alias + ".param_id=" + paramId + " AND " + alias + ".value="
                            + paramValue + " ");
                }
            } else {
                log.warn("Wrong condition: {}", value);
                return;
            }
        }
        // TODO: Warning when using deprecated 'linkedCustomerLink'
        else if (value.startsWith("linkCustomerLink") || value.startsWith("linkedCustomerLink")) {
            String alias = value;
            String columnAlias = alias + "Col";

            String customerLinkType = StringUtils.substringAfter(value, ":");

            selectPart.append("( SELECT GROUP_CONCAT( CONCAT( CONVERT( link.object_id, CHAR) , ':', " + alias
                    + ".title) SEPARATOR '$' ) " + " FROM " + TABLE_PROCESS_LINK + " AS link " + " INNER JOIN "
                    + TABLE_CUSTOMER + " AS " + alias + " ON link.object_id=" + alias + ".id " + " WHERE ");
            if (Utils.notBlankString(customerLinkType)) {
                selectPart.append("link.object_type='" + customerLinkType + "'");
            } else {
                selectPart.append("link.object_type LIKE '" + Customer.OBJECT_TYPE + "%'");
            }
            selectPart.append(
                    " AND link.process_id=" + target + ".id" + " GROUP BY link.process_id ) AS " + columnAlias + " ");
        }
        // TODO: Warning when using deprecated 'linkedCustomer:'
        else if (value.startsWith("linkCustomer:") || value.startsWith("linkedCustomer:")) {
            String[] parameters = value.split(":");

            if (parameters.length == 2) {

                String alias = parameters[0];
                String table = "customer";
                String column = parameters[1];

                selectPart.append("( SELECT GROUP_CONCAT(" + alias + "." + column + " SEPARATOR ', ')"
                        + " FROM process_link AS link " + " LEFT JOIN " + table + " AS " + alias + " ON link.object_id="
                        + alias + ".id WHERE link.object_type='" + table + "' AND link.process_id=" + target + ".id"
                        + " GROUP BY link.process_id ) AS " + table + " ");
            } else if (parameters.length == 3) {
                if (parameters[1].equals("param")) {
                    int paramId = Integer.parseInt(parameters[2]);

                    if (joinPart.indexOf("linked_customer") < 0) {
                        joinPart.append(" LEFT JOIN " + TABLE_PROCESS_LINK + " AS linked_customer ON " + target
                                + ".id=linked_customer.process_id AND linked_customer.object_type='customer' ");
                    }

                    ParamValueSelect.paramSelectQuery("param:" + paramId, "linked_customer.object_id", selectPart,
                            joinPart, false);
                }
            }
        }
        // TODO: Warning when using deprecated 'linkedObject:'
        else if (value.startsWith("linkObject:") || value.startsWith("linkedObject:")) {
            String[] parameters = value.split(":");

            if (parameters.length < 2) {
                return;
            }

            if (parameters[1].startsWith("process")) {
                String columnValue = parameters[1];

                selectPart.append(" ( SELECT GROUP_CONCAT(link.object_id SEPARATOR ', ') FROM process_link AS link "
                        + " WHERE link.object_type='" + columnValue + "' AND link.process_id=" + target + ".id "
                        + " GROUP BY link.process_id ) AS linked_process ");
            } else if (parameters[1].equals("contract")) {
                selectPart
                        .append(" ( SELECT GROUP_CONCAT(CONCAT(SUBSTRING_INDEX(link.object_type, ':', -1),':',CAST(link.object_id AS char),':',link.object_title) SEPARATOR ', ') FROM process_link AS link "
                                + " WHERE link.object_type LIKE 'contract%' AND link.process_id=" + target + ".id "
                                + " GROUP BY link.process_id ) AS contract ");
            }
            // FIXME: contract:ds - должно выводить названия договоров определённого только биллинга, но по сути вряд ли до сюда вообще доберётся когда-нибудь
            else if (parameters[1].startsWith("contract")) {
                String columnValue = "contract";
                selectPart.append(" ( SELECT GROUP_CONCAT(link.object_title SEPARATOR ', ') FROM process_link AS link "
                        + " WHERE link.object_type LIKE '" + columnValue + "%' AND link.process_id=" + target + ".id "
                        + " GROUP BY link.process_id ) AS contract ");
            }
            // тип объекта : id
            else if (parameters.length > 2 && parameters[2].equals("id")) {
                selectPart.append(" ( SELECT GROUP_CONCAT(link.object_id SEPARATOR ', ') FROM process_link AS link "
                        + " WHERE link.object_type LIKE '" + parameters[1] + "%' AND link.process_id=" + target + ".id "
                        + " GROUP BY link.process_id ) AS object_ids ");
            }
        } else if (value.startsWith("status:")) {
            String[] tokens = value.split(":");
            if (tokens.length < 3) {
                log.error("Incorrect column macros: " + value);
                return;
            }

            String statusList = tokens[1].trim();
            String whatSelect = tokens[2].trim();

            String statusString = statusList.replace(',', '_').replaceAll("\\s+", "");
            String tableAlias = "ps_" + statusString + "_" + target;
            String joinQuery = " LEFT JOIN " + TABLE_PROCESS_STATUS + " AS " + tableAlias + " ON " + tableAlias
                    + ".process_id=" + target + ".id AND " + tableAlias + ".status_id IN (" + statusList + ") AND "
                    + tableAlias + ".last";

            if (joinPart.indexOf(joinQuery) < 0) {
                joinPart.append(joinQuery);
            }

            if (whatSelect.startsWith("dt")) {
                addDateTimeParam(selectPart, tableAlias + ".dt", Utils.substringAfter(value, ":", 2));
            } else if ("comment".equals(whatSelect)) {
                selectPart.append(" CONCAT(DATE_FORMAT( " + tableAlias + ".dt, '%d.%m.%y %T' ) ,' [', " + tableAlias
                        + ".comment,']' ) ");
            } else if ("user".equals(whatSelect)) {
                String tableUserAlias = "su_" + statusString;

                joinPart.append(" LEFT JOIN " + TABLE_USER + " AS " + tableUserAlias + " ON " + tableAlias + ".user_id="
                        + tableUserAlias + ".id");
                selectPart.append(tableUserAlias + ".title ");
            } else if ("dt_comment_all".equals(whatSelect)) {
                selectPart.append(" ( SELECT GROUP_CONCAT( CONCAT(" + tableAlias + ".comment,' - ', CAST(" + tableAlias
                        + ".dt AS CHAR)) SEPARATOR '; ') " + " FROM process_status AS " + tableAlias + " WHERE "
                        + tableAlias + ".process_id=" + target + ".id AND " + tableAlias + ".status_id IN ("
                        + statusList + ") ) ");
            }
        } else if (value.startsWith("message:")) {
            String[] tokens = value.split(":");
            if (tokens.length < 3) {
                log.error("Incorrect macros: " + value);
                return;
            }
            if ("systemId".equals(tokens[2])) {
                // TODO: Support many columns with message: prefix
                String tableAlias = "messageSystemId";

                joinPart.append(" LEFT JOIN " + TABLE_MESSAGE + " AS " + tableAlias + " ON " + tableAlias + ".process_id=" + target + ".id AND "
                        + tableAlias + ".type_id IN (" + tokens[1] + ")");
                selectPart.append(tableAlias + ".system_id");
            }
        } else if (value.startsWith("message")) {
            String joinQuery = " LEFT JOIN " + TABLE_PROCESS_MESSAGE_STATE + " AS pm_state ON " + target
                    + ".id=pm_state.process_id " + " LEFT JOIN " + TABLE_MESSAGE
                    + " AS pm_last_in ON pm_state.in_last_id=pm_last_in.id " + " LEFT JOIN " + TABLE_MESSAGE
                    + " AS pm_last_out ON pm_state.out_last_id=pm_last_out.id " + " LEFT JOIN " + TABLE_USER
                    + " AS pm_last_in_user ON pm_last_in.user_id=pm_last_in_user.id " + " LEFT JOIN " + TABLE_USER
                    + " AS pm_last_out_user ON pm_last_out.user_id=pm_last_out_user.id";
            if (joinPart.indexOf(joinQuery) < 0) {
                joinPart.append(joinQuery);
            }

            if (value.equals("messageInCount")) {
                selectPart.append(" pm_state.in_count ");
            } else if (value.equals("messageInUnreadCount")) {
                selectPart.append(" pm_state.in_unread_count ");
            } else if (value.startsWith("messageInLastDt")) {
                addDateTimeParam(selectPart, "pm_state.in_last_dt", value);
            } else if (value.equals("messageInLastText")) {
                selectPart.append(" pm_last_in.text ");
            } else if (value.equals("messageInLastSubject")) {
                selectPart.append(" pm_last_in.subject ");
            } else if (value.equals("messageInLastUser")) {
                selectPart.append(" pm_last_in_user.title ");
            } else if (value.equals("messageOutCount")) {
                selectPart.append(" pm_state.out_count ");
            } else if (value.startsWith("messageOutLastDt")) {
                addDateTimeParam(selectPart, "pm_state.out_last_dt", value);
            } else if (value.equals("messageOutLastText")) {
                selectPart.append(" pm_last_out.text ");
            } else if (value.equals("messageOutLastSubject")) {
                selectPart.append(" pm_last_out.subject ");
            }else if (value.equals("messageOutLastUser")) {
                selectPart.append(" pm_last_out_user.title ");
            }
        }
        // колонка обрабатывается в JSP - список операций, в конфиге колонок чтобы была возможность ставить nowrap, выравнивание и т.п.
        // TODO: Сюда же можно перенести макросы выбора наименования типа, статуса и т.п., т.к. их можно выбрать из справочников
        else if (value.startsWith("executors") || value.startsWith("groups") || value.equals("actions")
                || value.startsWith("linkProcessList") || value.startsWith("linkedProcessList") || value.equals("N")) {
            selectPart.append("'0' ");
        }
        else {
            // TODO: This fallback is the correct one, fix everythere.
            log.error("Incorrect column value macros: {}", value);
            selectPart.append("'0' ");
        }

        selectPart.append(modifiers.getSecond());
        selectPart.append(", ");
    }

    private Pair<String, String> getModifiers() {
        String openTag = "", closeTag = "";
        String type = getConvert();
        if ("int".equals(type)) {
            openTag = " CAST((";
            closeTag = ") AS UNSIGNED) ";
        }
        return new Pair<>(openTag, closeTag);
    }

    // функция добавляет выбор требуемого параметра в нужном формате или без формата (value),
    // тогда столбец можно корректно использовать для сортировки
    private void addDateTimeParam(StringBuilder selectPart, String columnName, String value) {
        // нельзя делать substringAfterLast, чтобы не поломать двоеточия в формате дат!!
        String format = StringUtils.substringAfter(value, ":");
        // status:6:dt
        if (Utils.isBlankString(format) || format.equals("dt")) {
            selectPart.append(" DATE_FORMAT( ");
            selectPart.append(columnName);
            selectPart.append(", '%d.%m.%y %T' ) ");
        } else if (format.equals("value") || format.equals("nf")) {
            selectPart.append(columnName);
        }
        // произвольный формат
        else {
            selectPart.append(" DATE_FORMAT( ");
            selectPart.append(columnName);
            selectPart.append(", '");
            selectPart.append(SQLUtils.javaDateFormatToSql(format));
            selectPart.append("' ) ");
        }
    }

    private String addProcessStatusJoin(String target, StringBuilder joinPart) {
        String alias = "ps_" + target + "_data";

        String joinQuery = " LEFT JOIN " + TABLE_PROCESS_STATUS + " AS " + alias + " ON " + target + ".id=" + alias
                + ".process_id AND " + target + ".status_id=" + alias + ".status_id AND " + alias + ".last";

        if (joinPart.indexOf(joinQuery) < 0) {
            joinPart.append(joinQuery);
        }

        return alias;
    }

    public Object getCellValue(DynActionForm form, boolean isHtmlMedia, Process process, Object rawCellValue) throws SQLException {
        Object result = null;

        String columnValue = getValue();
        if (columnValue.startsWith("executor")) {
            String[] tokens = columnValue.split(":");

            Set<Integer> allowedGroupIds = Collections.emptySet();
            Set<Integer> allowedRoleIds = Collections.emptySet();

            if (tokens.length > 1) {
                allowedGroupIds = Utils.toIntegerSet(tokens[1]);
            }
            if (tokens.length > 2) {
                allowedRoleIds = Utils.toIntegerSet(tokens[2]);
            }

            StringBuilder executors = new StringBuilder(50);

            Set<Integer> executorIds = process.getExecutorIds();
            Set<Integer> allowedGroupsExecutors = process.getExecutorIdsWithGroups(allowedGroupIds);
            Set<Integer> allowedRolesExecutors = process.getExecutorIdsWithRoles(allowedRoleIds);

            for (User user : UserCache.getUserList()) {
                if (executorIds.contains(user.getId()) && (allowedGroupIds.size() == 0 || allowedGroupsExecutors.contains(user.getId()))
                        && (allowedRoleIds.size() == 0 || allowedRolesExecutors.contains(user.getId()))) {
                    Utils.addSeparated(executors, ", ", user.getTitle());
                }
            }

            result = executors.toString();
        } else if (columnValue.startsWith("groups")) {
            Set<Integer> allowedRoleIds = Utils.toIntegerSet(StringUtils.substringAfterLast(columnValue, ":"));

            StringBuilder groups = new StringBuilder(50);

            Set<Integer> groupIds = process.getGroupIds();
            Set<Integer> allowedRolesGroups = process.getGroupIdsWithRoles(allowedRoleIds);

            for (Group group : UserCache.getUserGroupList()) {
                if (groupIds.contains(group.getId()) && (allowedRoleIds.size() == 0 || allowedRolesGroups.contains(group.getId()))) {
                    Utils.addSeparated(groups, ", ", group.getTitle());
                }
            }

            result = groups.toString();
        }
        // TODO: Сделать подзапросом сразу в ProcessDAO.
        // linkProcessList:depend:open or linkedProcessList:depend:open
        else if (columnValue.startsWith("linkProcessList") || columnValue.startsWith("linkedProcessList")) {
            String[] tokens = columnValue.split(":");

            String linkTypeFilter = tokens.length > 1 ? tokens[1] : "*";
            String stateFilter = tokens.length > 2 ? tokens[2] : "open";
            String typeFilter = tokens.length > 3 ? tokens[3] : "*";

            String linkTypeParam = linkTypeFilter.equals("*") ? null : linkTypeFilter;
            boolean stateParam = stateFilter.equals("open");
            Set<Integer> typeParam = typeFilter.equals("*") ? null : Utils.toIntegerSet(typeFilter);

            ProcessLinkDAO dao = new ProcessLinkDAO(form.getConnectionSet().getSlaveConnection(), form);

            if (columnValue.startsWith("linkProcessList"))
                result = dao.getLinkProcessList(process.getId(), linkTypeParam, stateParam, typeParam);
            else
                result = dao.getLinkedProcessList(process.getId(), linkTypeParam, stateParam, typeParam);
        } else {
            if (isHtmlMedia) {
                result = rawCellValue;
            }
            //FIXME: Это должно быть в плагине BGBilling.
            else if (columnValue.startsWith("linkObject:contract")) {
                result = String.valueOf(rawCellValue).replaceAll("\\w+:\\d+:", "");
            } else {
                result = String.valueOf(rawCellValue).replace("\n", " ").replace("\r", " ");
            }
        }

        return result;
    }

    /**
     * @return special cell rendering HTML page, can be overwritten by different cells.
     */
    public String cellHtml(Process process, Object col) {
        return null;
    }
}
