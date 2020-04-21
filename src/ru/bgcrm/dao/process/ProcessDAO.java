package ru.bgcrm.dao.process;

import static ru.bgcrm.dao.Tables.TABLE_CUSTOMER;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_BLOB;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LIST;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LISTCOUNT;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_LOG;
import static ru.bgcrm.dao.Tables.TABLE_PARAM_TEXT;
import static ru.bgcrm.dao.message.Tables.TABLE_MESSAGE;
import static ru.bgcrm.dao.message.Tables.TABLE_PROCESS_MESSAGE_STATE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_EXECUTOR;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_GROUP;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LINK;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_LOG;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_STATUS_TITLE;
import static ru.bgcrm.dao.process.Tables.TABLE_PROCESS_TYPE;
import static ru.bgcrm.dao.user.Tables.TABLE_USER;
import static ru.bgcrm.dao.user.Tables.TABLE_USER_GROUP;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.EntityLogDAO;
import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.CommonObjectLink;
import ru.bgcrm.model.Customer;
import ru.bgcrm.model.EntityLogItem;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.Page;
import ru.bgcrm.model.Pair;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.config.IsolationConfig;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterAddressValue;
import ru.bgcrm.model.param.ParameterSearchedObject;
import ru.bgcrm.model.param.address.AddressHouse;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessExecutor;
import ru.bgcrm.model.process.ProcessGroup;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.queue.Filter;
import ru.bgcrm.model.process.queue.FilterCustomerParam;
import ru.bgcrm.model.process.queue.FilterGrEx;
import ru.bgcrm.model.process.queue.FilterLinkObject;
import ru.bgcrm.model.process.queue.FilterList;
import ru.bgcrm.model.process.queue.FilterOpenClose;
import ru.bgcrm.model.process.queue.FilterParam;
import ru.bgcrm.model.process.queue.FilterProcessType;
import ru.bgcrm.model.process.queue.SortMode;
import ru.bgcrm.model.process.queue.SortSet;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.action.ProcessAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.AddressUtils;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgcrm.util.sql.SQLUtils;

public class ProcessDAO extends CommonDAO {
    public static final String LINKED_PROCESS = "linked";
    private static final String LINKED_PROCESS_JOIN = " LEFT JOIN " + TABLE_PROCESS_LINK
            + " AS pllp ON pllp.object_id=process.id AND pllp.object_type LIKE 'process%' " + " LEFT JOIN "
            + TABLE_PROCESS + " AS " + LINKED_PROCESS + " ON pllp.process_id=" + LINKED_PROCESS + ".id";

    private final int userId;
    protected final User user;
    private boolean history;

    /**
     * Конструктор с полным доступом, без поддержки изоляций.
     * @param con
     */
    public ProcessDAO(Connection con) {
        super(con);
        this.userId = User.USER_SYSTEM_ID;
        this.user = User.USER_SYSTEM;
    }
    
    /**
     * Конструктор с поддержкой изоляции процессов.
     * @param con
     * @param user
     */
    public ProcessDAO(Connection con, User user) {
        super(con);
        this.userId = user.getId();
        this.user = user;
    }

    public ProcessDAO(Connection con, boolean history) {
        this(con);
        this.history = history;
    }

    @Deprecated
    public ProcessDAO(Connection con, boolean history, int userId) {
        super(con);
        this.userId = userId;
        this.user = User.USER_SYSTEM;
        this.history = history;
    }
    
    public ProcessDAO(Connection con, User user, boolean history) {
        this(con, user);
        this.history = history;
    }

    public static Process getProcessFromRs(ResultSet rs, String prefix) throws SQLException {
        Process process = new Process();

        process.setId(rs.getInt(prefix + "id"));
        process.setTitle(rs.getString(prefix + "title"));
        process.setDescription(rs.getString(prefix + "description"));
        process.setTypeId(rs.getInt(prefix + "type_id"));
        process.setStatusId(rs.getInt(prefix + "status_id"));
        process.setStatusUserId(rs.getInt(prefix + "status_user_id"));
        process.setCreateUserId(rs.getInt(prefix + "create_user_id"));
        process.setCloseUserId(rs.getInt(prefix + "close_user_id"));
        process.setPriority(rs.getInt(prefix + "priority"));
        process.setCreateTime(rs.getTimestamp(prefix + "create_dt"));
        process.setCloseTime(rs.getTimestamp(prefix + "close_dt"));
        process.setStatusTime(rs.getTimestamp(prefix + "status_dt"));
        //process.setLastMessageTime( rs.getTimestamp( prefix + "last_message_dt" ) );

        List<IdTitle> idTitle = Utils.parseIdTitleList(rs.getString(prefix + "groups"), "0");
        Set<ProcessGroup> processGroups = new LinkedHashSet<ProcessGroup>();

        for (IdTitle item : idTitle) {
            ProcessGroup processGroup = new ProcessGroup();
            processGroup.setGroupId(item.getId());
            processGroup.setRoleId(Integer.parseInt(item.getTitle()));

            processGroups.add(processGroup);
        }

        process.setProcessGroups(processGroups);
        process.setProcessExecutors(ProcessExecutor.parseSafe(rs.getString(prefix + "executors"), processGroups));

        return process;
    }

    public static Process getProcessFromRs(ResultSet rs) throws SQLException {
        return getProcessFromRs(rs, "process.");
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

        boolean hasAggregateColumns = addColumnList(queue, result.selectPart, result.joinPart, false);
        if (hasAggregateColumns) {
            result.selectAggregatePart = new StringBuilder();
            addColumnList(queue, result.selectAggregatePart, new StringBuilder(), true);
            result.selectAggregatePart.append("0");
        }

        result.selectPart.append("process.*");

        if (result.joinPart.indexOf(LINKED_PROCESS_JOIN) > 0) {
            result.selectPart.append("," + LINKED_PROCESS + ".*");
        }
        
        result.joinPart.append(getIsolationJoin(user));
        
        return result;
    }
    
    public static String getIsolationJoin(User user) {
        IsolationConfig isolation = user.getConfigMap().getConfig(IsolationConfig.class);
        if (isolation.getIsolationProcess() != null)
            switch (isolation.getIsolationProcess()) {
                case EXECUTOR:
                    return " INNER JOIN " + TABLE_PROCESS_EXECUTOR + " AS isol_e ON process.id=isol_e.process_id AND isol_e.user_id=" + user.getId() + " ";
                case GROUP:
                    return " INNER JOIN " + TABLE_PROCESS_GROUP + " AS isol_pg ON process.id=isol_pg.process_id " 
                    + "INNER JOIN " + TABLE_USER_GROUP + " AS isol_ur ON isol_ur.group_id=isol_pg.group_id AND isol_ur.user_id=" + user.getId()
                    + " AND (isol_ur.date_to IS NULL OR CURDATE()<=isol_ur.date_to) ";
                }
        return "";
    }

    /**
     * Выбирает процессы и связанные данные для очереди процессов.
     * @param searchResult
     * @param aggregateValues
     * @param queue
     * @param form
     * @throws Exception
     */
    public void searchProcess(SearchResult<Object[]> searchResult, List<String> aggregateValues, Queue queue, DynActionForm form) throws Exception {
        QueueSelectParams params = prepareQueueSelect(queue);
        
        addFilters(params.queue, form, params);

        String orders = Utils.toString(form.getSelectedValuesListStr("sort", "0"));

        SortSet sortSet = queue.getSortSet();
        // сортировки жёстко заданы в очереди
        if (sortSet.getSortValues().size() > 0) {
            orders = "";
            for (Integer value : sortSet.getSortValues().values()) {
                int pos = value - 1;
                if (pos < 0 || pos >= sortSet.getModeList().size()) {
                    log.error("Incorrect sort value in queue: " + value);
                    continue;
                }

                SortMode mode = sortSet.getModeList().get(pos);
                if (orders.length() != 0) {
                    orders += ",";
                }
                orders += mode.getOrderExpression();
            }
        }

        if (searchResult != null) {
            Page page = searchResult.getPage();

            List<Object[]> list = searchResult.getList();

            final int columns = params.queue.getColumnList().size();

            ResultSet rs = null;
            PreparedStatement ps = null;
            StringBuilder query = new StringBuilder();

            query.append("SELECT DISTINCT SQL_CALC_FOUND_ROWS ");
            query.append(params.selectPart);
            query.append(" FROM " + TABLE_PROCESS + " AS process");
            query.append(params.joinPart);
            query.append(params.wherePart);
            if (Utils.notBlankString(orders)) {
                query.append(SQL_ORDER_BY);
                query.append(orders);
            }
            query.append(getPageLimit(page));

            if (log.isDebugEnabled()) {
                log.debug(query.toString());
            }

            ps = con.prepareStatement(query.toString());

            final boolean selectLinked = params.joinPart.indexOf(LINKED_PROCESS_JOIN) > 0;

            rs = ps.executeQuery();
            while (rs.next()) {
                Process process = getProcessFromRs(rs, "process.");
                Process linkedProcess = selectLinked ? getProcessFromRs(rs, LINKED_PROCESS + ".") : null;

                Object[] row = new Object[columns + 1];

                // 0 столбец - под Process
                row[0] = new Process[] { process, linkedProcess };

                for (int i = 1; i <= columns; i++) {
                    row[i] = rs.getString(i);
                }
                list.add(row);
            }

            if (page != null) {
                page.setRecordCount(getFoundRows(ps));
            }
            ps.close();

            // подгрузка адресов с произвольным форматированием
            final List<ParameterMap> columnList = queue.getColumnList();
            final int length = columnList.size();

            for (int i = 0; i < length; i++) {
                ParameterMap col = columnList.get(i);

                String value = col.get("value");
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
                if (ParamValueDAO.PARAM_ADDRESS_FIELDS.contains(formatName)) {
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

            // агрегатные функции
            if (params.selectAggregatePart != null) {
                query.setLength(0);
                query.append("SELECT ");
                query.append(params.selectAggregatePart);
                query.append(" FROM " + TABLE_PROCESS + " AS process");
                query.append(params.joinPart);
                query.append(params.wherePart);

                if (log.isDebugEnabled()) {
                    log.debug(query.toString());
                }

                ps = con.prepareStatement(query.toString());

                rs = ps.executeQuery();
                if (rs.next()) {
                    for (int i = 0; i < columns; i++) {
                        aggregateValues.add(rs.getString(i + 1));
                    }
                }
                ps.close();

                if (log.isDebugEnabled()) {
                    log.debug("Aggregated values: " + aggregateValues);
                }
            }
        }
    }

    public String getCountQuery(Queue queue, DynActionForm form) throws BGException {
        try {
            QueueSelectParams params = prepareQueueSelect(queue);
            addFilters(params.queue, form, params);
            StringBuilder query = new StringBuilder();
            query.append("SELECT COUNT(DISTINCT process.id) ");
            query.append(" FROM " + TABLE_PROCESS + " AS process");
            query.append(params.joinPart);
            query.append(params.wherePart);

            return query.toString();
        } catch (Exception e) {
            throw new BGException("Queue " + queue.getId() + " " + e.getMessage(), e);
        }
    }

    private void addFilters(Queue queue, DynActionForm form, QueueSelectParams params) {
        StringBuilder joinPart = params.joinPart;
        StringBuilder wherePart = params.wherePart;

        FilterList filterList = queue.getFilterList();

        for (Filter f : filterList.getFilterList()) {
            String type = f.getType();

            if ("groups".equals(type)) {
                Filter filter = f;

                String groupIds = Utils.toString(form.getSelectedValues("group"));
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

                Set<String> executorIds = form.getSelectedValuesStr("executor");
                if (executorIds.contains("current")) {
                    executorIds.remove("current");
                    executorIds.add(String.valueOf(form.getUserId()));
                }

                // режим жёсткого фильтра ТОЛЬКО текущий исполнитель
                if (filter.getValues().contains("current")) {
                    executorIds = Collections.singleton(String.valueOf(form.getUserId()));
                }

                boolean includeCreateUser = false;
                if (Utils.parseBoolean(filter.getConfigMap().get("includeCreateUser"))) {
                    includeCreateUser = true;
                }

                /*if( currentUserMode )
                {
                    executorIds = String.valueOf( form.getUserId() );
                }*/

                if (executorIds.size() > 0) {
                    if (executorIds.contains("empty")) {
                        //joinPart.append( "AS ie ON process.id=ie.process_id AND ie.user_id =''" );
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
            } else if (f instanceof FilterGrEx) {
                FilterGrEx filter = (FilterGrEx) f;

                //currentUserMode = Utils.parseBoolean( form.getParam( "currentUserMode" + filter.getRoleId() ) );

                String groupIds = Utils.toString(form.getSelectedValues("group" + filter.getRoleId()));
                if (Utils.isBlankString(groupIds) && filter.getOnEmptyValues().size() > 0) {
                    groupIds = Utils.toString(filter.getOnEmptyValues());
                }

                if (Utils.notBlankString(groupIds))//&& !currentUserMode )
                {
                    String tableAlias = "pg_" + filter.getRoleId();

                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(Tables.TABLE_PROCESS_GROUP);
                    joinPart.append("AS " + tableAlias + " ON process.id=" + tableAlias + ".process_id AND "
                            + tableAlias + ".group_id IN(");
                    joinPart.append(groupIds);
                    joinPart.append(") AND " + tableAlias + ".role_id=" + filter.getRoleId());
                }

                String executorIds = Utils.toString(form.getSelectedValuesStr("executor" + filter.getRoleId()))
                        .replace("current", String.valueOf(form.getUserId()));

                if (Utils.notBlankString(executorIds)) {
                    if (executorIds.contains("empty")) {
                        wherePart.append(" AND process.executors=''");
                    } else {
                        String tableAlias = "pe_" + filter.getRoleId();

                        joinPart.append(SQL_INNER_JOIN);
                        joinPart.append(Tables.TABLE_PROCESS_EXECUTOR);
                        joinPart.append("AS " + tableAlias + " ON process.id=" + tableAlias + ".process_id AND "
                                + tableAlias + ".user_id IN(");
                        joinPart.append(executorIds);
                        joinPart.append(") AND " + tableAlias + ".role_id=" + filter.getRoleId());
                    }
                }
            } else if (f instanceof FilterProcessType) {
                Filter filter = f;

                String typeIds = Utils.toString(form.getSelectedValues("type"));
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
                String values = Utils.toString(form.getSelectedValues("quarter"));
                if (Utils.notBlankString(values)) {

                    String alias = "param_list_" + paramId;

                    joinPart.append(SQL_INNER_JOIN);
                    joinPart.append(TABLE_PARAM_LIST);
                    joinPart.append("AS " + alias + " ON process.id=" + alias + ".id AND " + alias + ".param_id="
                            + paramId + " AND " + alias + ".value IN(" + values + ") ");
                }
            } else if (f instanceof FilterOpenClose) {
                String openCloseFilterValue = form.getParam("openClose");

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

                String values = Utils.toString(form.getSelectedValues("param" + paramId + "value"));
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
                joinPart.append(TABLE_PARAM_LIST);
                joinPart.append(
                        "AS param_list ON " + customerLinkAlias + ".object_id=param_list.id AND param_list.param_id="
                                + paramId + " AND param_list.value IN(" + values + ")");
            } else if (f instanceof FilterParam) {
                FilterParam filter = (FilterParam) f;

                Parameter parameter = filter.getParameter();
                int paramId = parameter.getId();
                String paramType = parameter.getType();

                if (Parameter.TYPE_ADDRESS.equals(paramType)) {
                    String city = form.getParam("param" + parameter.getId() + "valueCity");
                    String street = form.getParam("param" + parameter.getId() + "valueStreet");
                    String quarter = form.getParam("param" + parameter.getId() + "valueQuarter");
                    String houseAndFrac = form.getParam("param" + parameter.getId() + "valueHouse");
                    int flat = form.getParamInt("param" + parameter.getId() + "valueFlat", 0);
                    int streetId = form.getParamInt("param" + parameter.getId() + "valueStreetId", 0);
                    int houseId = form.getParamInt("param" + parameter.getId() + "valueHouseId", 0);
                    int quarterId = form.getParamInt("param" + parameter.getId() + "valueQuarterId", 0);
                    int cityId = form.getParamInt("param" + parameter.getId() + "valueCityId", 0);

                    String paramAlias = " paramAddress" + parameter.getId();
                    String cityAlias = paramAlias + "city";
                    String streetAlias = paramAlias + "street";
                    String houseAlias = paramAlias + "house";
                    String quarterAlias = paramAlias + "quarter";

                    if (cityId > 0 || flat > 0 || houseId > 0 || streetId > 0 || quarterId > 0 || 
                            Utils.notEmptyString(city) || Utils.notEmptyString(street) || Utils.notEmptyString(quarter) ||  Utils.notEmptyString(houseAndFrac)) {
                        joinPart.append(SQL_INNER_JOIN);
                        joinPart.append(ru.bgcrm.dao.Tables.TABLE_PARAM_ADDRESS);
                        joinPart.append(" AS " + paramAlias + " ON process.id=" + paramAlias + ".id AND " + paramAlias
                                + ".param_id=" + parameter.getId() + " ");

                        if (flat > 0) {
                            joinPart.append(SQL_AND);
                            joinPart.append(paramAlias + ".flat='" + flat + "' ");
                        }

                        if (houseId > 0) {
                            joinPart.append(SQL_AND);
                            joinPart.append(paramAlias + ".house_id=" + houseId + " ");
                        } else {
                            joinPart.append(SQL_INNER_JOIN);
                            joinPart.append(ru.bgcrm.dao.Tables.TABLE_ADDRESS_HOUSE);
                            joinPart.append(" AS " + houseAlias + " ON " + paramAlias + ".house_id=" + houseAlias + ".id ");

                            if (Utils.notEmptyString(houseAndFrac)) {
                                AddressHouse houseFrac = AddressHouse.extractHouseAndFrac(houseAndFrac);
                                if (houseFrac.getHouse() > 0) {
                                    joinPart.append(SQL_AND);
                                    joinPart.append(houseAlias + ".house=" + houseFrac.getHouse() + " ");
                                }
                                if (Utils.notEmptyString(houseFrac.getFrac())) {
                                    joinPart.append(SQL_AND);
                                    joinPart.append(houseAlias + ".frac='" + houseFrac.getFrac() + "' ");
                                }
                            }
                            
                            if (quarterId > 0) {
                                joinPart.append(SQL_INNER_JOIN);
                                joinPart.append(ru.bgcrm.dao.Tables.TABLE_ADDRESS_QUARTER);
                                joinPart.append(" AS " + quarterAlias + " ON " + houseAlias + ".quarter_id="
                                        + quarterAlias + ".id AND" + quarterAlias + ".id=" + quarterId);
                            } else if (Utils.notBlankString(quarter)) {
                                //TODO: Сделать по запросу.
                            }
                            
                            if (streetId > 0) {
                                joinPart.append(SQL_INNER_JOIN);
                                joinPart.append(ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET);
                                joinPart.append(" AS " + streetAlias + " ON " + houseAlias + ".street_id=" + streetAlias
                                        + ".id AND " + streetAlias + ".id=" + streetId);
                            } else if (Utils.notEmptyString(street)) {
                                joinPart.append(SQL_INNER_JOIN);
                                joinPart.append(ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET);
                                joinPart.append(" AS " + streetAlias + " ON " + houseAlias + ".street_id=" + streetAlias
                                        + ".id AND " + streetAlias + ".title LIKE '%" + street + "%' ");
                            }
                            
                            Runnable addStreetJoin = () -> {
                                // JOIN может быть уже добавлен фильтром по названию улицы 
                                if (!joinPart.toString().contains(streetAlias)) {
                                    joinPart.append(SQL_INNER_JOIN);
                                    joinPart.append(ru.bgcrm.dao.Tables.TABLE_ADDRESS_STREET);
                                    joinPart.append(" AS " + streetAlias + " ON " + houseAlias + ".street_id=" + streetAlias + ".id ");
                                }
                            };
                            
                            if (streetId <= 0 && quarterId <= 0) {
                                if (cityId > 0) {
                                    addStreetJoin.run();
                                    // добавка к фильтру по названию улицы
                                    joinPart.append(" AND " + streetAlias + ".city_id=" + cityId);
                                } else if (Utils.notBlankString(city)) {
                                    addStreetJoin.run();
                                    // добавка джойна города
                                    joinPart.append(SQL_INNER_JOIN);
                                    joinPart.append(ru.bgcrm.dao.Tables.TABLE_ADDRESS_CITY);
                                    joinPart.append(" AS " + cityAlias + " ON " + cityAlias + ".id=" + streetAlias + 
                                            ".city_id AND " + cityAlias + ".title LIKE '%" + city + "%' ");
                                }
                            }
                        }
                    }
                } else if (Parameter.TYPE_LIST.equals(paramType) || Parameter.TYPE_LISTCOUNT.equals(paramType)) {
                    String values = Utils.toString(form.getSelectedValues("param" + paramId + "value"));
                    if (Utils.isBlankString(values) && filter.getOnEmptyValues().size() > 0) {
                        values = Utils.toString(filter.getOnEmptyValues());
                    }
                    if (Utils.isBlankString(values)) {
                        continue;
                    }

                    String alias = "param_list_" + paramId;

                    joinPart.append(SQL_INNER_JOIN);
                    if (Parameter.TYPE_LIST.equals(paramType)) {
                        joinPart.append(TABLE_PARAM_LIST);
                    } else {
                        joinPart.append(TABLE_PARAM_LISTCOUNT);
                    }
                    joinPart.append("AS " + alias + " ON process.id=" + alias + ".id AND " + alias + ".param_id="
                            + paramId + " AND " + alias + ".value IN(" + values + ")");
                } else if (Parameter.TYPE_TEXT.equals(paramType) || Parameter.TYPE_BLOB.equals(paramType)) {
                    String mode = filter.getConfigMap().get("mode");
                    String value = form.getParam("param" + paramId + "value");

                    if (Utils.notBlankString(value)) {
                        joinPart.append(SQL_INNER_JOIN);

                        if (Parameter.TYPE_BLOB.equals(paramType)) {
                            joinPart.append(TABLE_PARAM_BLOB);
                        } else if (Parameter.TYPE_TEXT.equals(paramType)) {
                            joinPart.append(TABLE_PARAM_TEXT);
                        }

                        if ("regexp".equals(mode)) {
                            joinPart.append("AS param_text ON process.id=param_text.id AND param_text.param_id="
                                    + paramId + " AND param_text.value RLIKE '" + value + "'");
                        }
                        if ("numeric".equals(mode)) {
                            joinPart.append("AS param_text ON process.id=param_text.id AND param_text.param_id="
                                    + paramId + " AND ( 1>1 ");
                            for (String val : value.split(",")) {
                                if (val.contains("-")) {
                                    String[] bVal = val.split("-");
                                    joinPart.append(
                                            " OR param_text.value between '" + bVal[0] + "' AND '" + bVal[1] + "'");
                                } else {
                                    joinPart.append(" OR param_text.value = '" + val + "'");
                                }
                            }
                            joinPart.append(" )");
                        } else {
                            joinPart.append(" AS param_text ON process.id=param_text.id AND param_text.param_id="
                                    + paramId + " AND param_text.value LIKE '" + getLikePattern(value, "subs") + "'");
                        }
                    }
                } else if (Parameter.TYPE_DATE.equals(paramType) || Parameter.TYPE_DATETIME.equals(paramType)) {
                    String paramAlias = "param_" + paramId;

                    if (!joinPart.toString().contains(paramAlias)) {
                        joinPart.append(" LEFT JOIN param_" + paramType);
                        joinPart.append(" AS " + paramAlias + " ON " + paramAlias + ".id=process.id AND " + paramAlias
                                + ".param_id=" + paramId);
                    }
                    addDateTimeFilter(form, wherePart, "dateTimeParam" + paramId, String.valueOf(paramId), filter);
                }
            } else if ("status".equals(type)) {
                Filter filter = f;

                String statusIds = Utils.toString(form.getSelectedValues("status"));
                if (Utils.isBlankString(statusIds) && filter.getOnEmptyValues().size() > 0) {
                    statusIds = Utils.toString(filter.getOnEmptyValues());
                }
                if (filter.getValues().size() > 0) {
                    statusIds = Utils.toString(filter.getValues());
                }

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
                            + getLikePattern(Customer.OBJECT_TYPE, "start") + "'");
                    joinPart.append(SQL_LEFT_JOIN);
                    joinPart.append(TABLE_CUSTOMER);
                    joinPart.append(" AS " + customerAlias + " ON " + linkedCustomerAlias + ".object_id="
                            + customerAlias + ".id ");

                    wherePart.append(SQL_AND);
                    wherePart.append(" " + customerAlias + ".title LIKE '%" + customerTitle + "%' ");
                }
            } else if ("linkedObject".equals(type)) {
                ParameterMap configMap = f.getConfigMap();
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

        /* заблокированы, причину см. Queue.extractFiltersAndSorts
        if( filterSet.createUserFilter != null )
        {
            if( filterSet.createUserFilter.getValues().contains( "current" ) )
            {
                currentUserMode = true;
            }
        
            String creatorsIds = Utils.toString( form.getSelectedValues( "creator" ) );
            if( Utils.isBlankString( creatorsIds ) && filterSet.createUserFilter.getOnEmptyValues().size() > 0 )
            {
                creatorsIds = Utils.toString( filterSet.createUserFilter.getOnEmptyValues() );
            }
        
            if( currentUserMode )
            {
                creatorsIds = String.valueOf( form.getUserId() );
            }
            if( Utils.notBlankString( creatorsIds ) )
            {
                wherePart.append( " AND process.create_user_id IN ( " );
                wherePart.append( creatorsIds );
                wherePart.append( " ) " );
            }
        }
        
        if( filterSet.statusUserFilter != null )
        {
            FilterStatusUser filterStatus = (FilterStatusUser)filterSet.statusUserFilter;
        
            if( filterSet.statusUserFilter.getValues().contains( "current" ) )
            {
                currentUserMode = true;
            }
        
            String changesIds = Utils.toString( form.getSelectedValues( "changer" ) );
            if( Utils.isBlankString( changesIds ) && filterSet.statusUserFilter.getOnEmptyValues().size() > 0 )
            {
                changesIds = Utils.toString( filterSet.statusUserFilter.getOnEmptyValues() );
            }
            if( currentUserMode )
            {
                changesIds = String.valueOf( form.getUserId() );
            }
            if( Utils.notBlankString( changesIds ) )
            {
                wherePart.append( " AND process.status_user_id IN ( " );
                wherePart.append( changesIds );
                wherePart.append( " ) AND process.status_id=" );
                wherePart.append( filterStatus.getStatusId() );
            }
        }
        
        if( filterSet.closeUserFilter != null )
        {
            if( filterSet.closeUserFilter.getValues().contains( "current" ) )
            {
                currentUserMode = true;
            }
        
            String closersIds = Utils.toString( form.getSelectedValues( "closer" ) );
            if( Utils.isBlankString( closersIds ) && filterSet.closeUserFilter.getOnEmptyValues().size() > 0 )
            {
                closersIds = Utils.toString( filterSet.closeUserFilter.getOnEmptyValues() );
            }
            if( currentUserMode )
            {
                closersIds = String.valueOf( form.getUserId() );
            }
            if( Utils.notBlankString( closersIds ) )
            {
                wherePart.append( " AND process.close_user_id IN ( " );
                wherePart.append( closersIds );
                wherePart.append( " ) " );
            }
        }*/
    }

    public void addDateTimeFilter(DynActionForm form, StringBuilder wherePart, String paramPrefix, String paramId,
            FilterParam filter) {
        boolean orEmpty = filter.getConfigMap().getBoolean("orEmpty", false);

        Date dateFrom = TimeUtils.parse(form.getParam(paramPrefix + "From"), TimeUtils.FORMAT_TYPE_YMD);
        Date dateTo = TimeUtils.parse(form.getParam(paramPrefix + "To"), TimeUtils.FORMAT_TYPE_YMD);

        if (filter.getConfigMap().get("valueFrom", "").equals("curdate"))
            dateFrom = new Date();
        if (filter.getConfigMap().get("valueTo", "").equals("curdate"))
            dateTo = new Date();

        String tableAlias = "param_" + paramId;

        if (orEmpty) {
            wherePart.append(" AND (  " + tableAlias + ".param_id" + " IS NULL OR ( 1>0 ");
        }

        /*if( dateFrom != null || dateTo != null )
        {
            wherePart.append( " AND " + tableAlias + ".param_id" + "=" + paramId );
        }*/
        if (dateFrom != null) {
            wherePart.append(" AND " + tableAlias + ".value" + ">=" + TimeUtils.formatSqlDate(dateFrom));
        }
        if (dateTo != null) {
            wherePart.append(
                    " AND " + tableAlias + ".value" + "<" + TimeUtils.formatSqlDate(TimeUtils.getNextDay(dateTo)));
        }

        if (orEmpty) {
            wherePart.append(" ) ) ");
        }
    }

    public void addDateFilter(DynActionForm form, StringBuilder wherePart, String paramPrefix, String column) {
        Date dateFrom = TimeUtils.parse(form.getParam(paramPrefix + "From"), TimeUtils.FORMAT_TYPE_YMD);
        Date dateTo = TimeUtils.parse(form.getParam(paramPrefix + "To"), TimeUtils.FORMAT_TYPE_YMD);
        if (dateFrom != null) {
            wherePart.append(" AND process." + column + ">=" + TimeUtils.formatSqlDate(dateFrom));
        }
        if (dateTo != null) {
            wherePart.append(" AND process." + column + "<" + TimeUtils.formatSqlDate(TimeUtils.getNextDay(dateTo)));
        }
    }

    private Pair<String, String> getModifiers(ParameterMap col) {
        String openTag = "", closeTag = "";
        String type = col.get("convert");
        if ("int".equals(type)) {
            openTag = " CAST((";
            closeTag = ") AS UNSIGNED) ";
        }
        return new Pair<String, String>(openTag, closeTag);
    }

    // Возвращает наличие агрегатной функции.
    private boolean addColumnList(Queue queue, StringBuilder selectPart, StringBuilder joinPart, boolean aggregate)
            throws Exception {
        StringBuilder selectPartBuffer = new StringBuilder(60);

        boolean aggregateFunctions = false;
        for (ParameterMap col : queue.getColumnList()) {
            String aggregateFunction = col.get("aggregate");
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

                    addColumn(col, selectPartBuffer, joinPart);

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
                addColumn(col, selectPart, joinPart);
            }
        }
        return aggregateFunctions;
    }

    private void addColumn(ParameterMap col, StringBuilder selectPart, StringBuilder joinPart) throws Exception {
        String value = col.get("value");
        if (value == null) {
            throw new BGException(".value not defined, column: " + col);
        }

        // тут может быть "linked"
        String target = col.get("process", "process");

        if (LINKED_PROCESS.equals(target) && joinPart.indexOf(LINKED_PROCESS_JOIN) < 0) {
            joinPart.append(LINKED_PROCESS_JOIN);
        }

        Pair<String, String> modificator = getModifiers(col);
        selectPart.append(modificator.getFirst());

        if ("check".equals(value) || "id".equals(value)) {
            selectPart.append(target + ".id ");
        } else if ("type_title".equals(value)) {
            String alias = "type_" + target;

            selectPart.append(alias + ".title ");
            joinPart.append(" LEFT JOIN " + TABLE_PROCESS_TYPE + " AS " + alias + " ON " + target + ".type_id=" + alias
                    + ".id");
        } else if (value.startsWith("status_")) {
            String alias = addProcessStatusJoin(target, joinPart);

            if ("status_title".equals(value)) {
                String aliasPst = "process_status_title_" + target;

                selectPart.append(aliasPst + ".title ");
                joinPart.append(" LEFT JOIN process_status_title AS " + aliasPst + " ON " + target + ".status_id="
                        + aliasPst + ".id");
            } else if (value.startsWith("status_dt")) {
                addDateTimeParam(selectPart, alias + ".dt", value);
            } else if ("status_user".equals(value)) {
                String aliasPsu = "status_user_" + target;

                selectPart.append(aliasPsu + ".title ");
                joinPart.append(" LEFT JOIN user AS " + aliasPsu + " ON " + aliasPsu + ".id=" + alias + ".user_id ");
            } else if ("status_comment".equals(value)) {
                selectPart.append(alias + ".comment");
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
        } else if (value.startsWith("text_param") || value.startsWith("param")) {
            ParamValueDAO.paramSelectQuery(value, target + ".id", selectPart, joinPart, false);
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
                    joinPart.append(" LEFT JOIN " + TABLE_PARAM_LIST + " AS " + alias + " ON " + target + ".id= "
                            + alias + ".id AND " + alias + ".param_id=" + paramId + " AND " + alias + ".value="
                            + paramValue + " ");
                }
            } else {
                log.warn("Неверное условие: " + value);
                return;
            }
        } else if (value.startsWith("linkCustomerLink") || value.startsWith("linkedCustomerLink")) {
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
        } else if (value.startsWith("linkCustomer:") || value.startsWith("linkedCustomer:")) {
            // TODO: тудудуду
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

                    ParamValueDAO.paramSelectQuery("param:" + paramId, "linked_customer.object_id", selectPart,
                            joinPart, false);
                }
            }
        } else if (value.startsWith("linkObject:") || value.startsWith("linkedObject:")) {
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
                //selectPart.append( " DATE_FORMAT( " + tableAlias + ".dt, '%d.%m.%y %T' ) " );
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
        } else {
            log.error("Incorrect column value macros: " + value);
            return;
        }

        selectPart.append(modificator.getSecond());
        selectPart.append(", ");
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

    public Process getProcess(int id) throws BGException {
        try {
            Process result = null;

            String query = "SELECT process.*, ps.* FROM " + TABLE_PROCESS + " AS process " 
                    + "LEFT JOIN " + TABLE_PROCESS_STATUS
                    + " AS ps ON process.id=ps.process_id AND ps.status_id=process.status_id AND ps.last "
                    + getIsolationJoin(user)
                    + " WHERE process.id=?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = getProcessFromRs(rs);
                result.setStatusChange(StatusChangeDAO.getProcessStatusFromRs(rs, "ps."));
            }
            ps.close();

            return result;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public List<Process> getProcessList(Collection<Integer> processIds) throws BGException {
        List<Process> processList = new ArrayList<Process>();
        try {
            String query = "SELECT process.* FROM " + TABLE_PROCESS + " AS process " + "WHERE process.id IN ( "
                    + Utils.toString(processIds) + ")";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                processList.add(getProcessFromRs(rs));
            }
            ps.close();

            return processList;
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    public void updateProcessGroups(Set<ProcessGroup> processGroups, int processId) throws Exception {
        if (history) {
            Process oldValue = new ProcessDAO(con).getProcess(processId);
            Process newValue = oldValue.clone();
            newValue.setProcessGroups(processGroups);
            logProcessChange(newValue, oldValue);
        }
        
        updateColumn(TABLE_PROCESS, processId, "groups", ProcessGroup.serialize(processGroups));
        
        String query = SQL_DELETE + Tables.TABLE_PROCESS_GROUP + " WHERE process_id=?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, processId);
        ps.executeUpdate();
        ps.close();

        query = SQL_INSERT + Tables.TABLE_PROCESS_GROUP + "VALUES (?, ?, ?)";
        ps = con.prepareStatement(query);
        ps.setInt(1, processId);

        for (ProcessGroup item : processGroups) {
            ps.setInt(2, item.getGroupId());
            ps.setInt(3, item.getRoleId());
            ps.executeUpdate();
        }

        ps.close();
    }

    /**
     * Устаревшая функция - исполнители устанавливаются без привязки к группам,
     * привязка восстанавливается затем по членству пользователей в группах.
     * Использовать {@link #updateProcessExecutors(Set, int)}.
     */
    @Deprecated
    public void updateProcessExecutors(int processId, Set<Integer> executorIds) throws BGException {
        if (history) {
            String executorString = "";

            for (Integer item : executorIds) {
                executorString += UserCache.getUser(item).getTitle() + ", ";
            }

            if (executorString.length() > 2) {
                executorString = executorString.substring(0, executorString.length() - 2);
            }

            logProcessChange(processId, "Исполнители: [" + executorString + "]");
        }

        updateColumn(TABLE_PROCESS, processId, "executors", Utils.toString(executorIds));
        updateIds(Tables.TABLE_PROCESS_EXECUTOR, "process_id", "user_id", processId, executorIds);
    }

    public void updateProcessExecutors(Set<ProcessExecutor> processExecutors, int processId) throws BGException {
        if (history) {
            Process oldValue = new ProcessDAO(con).getProcess(processId);
            Process newValue = oldValue.clone();
            newValue.setProcessExecutors(processExecutors);
            logProcessChange(newValue, oldValue);
        }

        updateColumn(TABLE_PROCESS, processId, "executors", ProcessExecutor.serialize(processExecutors));

        try {
            String query = "DELETE FROM " + Tables.TABLE_PROCESS_EXECUTOR + " WHERE process_id=?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, processId);
            ps.executeUpdate();
            ps.close();

            query = "INSERT INTO " + Tables.TABLE_PROCESS_EXECUTOR
                    + " ( process_id, group_id, role_id, user_id ) VALUES ( ?, ?, ?, ? ) ";
            ps = con.prepareStatement(query);
            ps.setInt(1, processId);

            for (ProcessExecutor processExecutor : processExecutors) {
                ps.setInt(2, processExecutor.getGroupId());
                ps.setInt(3, processExecutor.getRoleId());
                ps.setInt(4, processExecutor.getUserId());
                ps.executeUpdate();
            }

            ps.close();
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    private void logProcessChange(Process process, Process oldProcess) throws BGException {
        String changes = process.getChangesLog(oldProcess);
        if (changes.length() > 0) {
            logProcessChange(process.getId(), changes.toString());
        }
    }

    private void logProcessChange(int processId, String log) throws BGException {
        new EntityLogDAO(this.con, Tables.TABLE_PROCESS_LOG).insertEntityLog(processId, userId, log);
    }

    public void updateProcess(Process process) throws BGException {
        if (process != null) {
            try {
                Process oldProcess = getProcess(process.getId());
                if (history) {
                    if (oldProcess != null && !oldProcess.isEqualProperties(process)) {
                        logProcessChange(process, oldProcess);
                    }
                }

                int index = 1;
                PreparedStatement ps = null;
                StringBuilder query = new StringBuilder();
                // раньше была прроверка на положительный ID, но он может быть отрицательным в случае, если процесс временный
                if (oldProcess != null) {
                    query.append("UPDATE " + TABLE_PROCESS
                            + " SET status_id=?, status_dt=?, status_user_id=?, description=?, close_dt=?, priority=?, close_user_id=?, type_id=? WHERE id=?");
                    ps = con.prepareStatement(query.toString());
                    ps.setInt(index++, process.getStatusId());
                    ps.setTimestamp(index++, TimeUtils.convertDateToTimestamp(process.getStatusTime()));
                    ps.setInt(index++, process.getStatusUserId());
                    ps.setString(index++, process.getDescription());
                    ps.setTimestamp(index++, TimeUtils.convertDateToTimestamp(process.getCloseTime()));
                    ps.setInt(index++, process.getPriority());
                    ps.setInt(index++, process.getCloseUserId());
                    ps.setInt(index++, process.getTypeId());
                    //ps.setTimestamp( index++, TimeUtils.convertDateToTimestamp( process.getLastMessageTime() ) );
                    ps.setInt(index++, process.getId());
                    ps.executeUpdate();

                } else {
                    query.append("INSERT INTO " + TABLE_PROCESS
                            + " SET type_id=?, status_id=?, status_user_id=?, status_dt=NOW(), description=?, title=?, create_dt=NOW(), executors=?, create_user_id=?");
                    ps = con.prepareStatement(query.toString(), PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setInt(index++, process.getTypeId());
                    ps.setInt(index++, process.getStatusId());
                    ps.setInt(index++, process.getStatusUserId());
                    ps.setString(index++, process.getDescription());
                    ps.setString(index++, process.getTitle());
                    ps.setString(index++, ProcessExecutor.serialize(process.getProcessExecutors()));
                    ps.setInt(index++, process.getCreateUserId());
                    ps.executeUpdate();
                    process.setId(lastInsertId(ps));
                }
                ps.close();
            } catch (SQLException e) {
                throw new BGException(e);
            }
        }
    }

    public void deleteProcess(int processId) throws BGException {
        try {
            deleteProcessData(processId, "DELETE FROM " + TABLE_PROCESS + " WHERE id=?");
            deleteProcessData(processId, "DELETE FROM " + TABLE_PROCESS_GROUP + " WHERE process_id=?");
            deleteProcessData(processId, "DELETE FROM " + TABLE_PROCESS_EXECUTOR + " WHERE process_id=?");

            deleteProcessData(processId, "DELETE FROM " + TABLE_PROCESS_LINK + " WHERE process_id=?");
            deleteProcessData(processId,
                    "DELETE FROM " + TABLE_PROCESS_LINK + " WHERE object_id=? AND object_type LIKE 'process%'");

            new ParamValueDAO(con).deleteParams(Process.OBJECT_TYPE, processId);

            new EntityLogDAO(this.con, Tables.TABLE_PROCESS_LOG).deleteHistory(processId);
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    private void deleteProcessData(int processId, String query) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, processId);
        ps.executeUpdate();
        ps.close();
    }

    public void processIdInvert(Process process) throws BGException {
        try {
            int currentProcessId = process.getId();

            updateProcessId(currentProcessId, "UPDATE " + TABLE_PROCESS + " SET id=? WHERE id=?");
            updateProcessId(currentProcessId, "UPDATE " + TABLE_PROCESS_GROUP + " SET process_id=? WHERE process_id=?");
            updateProcessId(currentProcessId,
                    "UPDATE " + TABLE_PROCESS_EXECUTOR + " SET process_id=? WHERE process_id=?");

            updateProcessId(currentProcessId, "UPDATE " + TABLE_PROCESS_LINK + " SET process_id=? WHERE process_id=?");
            updateProcessId(currentProcessId, "UPDATE " + TABLE_PROCESS_LINK
                    + " SET object_id=? WHERE object_id=? AND object_type LIKE 'process%'");

            new ParamValueDAO(con).objectIdInvert(Process.OBJECT_TYPE, currentProcessId);

            process.setId(-currentProcessId);
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    private void updateProcessId(int currentProcessId, String query) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, -currentProcessId);
        ps.setInt(2, currentProcessId);
        ps.executeUpdate();
        ps.close();
    }

    /**
     * Выбирает процессы по адресному параметру.
     * @param searchResult
     * @param addressParamIdList
     * @param houseId код дома
     * @param houseFlat квартира
     * @param houseRoom комната
     * @throws SQLException
     */
    public void searchProcessListByAddress(SearchResult<ParameterSearchedObject<Process>> searchResult,
            Set<Integer> typeIds, Set<Integer> addressParamIdList, int houseId, String houseFlat, String houseRoom)
                    throws BGException {
        try {
            if (searchResult != null) {
                Page page = searchResult.getPage();
                List<ParameterSearchedObject<Process>> list = searchResult.getList();

                PreparedDelay ps = new PreparedDelay(con);
                String ids = Utils.toString(addressParamIdList);

                ps.addQuery(SQL_SELECT_COUNT_ROWS);
                ps.addQuery("DISTINCT param.param_id, param.value, process.*, type.title, status.title ");
                ps.addQuery(SQL_FROM);
                ps.addQuery(TABLE_PROCESS);
                ps.addQuery("AS process");

                ps.addQuery(SQL_INNER_JOIN);
                ps.addQuery(TABLE_PARAM_ADDRESS);
                ps.addQuery("AS param ON c.id=param.id AND param.param_id IN (");
                ps.addQuery(ids);
                ps.addQuery(")");

                ps.addQuery(" AND param.house_id=?");
                ps.addInt(houseId);

                if (Utils.notBlankString(houseFlat)) {
                    ps.addQuery(" AND param.flat=?");
                    ps.addString(houseFlat);
                }
                if (Utils.notBlankString(houseRoom)) {
                    ps.addQuery(" AND param.room=?");
                    ps.addString(houseRoom);
                }

                ps.addQuery(" LEFT JOIN " + TABLE_PROCESS_TYPE + " AS type ON process.type_id=type.id ");
                ps.addQuery(
                        " LEFT JOIN " + TABLE_PROCESS_STATUS_TITLE + " AS status ON status.id = process.status_id ");

                ps.addQuery(" WHERE 1>0 ");
                if (typeIds != null && typeIds.size() > 0) {
                    ps.addQuery(" AND process.type_id IN ");
                    ps.addQuery(Utils.toString(typeIds));
                    ps.addQuery(" )");
                }

                ps.addQuery(SQL_ORDER_BY);
                ps.addQuery("p.create_dt");
                ps.addQuery(getPageLimit(page));

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(new ParameterSearchedObject<>(getProcessFromRs(rs), rs.getInt(1), rs.getString(2)));
                }

                setRecordCount(page, ps.getPrepared());
                ps.close();
            }
        } catch (SQLException e) {
            throw new BGException(e);
        }
    }

    /**
     * Выбирает процессы возможно привязанные к сообщению.
     * @param searchResult
     * @param from
     * @throws SQLException
     */
    public void searchProcessListForMessage(SearchResult<Process> searchResult, String from, List<CommonObjectLink> links, Boolean open)
            throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Process> list = searchResult.getList();

            PreparedDelay pd = new PreparedDelay(con);

            pd.addQuery(SQL_SELECT_COUNT_ROWS);
            pd.addQuery("DISTINCT p.*");
            pd.addQuery(SQL_FROM);
            pd.addQuery(TABLE_PROCESS);
            pd.addQuery("AS p ");
            pd.addQuery(SQL_INNER_JOIN);
            pd.addQuery(TABLE_MESSAGE);
            pd.addQuery("AS m ON m.process_id=p.id AND m.from=?");
            pd.addString(from);
            addOpenFilter(pd, open);

            if (CollectionUtils.isNotEmpty(links)) {
                Set<Integer> objectIds = new HashSet<Integer>();
                StringBuilder objectFilter = new StringBuilder();

                objectFilter.append("(0>1 ");

                for (CommonObjectLink link : links) {
                    objectIds.add(link.getLinkedObjectId());
                    if (Customer.OBJECT_TYPE.equals(link.getLinkedObjectType())) {
                        objectFilter.append(" OR (pl.object_type LIKE 'customer%' AND pl.object_id="
                                + link.getLinkedObjectId() + ")");
                    } else {
                        objectFilter.append(" OR (pl.object_type='" + link.getLinkedObjectType() + "' AND pl.object_id="
                                + link.getLinkedObjectId() + ")");
                    }
                }

                objectFilter.append(" ) ");

                pd.addQuery("UNION SELECT DISTINCT p.*");
                pd.addQuery(SQL_FROM);
                pd.addQuery(TABLE_PROCESS);
                pd.addQuery("AS p ");
                pd.addQuery(SQL_INNER_JOIN);
                pd.addQuery(TABLE_PROCESS_LINK);
                pd.addQuery("AS pl ON pl.process_id=p.id AND pl.object_id IN (" + Utils.toString(objectIds) + ") AND ");
                pd.addQuery(objectFilter.toString());

                addOpenFilter(pd, open);
            }

            pd.addQuery(SQL_ORDER_BY);
            pd.addQuery("create_dt DESC");

            pd.addQuery(getPageLimit(page));

            ResultSet rs = pd.executeQuery();
            while (rs.next())
                list.add(getProcessFromRs(rs, ""));

            setRecordCount(page, pd.getPrepared());
            pd.close();
        }
    }

    /**
     * Выбирает процессы, с пользователем в исполнителях.
     * @param searchResult 
     * @throws BGException
     */
    public void searchProcessListForUser(SearchResult<Process> searchResult, int userId, Boolean open)
            throws BGException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Process> list = searchResult.getList();

            PreparedDelay pd = new PreparedDelay(con);

            pd.addQuery(SQL_SELECT_COUNT_ROWS);
            pd.addQuery("DISTINCT p.*");
            pd.addQuery(SQL_FROM);
            pd.addQuery(TABLE_PROCESS);
            pd.addQuery("AS p ");
            pd.addQuery(SQL_INNER_JOIN);
            pd.addQuery(TABLE_PROCESS_EXECUTOR);
            pd.addQuery("AS e ON e.process_id=p.id AND e.user_id=?");
            pd.addInt(userId);
            addOpenFilter(pd, open);
            pd.addQuery(SQL_ORDER_BY);
            pd.addQuery("create_dt DESC");

            pd.addQuery(getPageLimit(page));

            try {
                ResultSet rs = pd.executeQuery();
                while (rs.next())
                    list.add(getProcessFromRs(rs, ""));
                setRecordCount(page, pd.getPrepared());
                pd.close();
            } catch (SQLException ex) {
                throw new BGException(ex);
            }
        }
    }
    
    public static final int MODE_USER_CREATED = 1;
    public static final int MODE_USER_CLOSED = 2;
    public static final int MODE_USER_STATUS_CHANGED = 3;
    
    /**
     * Выбирает связанные с процессом процессы.
     * @param searchResult
     * @param userId код пользователя.
     * @param mode принимает значения {@link #MODE_USER_CREATED}, {@link #MODE_USER_CLOSED}, {@link #MODE_USER_STATUS_CHANGED}.
     * @throws SQLException
     */
    public void searchProcessListForUser(SearchResult<Process> searchResult, int userId, int mode) throws SQLException {
        if (searchResult != null) {
            Page page = searchResult.getPage();
            List<Process> list = searchResult.getList();

            PreparedDelay pd = new PreparedDelay(con);

            pd.addQuery(SQL_SELECT_COUNT_ROWS);
            pd.addQuery("*");
            pd.addQuery(SQL_FROM);
            pd.addQuery(TABLE_PROCESS);

            pd.addQuery(getIsolationJoin(user));

            if (mode == MODE_USER_CREATED) {
                pd.addQuery("WHERE create_user_id=?");
                pd.addInt(userId);
                pd.addQuery(" AND close_dt is NULL");
                pd.addQuery(SQL_ORDER_BY);
                pd.addQuery("create_dt DESC");
            } else if (mode == MODE_USER_CLOSED) {
                pd.addQuery("WHERE close_user_id=?");
                pd.addInt(userId);
                pd.addQuery(SQL_ORDER_BY);
                pd.addQuery("close_dt DESC");
            } else if (mode == MODE_USER_STATUS_CHANGED) {
                pd.addQuery("WHERE status_user_id=?");
                pd.addInt(userId);
                pd.addQuery(SQL_ORDER_BY);
                pd.addQuery("status_dt DESC");
            }

            pd.addQuery(getPageLimit(page));

            ResultSet rs = pd.executeQuery();
            while (rs.next())
                list.add(getProcessFromRs(rs, ""));

            setRecordCount(page, pd.getPrepared());
            pd.close();
        }
    }

    private void addOpenFilter(PreparedDelay pd, Boolean open) {
        if (open != null) {
            if (open) {
                pd.addQuery(" WHERE close_dt IS NULL ");
            } else {
                pd.addQuery(" WHERE close_dt IS NOT NULL ");
            }
        }
    }

    /**
     * Возвращает последнюю запись лога изменения процесса.
     * @param process
     * @return
     * @throws BGException
     */
    public EntityLogItem getLastProcessChangeLog(Process process) throws BGException {
        SearchResult<EntityLogItem> logItems = new SearchResult<EntityLogItem>();
        logItems.getPage().setPageIndex( 1 );
        logItems.getPage().setPageSize( 1 );
    
        new ProcessDAO( con ).searchProcessLog( ProcessAction.getProcessType( process.getTypeId() ), process.getId(), logItems );
        
        return Utils.getFirst(logItems.getList());
    }
    
    /**
     * Выборка логов изменения процесса.
     * @param processType
     * @param processId
     * @param result
     * @throws BGException
     */
    public void searchProcessLog(ProcessType processType, int processId, SearchResult<EntityLogItem> result)
            throws BGException {
        PreparedDelay pd = new PreparedDelay(con);

        Page page = result.getPage();
        /*Если не кастить в каждом запросе поле, то с кордировкой какая-то лажа получается, сокрее всего это из-за
         *  того что разные типы в одном поле смешиваются. На старой версии mysql 5.0.x не работало( на новой - не известно, вроде в 
         *  maria 5.5 как-будто работает). 
         */

        pd.addQuery(SQL_SELECT_COUNT_ROWS + " dt , user_id, 0 , CAST( data AS CHAR), 0 FROM " + TABLE_PROCESS_LOG);
        pd.addQuery(" WHERE id= ? ");
        pd.addInt(processId);

        pd.addQuery(" UNION SELECT dt, user_id, -1, CAST(status_id AS CHAR) , comment FROM " + TABLE_PROCESS_STATUS);
        pd.addQuery(" WHERE process_id=? ");
        pd.addInt(processId);

        pd.addQuery(" UNION SELECT dt, user_id, param_id, CAST(text AS CHAR), 0 FROM " + TABLE_PARAM_LOG);
        pd.addQuery(" WHERE object_id=? AND param_id IN  ( "
                + Utils.toString(processType.getProperties().getParameterIds(), " 0 ", " , ") + " ) ");
        pd.addInt(processId);

        pd.addQuery(" ORDER BY dt DESC ");
        pd.addQuery(getPageLimit(page));

        List<EntityLogItem> list = result.getList();

        try {
            ResultSet rs = pd.executeQuery();
            while (rs.next()) {
                String text = " ??? ";
                int paramId = rs.getInt(3);
                // параметр
                if (paramId > 0) {
                    Parameter param = ParameterCache.getParameter(paramId);
                    if (param != null) {
                        text = " Параметр: ' " + param.getTitle() + " ' : " + rs.getString(4);
                    }
                }
                // статус
                else if (paramId == -1) {
                    Status status = ProcessTypeCache.getStatusMap().get(Utils.parseInt(rs.getString(4)));
                    text = "Статус: " + (status != null ? status.getTitle() : " ??? " + rs.getString(4) + " . ") + " ["
                            + Utils.maskNull(rs.getString(5)) + "]";
                }
                // процесс
                else {
                    text = rs.getString(4);
                }
                list.add(new EntityLogItem(TimeUtils.convertTimestampToDate(rs.getTimestamp(1)), processId,
                        rs.getInt(2), text));
            }
            setRecordCount(page, pd.getPrepared());
            pd.close();
        } catch (SQLException ex) {
            throw new BGException(ex);
        }
    }
}
