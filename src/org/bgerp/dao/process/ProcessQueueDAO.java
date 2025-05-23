package org.bgerp.dao.process;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.Pageable;
import org.bgerp.model.param.Parameter;
import org.bgerp.model.process.queue.Column;
import org.bgerp.model.process.queue.filter.Filter;
import org.bgerp.model.process.queue.filter.FilterCustomerParam;
import org.bgerp.model.process.queue.filter.FilterGrEx;
import org.bgerp.model.process.queue.filter.FilterLinkObject;
import org.bgerp.model.process.queue.filter.FilterList;
import org.bgerp.model.process.queue.filter.FilterOpenClose;
import org.bgerp.model.process.queue.filter.FilterParam;
import org.bgerp.model.process.queue.filter.FilterProcessType;
import org.bgerp.util.sql.LikePattern;

import ru.bgcrm.dao.ParamValueSelect;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.customer.Customer;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

public class ProcessQueueDAO extends ProcessDAO {
    public static final String LINKED_PROCESS_JOIN = " LEFT JOIN " + TABLE_PROCESS_LINK
            + " AS pllp ON pllp.object_id=process.id AND pllp.object_type LIKE 'process%' " + " LEFT JOIN "
            + TABLE_PROCESS + " AS " + LINKED_PROCESS + " ON pllp.process_id=" + LINKED_PROCESS + ".id";

    /**
     * Constructor without user isolation.
     * @param con DB connection.
     */
    public ProcessQueueDAO(Connection con) {
        super(con);
    }

    /**
     * Constructor with isolation support.
     * @param con DB connection.
     * @param form value of {@link #form}.
     */
    public ProcessQueueDAO(Connection con, DynActionForm form) {
        super(con, form);
    }

    /**
     * Selects processes for a queue's.
     * @param searchResult
     * @param aggregatedValues if not null - aggregated values are stored there.
     * @param queue
     * @param form
     * @throws Exception
     */
    public void searchProcess(Pageable<Object[]> searchResult, List<String> aggregatedValues, Queue queue, DynActionForm form) throws Exception {
        QueueSelectParams params = prepareQueueSelect(queue);

        addFilters(params.queue, form, params);

        String orders = queue.getSortSet().getOrders(form);

        Page page = searchResult.getPage();

        StringBuilder query = new StringBuilder(1000);

        query.append("SELECT DISTINCT SQL_CALC_FOUND_ROWS ");
        query.append(params.selectPart);
        query.append(" FROM " + TABLE_PROCESS + " AS process");
        query.append(params.joinPart);
        query.append(params.wherePart);
        if (Utils.notBlankString(orders)) {
            query.append(SQL_ORDER_BY);
            query.append(orders);
        }
        query.append(page.getLimitSql());

        log.debug(query.toString());

        var ps = con.prepareStatement(query.toString());

        final boolean selectLinked = params.joinPart.indexOf(LINKED_PROCESS_JOIN) > 0;

        final List<Object[]> list = searchResult.getList();

        final int columns = params.queue.getColumnList().size();

        var rs = ps.executeQuery();
        while (rs.next()) {
            Process process = getProcessFromRs(rs, "process.");
            Process linkedProcess = selectLinked ? getProcessFromRs(rs, LINKED_PROCESS + ".") : null;

            Object[] row = new Object[columns + 1];

            row[0] = new Process[] { process, linkedProcess };
            for (int i = 1; i <= columns; i++)
                row[i] = rs.getObject(i);

            list.add(row);
        }

        if (page != null)
            page.setRecordCount(ps);

        ps.close();

        loadFormattedAddressParamValues(searchResult, queue);

        if (aggregatedValues != null && params.selectAggregatePart != null)
            selectAggregatedValues(aggregatedValues, params, columns);
    }

    protected QueueSelectParams prepareQueueSelect(Queue queue) throws Exception {
        QueueSelectParams result = new QueueSelectParams();

        result.queue = queue;

        result.selectPart = new StringBuilder("");
        result.joinPart = new StringBuilder();

        result.wherePart = new StringBuilder(SQL_WHERE);
        result.wherePart.append("process.type_id IN (");
        result.wherePart.append(Utils.toString(queue.getProcessTypeIds(), "-1", ","));
        result.wherePart.append(") AND process.id>0");

        boolean hasAggregateColumns = addColumns(queue, result.selectPart, result.joinPart, false);
        if (hasAggregateColumns) {
            result.selectAggregatePart = new StringBuilder();
            addColumns(queue, result.selectAggregatePart, new StringBuilder(), true);
            result.selectAggregatePart.append("0");
        }

        result.selectPart.append("process.*");

        if (result.joinPart.indexOf(LINKED_PROCESS_JOIN) > 0) {
            result.selectPart.append("," + LINKED_PROCESS + ".*");
        }

        result.joinPart.append(getIsolationJoin(form, "process"));

        return result;
    }

    private void loadFormattedAddressParamValues(Pageable<Object[]> searchResult, Queue queue) throws SQLException {
        final var columnList = queue.getColumnList();
        final int length = columnList.size();

        for (int i = 0; i < length; i++) {
            Column col = columnList.get(i);

            String value = col.getValue();
            if (!value.startsWith("param:")) {
                continue;
            }

            // если код параметра между двоеточиями, то указан либо формат либо поле адресного параметра
            // либо :value для параметра date(time)
            int paramId = Utils.parseInt(StringUtils.substringBetween(value, ":"));
            if (paramId <= 0) {
                continue;
            }

            Parameter param = ParameterCache.getParameter(paramId);
            if (param == null) {
                log.warn("Queue: " + queue.getId() + "; incorrect column expression, param not found: " + value);
                continue;
            }

            // это не ошибка, просто параметр не того типа
            if (!Parameter.TYPE_ADDRESS.equals(param.getType())) {
                continue;
            }

            String formatName = StringUtils.substringAfterLast(value, ":");

            // это не формат а поле адресного параметра
            if (ParamValueSelect.PARAM_ADDRESS_FIELDS.contains(formatName)) {
                continue;
            }

            for (Object[] row : searchResult.getList()) {
                StringBuilder newValue = new StringBuilder(100);

                if (row[i + 1] == null) {
                    continue;
                }

                for (String token : Utils.maskNull(row[i + 1].toString()).split("\\|")) {
                    String[] addressData = token.split(":", -1);

                    ParameterAddressValue addrValue = new ParameterAddressValue();
                    addrValue.setHouseId(Utils.parseInt(addressData[0]));
                    addrValue.setFlat(addressData[1]);
                    addrValue.setRoom(addressData[2]);
                    addrValue.setPod(Utils.parseInt(addressData[3]));
                    addrValue.setFloor(Utils.parseInt(addressData[4]));
                    addrValue.setComment(addressData[5]);

                    Utils.addSeparated(newValue, "; ", AddressUtils.buildAddressValue(addrValue, con, formatName));
                }

                row[i + 1] = newValue.toString();
            }
        }
    }

    private void selectAggregatedValues(List<String> aggregateValues, QueueSelectParams params, int columns) throws SQLException {
        var query = new StringBuilder(100);
        query.append("SELECT ");
        query.append(params.selectAggregatePart);
        query.append(" FROM " + TABLE_PROCESS + " AS process");
        query.append(params.joinPart);
        query.append(params.wherePart);

        var ps = con.prepareStatement(query.toString());

        var rs = ps.executeQuery();
        if (rs.next()) {
            for (int i = 0; i < columns; i++) {
                aggregateValues.add(rs.getString(i + 1));
            }
        }
        ps.close();

        log.debug("Aggregated values: {}", aggregateValues);
    }

    public String getCountQuery(Queue queue, DynActionForm form) throws Exception {
        QueueSelectParams params = prepareQueueSelect(queue);
        addFilters(params.queue, form, params);
        StringBuilder query = new StringBuilder();
        query.append("SELECT COUNT(DISTINCT process.id) ");
        query.append(" FROM " + TABLE_PROCESS + " AS process");
        query.append(params.joinPart);
        query.append(params.wherePart);

        return query.toString();
    }

    protected void addFilters(Queue queue, DynActionForm form, QueueSelectParams params) {
        StringBuilder joinPart = params.joinPart;
        StringBuilder wherePart = params.wherePart;

        FilterList filterList = queue.getFilterList();

        for (Filter f : filterList.getFilterList()) {
            final String type = f.getType();

            if ("groups".equals(type)) {
                Filter filter = f;

                String groupIds = Utils.toString(form.getParamValues("group"));
                if (Utils.isBlankString(groupIds) && filter.getOnEmptyValues().size() > 0) {
                    groupIds = Utils.toString(filter.getOnEmptyValues());
                }

                // При выборе текущего исполнителя фильтр по группам не учитывался - убрал.
                if (Utils.notBlankString(groupIds) /*&& !currentUserMode*/) {
                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(Tables.TABLE_PROCESS_GROUP);
                    joinPart.append("AS ig ON process.id=ig.process_id AND ig.group_id IN(");
                    joinPart.append(groupIds);
                    joinPart.append(")");
                }
            } else if ("executors".equals(type)) {
                Filter filter = f;

                Set<String> executorIds = form.getParamValuesStr("executor");
                if (executorIds.contains("current")) {
                    executorIds.remove("current");
                    executorIds.add(String.valueOf(form.getUserId()));
                }

                // hard filter with only the current executor
                if (filter.getValues().contains("current")) {
                    executorIds = Collections.singleton(String.valueOf(form.getUserId()));
                }

                boolean includeCreateUser = false;
                if (Utils.parseBoolean(filter.getConfigMap().get("includeCreateUser"))) {
                    includeCreateUser = true;
                }

                if (executorIds.size() > 0) {
                    if (executorIds.contains("empty")) {
                        wherePart.append(" AND process.executors=''");
                    } else {
                        executorIds.remove("empty");

                        String executorIdsStr = Utils.toString(executorIds);

                        joinPart.append(SQL_LEFT_JOIN);
                        joinPart.append(Tables.TABLE_PROCESS_EXECUTOR);
                        joinPart.append("AS ie ON process.id=ie.process_id ");

                        wherePart.append(" AND ( ie.user_id IN(");
                        wherePart.append(executorIdsStr);
                        wherePart.append(")");
                        if (includeCreateUser) {
                            wherePart.append(" OR process.create_user_id IN(");
                            wherePart.append(executorIdsStr);
                            wherePart.append(")");
                        }

                        wherePart.append(") ");
                    }
                }
            } else if ("create_user".equals(type)) {
                var userIds = form.getParamValues("create_user");
                if (!userIds.isEmpty()) {
                    wherePart
                        .append(" AND process.create_user_id IN (")
                        .append(Utils.toString(userIds))
                        .append(") ");
                }
            } else if ("close_user".equals(type)) {
                var userIds = form.getParamValues("close_user");
                if (!userIds.isEmpty()) {
                    wherePart
                        .append(" AND process.close_user_id IN (")
                        .append(Utils.toString(userIds))
                        .append(") ");
                }
            } else if (f instanceof FilterGrEx filter) {
                filter.apply(form, params);
            } else if (f instanceof FilterProcessType) {
                Filter filter = f;

                String typeIds = Utils.toString(form.getParamValues("type"));
                if (Utils.isBlankString(typeIds) && filter.getOnEmptyValues().size() > 0) {
                    typeIds = Utils.toString(filter.getOnEmptyValues());
                }

                // жёстко заданные типы процессов убраны, т.к. фильтр по типам есть в очереди всегда

                if (Utils.notBlankString(typeIds)) {
                    wherePart.append(" AND process.type_id IN (");
                    wherePart.append(typeIds);
                    wherePart.append(")");
                }
            } else if ("quarter".equals(type)) {
                FilterParam filter = (FilterParam) f;

                int paramId = filter.getParameter().getId();
                String values = Utils.toString(form.getParamValues("quarter"));
                if (Utils.notBlankString(values)) {

                    String alias = "param_list_" + paramId;

                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_LIST);
                    joinPart.append("AS " + alias + " ON process.id=" + alias + ".id AND " + alias + ".param_id="
                            + paramId + " AND " + alias + ".value IN(" + values + ") ");
                }
            } else if (f instanceof FilterOpenClose) {
                String openCloseFilterValue = form.getParam("openClose", ((FilterOpenClose) f).getDefaultValue());

                if (f.getValues().size() > 0)
                    openCloseFilterValue = f.getValues().iterator().next();

                if (FilterOpenClose.OPEN.equals(openCloseFilterValue)) {
                    wherePart.append(" AND process.close_dt IS NULL ");
                } else if (FilterOpenClose.CLOSE.equals(openCloseFilterValue)) {
                    wherePart.append(" AND process.close_dt IS NOT NULL ");
                }
            } else if (f instanceof FilterCustomerParam) {
                FilterCustomerParam filter = (FilterCustomerParam) f;

                //FIXME: Тут нет проверки, что параметр привязанного контрагента "list".

                int paramId = filter.getParameter().getId();

                String values = Utils.toString(form.getParamValues("param" + paramId + "value"));
                if (Utils.isBlankString(values) && filter.getOnEmptyValues().size() > 0) {
                    values = Utils.toString(filter.getOnEmptyValues());
                }
                if (Utils.isBlankString(values)) {
                    continue;
                }

                String customerLinkAlias = "customer_link_" + paramId;

                joinPart.append(SQL_INNER_JOIN);
                joinPart.append(TABLE_PROCESS_LINK);
                joinPart.append("AS " + customerLinkAlias + " ON process.id=" + customerLinkAlias + ".process_id AND "
                        + customerLinkAlias + ".object_type='" + Customer.OBJECT_TYPE + "'");
                joinPart.append(SQL_INNER_JOIN);
                joinPart.append(org.bgerp.dao.param.Tables.TABLE_PARAM_LIST);
                joinPart.append(
                        "AS param_list ON " + customerLinkAlias + ".object_id=param_list.id AND param_list.param_id="
                                + paramId + " AND param_list.value IN(" + values + ")");
            } else if (f instanceof FilterParam filter) {
                filter.apply(form, params);
            } else if ("status".equals(type)) {
                String statusIds = f.getValues(form, "status");

                if (Utils.notBlankString(statusIds)) {
                    wherePart.append(" AND process.status_id IN (");
                    wherePart.append(statusIds);
                    wherePart.append(")");
                }
            } else if ("create_date".equals(type)) {
                addDateFilter(form, wherePart, "dateCreate", "create_dt");
            } else if ("close_date".equals(type)) {
                addDateFilter(form, wherePart, "dateClose", "close_dt");
            } else if ("code".equals(type)) {
                int code = Utils.parseInt(form.getParam("code"));
                if (code > 0) {
                    wherePart.append(" AND process.id=");
                    wherePart.append(code);
                }
            } else if ("description".equals(type)) {
                String description = form.getParam("description");
                if (Utils.notBlankString(description)) {
                    wherePart.append(" AND POSITION( '");
                    wherePart.append(description);
                    wherePart.append("' IN process.description)>0");
                }
            } else if ("status_date".equals(type)) {
                int statusId = form.getParamInt("dateStatusStatus", -1);
                Date dateFrom = TimeUtils.parse(form.getParam("dateStatusFrom"), TimeUtils.FORMAT_TYPE_YMD);
                Date dateTo = TimeUtils.parse(form.getParam("dateStatusTo"), TimeUtils.FORMAT_TYPE_YMD);

                if (dateFrom != null || dateTo != null) {
                    joinPart.append(
                            " INNER JOIN process_status ON process.id=process_status.process_id AND process_status.status_id=");
                    joinPart.append(statusId);
                    if (dateFrom != null) {
                        joinPart.append(" AND process_status.dt>=" + TimeUtils.formatSqlDate(dateFrom));
                    }
                    if (dateTo != null) {
                        joinPart.append(
                                " AND process_status.dt<" + TimeUtils.formatSqlDate(TimeUtils.getNextDay(dateTo)));
                    }
                }
            } else if ("linkedCustomer:title".equals(type)) {
                String customerTitle = form.getParam("linkedCustomer:title");
                if (Utils.notEmptyString(customerTitle)) {
                    String linkedCustomerAlias = "linked_customer_title_filter_link";
                    String customerAlias = "linked_customer_title_filter_customer";
                    joinPart.append(SQL_LEFT_JOIN);
                    joinPart.append(TABLE_PROCESS_LINK);
                    joinPart.append(" AS " + linkedCustomerAlias + " ON process.id=" + linkedCustomerAlias
                            + ".process_id AND " + linkedCustomerAlias + ".object_type LIKE '"
                            + LikePattern.START.get(Customer.OBJECT_TYPE) + "'");
                    joinPart.append(SQL_LEFT_JOIN);
                    joinPart.append(TABLE_CUSTOMER);
                    joinPart.append(" AS " + customerAlias + " ON " + linkedCustomerAlias + ".object_id="
                            + customerAlias + ".id ");

                    wherePart.append(SQL_AND);
                    wherePart.append(" " + customerAlias + ".title LIKE '%" + customerTitle + "%' ");
                }
            } else if ("linkedObject".equals(type)) {
                ConfigMap configMap = f.getConfigMap();
                String objectTypeMask = configMap.get("objectTypeMask");
                String objectTitleRegExp = configMap.get("objectTitleRegExp");
                boolean notMode = configMap.getBoolean("notMode", false);

                if (!Utils.isEmptyString(objectTypeMask) && !Utils.isEmptyString(objectTitleRegExp)) {
                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(TABLE_PROCESS_LINK);
                    joinPart.append("AS linked_object_filter ");
                    joinPart.append("ON linked_object_filter.process_id = process.id ");
                    joinPart.append("AND linked_object_filter.object_type LIKE '" + objectTypeMask + "' ");
                    joinPart.append("AND linked_object_filter.object_title ");

                    if (notMode) {
                        joinPart.append("NOT ");
                    }

                    joinPart.append("REGEXP '" + objectTitleRegExp + "'");
                }
            } else if ("message:systemId".equals(type)) {
                String systemId = form.getParam("message:systemId");
                if (Utils.notEmptyString(systemId)) {
                    String messageAlias = "linked_message";
                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(TABLE_MESSAGE);
                    joinPart.append(" AS " + messageAlias + " ON process.id=" + messageAlias + ".process_id ");

                    wherePart.append(SQL_AND);
                    wherePart.append(" " + messageAlias + ".system_id = '" + systemId + "' ");
                }
            } else if (f instanceof FilterLinkObject) {
                FilterLinkObject filter = (FilterLinkObject) f;

                String value = form.getParam(filter.getParamName());
                if (!filter.getValues().isEmpty())
                    value = Utils.getFirst(filter.getValues());

                if (Utils.notBlankString(value)) {
                    String joinTableName = " link_obj_f_" + filter.getId() + " ";

                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(TABLE_PROCESS_LINK);
                    joinPart.append("AS " + joinTableName);
                    joinPart.append("ON " + joinTableName + ".process_id=process.id AND " + joinTableName
                            + ".object_type='" + filter.getObjectType() + "' ");

                    if (FilterLinkObject.WHAT_FILTER_ID.equals(filter.getWhatFilter())) {
                        joinPart.append("AND " + joinTableName + ".object_id='" + value + "'");
                    }
                    else if (FilterLinkObject.WHAT_FILTER_TITLE.equals(filter.getWhatFilter())) {
                        joinPart.append("AND " + joinTableName + ".object_title LIKE '%" + value + "%'");
                    }
                    else {
                        log.error( "Incorrect linkObject filter( " + filter.getId() + " )! Not a valid value \"whatFiltered\". " );
                    }
                }
            }
        }
    }

    private void addDateFilter(DynActionForm form, StringBuilder wherePart, String paramPrefix, String column) {
        Date dateFrom = TimeUtils.parse(form.getParam(paramPrefix + "From"), TimeUtils.FORMAT_TYPE_YMD);
        Date dateTo = TimeUtils.parse(form.getParam(paramPrefix + "To"), TimeUtils.FORMAT_TYPE_YMD);
        if (dateFrom != null) {
            wherePart.append(" AND process." + column + ">=" + TimeUtils.formatSqlDate(dateFrom));
        }
        if (dateTo != null) {
            wherePart.append(" AND process." + column + "<" + TimeUtils.formatSqlDate(TimeUtils.getNextDay(dateTo)));
        }
    }

    // TODO: Extract hasAggregateFunctions to a separated method.
    /**
     * Appends column expression in SQL query.
     * @param queue queue with columns.
     * @param selectPart SELECT query part.
     * @param joinPart JOIN query part.
     * @param aggregate
     * @return existence of an aggregating function.
     * @throws Exception
     */
    private boolean addColumns(Queue queue, StringBuilder selectPart, StringBuilder joinPart, boolean aggregate) throws Exception {
        StringBuilder selectPartBuffer = new StringBuilder(60);

        boolean aggregateFunctions = false;
        for (Column col : queue.getColumnList()) {
            String aggregateFunction = col.getAggregate();
            boolean hasAggregateFunction = Utils.notBlankString(aggregateFunction);

            aggregateFunctions = aggregateFunctions || hasAggregateFunction;

            if (aggregate) {
                if (hasAggregateFunction) {
                    selectPartBuffer.setLength(0);
                    selectPartBuffer.append(aggregateFunction);
                    selectPartBuffer.append("(");

                    // вся это свистопляска с размером нужна, т.к. в случае ошибки в конфигурации
                    // addColumn не добавляет столбец напрочь
                    int lengthBefore = selectPartBuffer.length();

                    col.addQuery(selectPartBuffer, joinPart);

                    if (selectPartBuffer.length() != lengthBefore) {
                        int pos = selectPartBuffer.lastIndexOf(",");
                        selectPart.append(selectPartBuffer.substring(0, pos - 1));
                        selectPart.append(")");
                        selectPart.append(selectPartBuffer.substring(pos));
                    }
                } else {
                    selectPart.append("NULL,");
                }
            } else {
                col.addQuery(selectPart, joinPart);
            }
        }

        return aggregateFunctions;
    }
}
