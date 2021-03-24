package ru.bgcrm.model.process;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.ProcessLinkDAO;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.process.queue.Action;
import ru.bgcrm.model.process.queue.Filter;
import ru.bgcrm.model.process.queue.FilterCustomerParam;
import ru.bgcrm.model.process.queue.FilterGrEx;
import ru.bgcrm.model.process.queue.FilterLinkObject;
import ru.bgcrm.model.process.queue.FilterList;
import ru.bgcrm.model.process.queue.FilterOpenClose;
import ru.bgcrm.model.process.queue.FilterParam;
import ru.bgcrm.model.process.queue.FilterProcessType;
import ru.bgcrm.model.process.queue.Processor;
import ru.bgcrm.model.process.queue.SortMode;
import ru.bgcrm.model.process.queue.SortSet;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Preferences;
import ru.bgcrm.util.Utils;
import ru.bgerp.util.Log;

// TODO: Use IdTitleConfig
public class Queue extends IdTitle {
    private static final Log log = Log.getLog();

    public static final String MEDIA_HTML = "html";
    public static final String MEDIA_HTML_OPEN = "html.open";
    public static final String MEDIA_PRINT = "print";
    public static final String MEDIA_XLS = "xls";

    private int id;
    private String title;
    private String config;
    private Set<Integer> processTypeIds = new HashSet<Integer>();
    private ParameterMap configMap;
    private List<ParameterMap> columnList;
    private SortedMap<Integer, ParameterMap> columnMap;
    private SortSet sortSet = new SortSet();

    private final FilterList filterList = new FilterList();

    private final Map<Integer, Processor> processorMap = new HashMap<Integer, Processor>();
    private final List<Action> actionList = new ArrayList<Action>();
    private final List<IdTitle> createAllowedProcessList = new ArrayList<IdTitle>();

    private LastModify lastModify = new LastModify();

    public Queue() {}

    public Queue(int id, String title) {
        super(id, title);
    }

    public String getConfig() {
        return config;
    }

    private List<String> getMediaColumns(String media) {
        ParameterMap configMap = getConfigMap();

        List<String> result = Utils.toList(configMap.get("media." + media + ".columns"));

        // fallback for media 'html' only
        if (result.isEmpty() && MEDIA_HTML.equals(media)) {
            for (Map.Entry<Integer, ParameterMap> me : columnMap.entrySet()) {
                if (me.getValue().getBoolean("show", true)) {
                    result.add(String.valueOf(me.getKey()));
                }
            }
        }

        return result;
    }

    public Queue clone() {
        Queue result = new Queue();

        result.processTypeIds = processTypeIds;
        result.configMap = configMap;
        result.columnList = columnList;
        result.columnMap = columnMap;
        result.sortSet = sortSet;

        return result;
    }

    // столбец указанный в конфигурации
    public static class ColumnRef {
        private final int columnId;
        private final ParameterMap columnConf;
        public int rawDataIndex;

        public ColumnRef(int columnId, ParameterMap columnConf) {
            this.columnId = columnId;
            this.columnConf = columnConf;
        }

        public Process getProcess(Process[] processArray) {
            // тут может быть "linked"
            String target = columnConf.get("process", "process");
            if (target.equals(ProcessDAO.LINKED_PROCESS)) {
                return processArray[1];
            }
            return processArray[0];
        }
    }

    /**
     * Для возможности склеивания в одну колонку нескольких значений.
     * TODO: Несколько избыточно, для HTML есть для этого более гибкое задание в JEXL.
     */
    public static class ColumnConf {
        private ColumnRef firstColumn;
        private List<ColumnRef> refList = new ArrayList<ColumnRef>(1);

        public void addColumnRef(ColumnRef ref) {
            refList.add(ref);
            firstColumn = Utils.getFirst(refList);
        }

        public int getColumnId() {
            return firstColumn.columnId;
        }

        public ParameterMap getColumnConf() {
            return firstColumn.columnConf;
        }

        public ColumnRef getFirstColumn() {
            return firstColumn;
        }

        public String getTitle() {
            String title = Utils.getFirst(refList).columnConf.get("title");

            String value = Utils.getFirst(refList).columnConf.get("value");
            if (value.startsWith("isListParam")) {
                String[] parts = value.split(":");

                int paramId = Utils.parseInt(parts[1]);
                int paramValue = Utils.parseInt(parts[2]);

                for (IdTitle listValue : ParameterCache.getListParamValues(ParameterCache.getParameter(paramId))) {
                    if (paramValue == listValue.getId()) {
                        title = listValue.getTitle();
                        break;
                    }
                }
            }

            return title;
        }

        public Object getValue(DynActionForm form, boolean isHtmlMedia, Object[] rawData) throws SQLException {
            Object data = null;

            for (ColumnRef ref : refList) {
                Object dataAdd = decryptDir(form, (Process[]) rawData[0], isHtmlMedia, ref, rawData[ref.rawDataIndex]);
                if (data instanceof String && (dataAdd == null || dataAdd instanceof String)) {
                    data = (String) data + Utils.maskNull((String) dataAdd);
                } else {
                    data = dataAdd;
                }
            }

            return data;
        }
    }

    public List<ColumnConf> getMediaColumnList(String media) {
        return getColumnConfList(getMediaColumns(media));
    }

    public List<ColumnConf> getColumnConfList(List<String> columnIds) {
        // обработка склеиваемых с помощью + колонок
        List<ColumnConf> result = new ArrayList<ColumnConf>(columnIds.size());
        for (String columnId : columnIds) {
            ColumnConf cc = new ColumnConf();
            for (Integer colId : Utils.toIntegerList(columnId, "+")) {
                cc.addColumnRef(new ColumnRef(colId, columnMap.get(colId)));
            }
            result.add(cc);
        }

        // определение позиций столбцов в rawData массиве
        int index = 1;
        for (Map.Entry<Integer, ParameterMap> me : columnMap.entrySet()) {
            int columnId = me.getKey();

            for (ColumnConf cc : result) {
                for (ColumnRef cr : cc.refList) {
                    if (cr.columnId == columnId) {
                        cr.rawDataIndex = index;
                    }
                }
            }

            index++;
        }

        return result;
    }

    public void processDataForMedia(DynActionForm form, String media, List<Object[]> rawData) throws SQLException {
        List<ColumnConf> mediaColumns = getMediaColumnList(media);

        final boolean isHtmlMedia = MEDIA_HTML.equals(media) || MEDIA_HTML_OPEN.equals(media);

        processDataForColumns(form, rawData, mediaColumns, isHtmlMedia);
    }

    /**
     * Преобразует объект rowData, заменяя "сырые" данные в нём упорядоченными значениями столбцов.
     * 
     * @param form
     * @param rawData
     * @param mediaColumns
     * @param isHtmlMedia
     */
    public void processDataForColumns(DynActionForm form, List<Object[]> rawData, List<ColumnConf> mediaColumns, boolean isHtmlMedia) throws SQLException {
        final int columnsForMedia = mediaColumns.size();

        // размер массива, 0ой элемент занят объектом Process для вывода в HTML
        final int size = isHtmlMedia ? columnsForMedia + 1 : columnsForMedia;

        final int records = rawData.size();

        for (int k = 0; k < records; k++) {
            Object[] data = rawData.get(k);
            Object[] mediaData = new Object[size];

            if (isHtmlMedia) {
                mediaData[0] = data[0];

                for (int i = 1; i < size; i++) {
                    ColumnConf info = mediaColumns.get(i - 1);
                    if (info != null) {
                        mediaData[i] = info.getValue(form, isHtmlMedia, data);
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    ColumnConf info = mediaColumns.get(i);
                    if (info != null) {
                        mediaData[i] = info.getValue(form, isHtmlMedia, data);
                    }
                }
            }

            rawData.set(k, mediaData);
        }
    }

    // универсальная расшифровка справочных полей для вывода как в HTML так и на печать
    private static Object decryptDir(DynActionForm form, Process[] processArray, final boolean isHtml, ColumnRef ref, Object obj) throws SQLException {
        Object value = null;

        String columnType = ref.columnConf.get("value");

        Process process = ref.getProcess(processArray);

        if (columnType.startsWith("executor")) {
            String[] tokens = columnType.split(":");

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

            value = executors.toString();
        } else if (columnType.startsWith("groups")) {
            Set<Integer> allowedRoleIds = Utils.toIntegerSet(StringUtils.substringAfterLast(columnType, ":"));

            StringBuilder groups = new StringBuilder(50);

            Set<Integer> groupIds = process.getGroupIds();
            Set<Integer> allowedRolesGroups = process.getGroupIdsWithRoles(allowedRoleIds);

            for (Group group : UserCache.getUserGroupList()) {
                if (groupIds.contains(group.getId()) && (allowedRoleIds.size() == 0 || allowedRolesGroups.contains(group.getId()))) {
                    Utils.addSeparated(groups, ", ", group.getTitle());
                }
            }

            value = groups.toString();
        }
        // TODO: Сделать подзапросом сразу в ProcessDAO.
        // linkProcessList:depend:open or linkedProcessList:depend:open
        else if (columnType.startsWith("linkProcessList") || columnType.startsWith("linkedProcessList")) {
            String[] tokens = columnType.split(":");

            String linkTypeFilter = tokens.length > 1 ? tokens[1] : "*";
            String stateFilter = tokens.length > 2 ? tokens[2] : "open";
            String typeFilter = tokens.length > 3 ? tokens[3] : "*";

            String linkTypeParam = linkTypeFilter.equals("*") ? null : linkTypeFilter;
            boolean stateParam = stateFilter.equals("open");
            Set<Integer> typeParam = typeFilter.equals("*") ? null : Utils.toIntegerSet(typeFilter);

            ProcessLinkDAO dao = new ProcessLinkDAO(form.getConnectionSet().getSlaveConnection(), form.getUser());
            
            if (columnType.startsWith("linkProcessList"))
                value = dao.getLinkProcessList(process.getId(), linkTypeParam, stateParam, typeParam);
            else
                value = dao.getLinkedProcessList(process.getId(), linkTypeParam, stateParam, typeParam);
        } else {
            if (isHtml) {
                value = obj;
            }
            //FIXME: Это должно быть в плагине BGBilling.
            else if (columnType.startsWith("linkObject:contract")) {
                value = String.valueOf(obj).replaceAll("\\w+:\\d+:", "");
            } else {
                value = String.valueOf(obj).replace("\n", " ").replace("\r", " ");
            }
        }

        return value;
    }

    public ParameterMap getConfigMap() {
        if (configMap == null) {
            configMap = new Preferences(config);
        }
        return configMap;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Set<Integer> getProcessTypeIds() {
        return processTypeIds;
    }

    public void setProcessTypeIds(Set<Integer> processTypes) {
        this.processTypeIds = processTypes;
    }

    public List<ParameterMap> getColumnList() {
        return columnList;
    }

    public SortedMap<Integer, ParameterMap> getColumnMap() {
        return columnMap;
    }

    public FilterList getFilterList() {
        return filterList;
    }

    @Deprecated
    public Map<Integer, Processor> getProcessorMap() {
        return processorMap;
    }

    /**
     * Processors for interface.
     * @param iface value from {@link org.bgerp.Interface}.
     * @return
     */
    public List<Processor> getProcessors(String iface) {
        return processorMap.values().stream()
            .filter(p -> p.getIface().equals(iface))
            .collect(Collectors.toList());
    }

    public SortSet getSortSet() {
        return sortSet;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public List<IdTitle> getCreateAllowedProcessList() {
        return createAllowedProcessList;
    }

    public LastModify getLastModify() {
        return lastModify;
    }

    public void setLastModify(LastModify lastModify) {
        this.lastModify = lastModify;
    }

    public String getOpenUrl() {
        return getConfigMap().get("openUrl");
    }

    public void extractFiltersAndSorts() {
        log.debug("Extract queue id: %s ;title: %s", id, title);

        var config = new Preferences(this.config);

        columnMap = config.subIndexed("column.");

        columnList = new ArrayList<ParameterMap>();
        columnList.addAll(columnMap.values());

        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("filter.").entrySet()) {
            int id = me.getKey();
            ParameterMap filter = me.getValue();

            try {
                String type = filter.get("type", "");

                if (StringUtils.equalsAny(type, "status", "groups", "executors", "close_date", "create_date", "status_date", "code", "description", "message:systemId")) {
                    filterList.add(new Filter(id, filter));
                } else if ("type".equals(type)) {
                    filterList.add(new FilterProcessType(id, filter));
                } else if ("openClose".equals(type)) {
                    filterList.add(new FilterOpenClose(id, filter));
                } else if ("grex".equals(type)) {
                    filterList.add(new FilterGrEx(id, filter));
                }
                // фильтры временно заблокированы, т.к. непонятны их отношения с фильтром по группам:
                // закрывающий, создающий, меняющий статус пользователь не обязательно имеет группу, состояющую в составе исполнителей
                // особенно запутывается всё, если параллельно используется фильтр по исполнителям
                // и, похоже, что не работало переключение при смене групп, т.к. чекбоксы генерируются со стандартным именем executor
                /*
                else if( "status_user".equals( type ) )
                {
                	filterSet.statusUserFilter = new Filter( filter );
                }
                else if( type.startsWith( "status_user:" ) )
                {					
                	filterSet.statusUserFilter = new FilterStatusUser( filter, type );
                }				
                else if( "create_user".equals( type ) )
                {
                	filterSet.createUserFilter = new Filter( filter );
                }
                else if( "close_user".equals( type ) )
                {
                	filterSet.closeUserFilter = new Filter( filter );
                }*/
                else if ("quarter".equals(type)) {
                    int paramId = Utils.parseInt(filter.get("param"));
                    if (paramId > 0) {
                        Parameter parameter = ParameterCache.getParameter(paramId);
                        filterList.add(new FilterParam(id, filter, parameter));
                    }
                } else if (type.startsWith("linkCustomer:") || type.startsWith("linkedCustomer:")) {
                    String entity = StringUtils.substringAfter(type, ":");
                    if (entity.startsWith("title")) {
                        filterList.add(new Filter(id, filter));
                    } else if (entity.startsWith("param:")) {
                        int paramId = Utils.parseInt(StringUtils.substringAfter(entity, "param:"));
                        Parameter parameter = ParameterCache.getParameter(paramId);

                        if (Parameter.TYPE_LIST.equals(parameter.getType())) {
                            filterList.add(new FilterCustomerParam(id, filter, parameter));
                        }
                    }
                } else if (type.startsWith("linkObject")) {
                    filterList.add(new FilterLinkObject(id, filter, filter.get("objectType"), filter.get("whatFilter", "id")));
                } else if ("linkedObject".equals(type)) {
                    filterList.add(new Filter(id, filter));
                } else if (type.startsWith("param:")) {
                    int paramId = Utils.parseInt(StringUtils.substringAfter(type, ":"));

                    Parameter param = ParameterCache.getParameter(paramId);
                    if (param == null) {
                        log.error("Param not found for filter: " + paramId);
                        continue;
                    }

                    String paramType = param.getType();

                    if (Parameter.TYPE_LIST.equals(paramType) || Parameter.TYPE_LISTCOUNT.equals(paramType)
                            || Parameter.TYPE_DATETIME.equals(paramType) || Parameter.TYPE_DATE.equals(paramType)
                            || Parameter.TYPE_ADDRESS.equals(paramType) || Parameter.TYPE_TEXT.equals(paramType)
                            || Parameter.TYPE_BLOB.equals(paramType)) {
                        filterList.add(new FilterParam(id, filter, param));
                    } else {
                        log.error("Queue configuration error " + this.id + " \"" + this.title + "\": in the filter " + id
                                + " configured to unsupported parameter type - " + paramType);
                    }
                } else {
                    log.error("Queue configuration error " + this.id + " \"" + this.title + "\": incorrect filter type " + id + " - " + type);
                }
            } catch (Throwable t) {
                log.error("An error occurred while reading the queue configuration " + this.id + " \"" + this.title + "\" ", t);
            }
        }

        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("processor.").entrySet()) {
            Processor p = new Processor(me.getKey(), me.getValue());
            processorMap.put(p.getId(), p);
        }

        sortSet.setComboCount(config.getInt("sort.combo.count", 0));

        for (Map<String, String> sortComboValues : config.parseObjects("sort.combo.")) {
            int id = Utils.parseInt(sortComboValues.get("id"));
            if (id > 0) {
                int defaultValue = Utils.parseInt(sortComboValues.get("default"));
                if (defaultValue > 0) {
                    sortSet.setDefaultSortValue(id, defaultValue);
                }
                int value = Utils.parseInt(sortComboValues.get("value"));
                if (value > 0) {
                    sortSet.setSortValue(id, value);
                }
            }
        }

        for (Map<String, String> modeParams : config.parseObjects("sort.mode.")) {
            SortMode mode = new SortMode();
            mode.setDesc(Utils.parseBoolean(modeParams.get("desc")));
            mode.setTitle(modeParams.get("title"));

            String expression = modeParams.get("expr");
            int columnId = Utils.parseInt(modeParams.get("columnId"));
            if (columnId <= 0) {
                columnId = Utils.parseInt(modeParams.get("column.id"));
            }

            if (Utils.notBlankString(expression)) {
                mode.setOrderExpression(expression);
            } else if (columnId > 0) {
                ParameterMap column = columnMap.get(columnId);

                if (column == null) {
                    log.error("Queue configuration error " + this.id + " \"" + this.title + "\": sort by non existent column " + columnId);
                    continue;
                }

                mode.setColumnPos(columnList.indexOf(column) + 1);
            } else if (columnId == 0) {
                mode.setOrderExpression("RAND()");
            } else {
                log.error("Incorrect columnId: " + columnId);
                continue;
            }

            if (Utils.notBlankString(mode.getTitle())) {
                sortSet.addMode(mode);
                if (log.isDebugEnabled()) {
                    log.debug("Sort mode add: " + mode);
                }
            }
        }

        for (ParameterMap actionConfig : config.subIndexed("action.").values()) {
            actionList.add(new Action(actionConfig));
        }

        for (String token : config.get("createAllowedProcessList", "").split(";")) {
            String[] pair = token.split(":");
            if (pair.length != 2) {
                continue;
            }
            createAllowedProcessList.add(new IdTitle(Utils.parseInt(pair[0]), pair[1]));
        }
    }
}
