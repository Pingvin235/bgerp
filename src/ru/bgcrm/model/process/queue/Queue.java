package ru.bgcrm.model.process.queue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.bean.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Preferences;
import org.bgerp.model.base.IdTitle;
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
import org.bgerp.util.Dynamic;
import org.bgerp.util.Log;

import javassist.NotFoundException;
import ru.bgcrm.cache.ParameterCache;
import ru.bgcrm.model.LastModify;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class Queue extends IdTitle {
    private static final Log log = Log.getLog();

    public static final String MEDIA_HTML = "html";
    public static final String MEDIA_HTML_OPEN = "html.open";
    public static final String MEDIA_PRINT = "print";
    public static final String MEDIA_XLS = "xls";

    private static final Set<String> SUPPORTED_PARAM_TYPES = Set.of(Parameter.TYPE_ADDRESS, Parameter.TYPE_DATETIME, Parameter.TYPE_DATE,
            Parameter.TYPE_LIST, Parameter.TYPE_LISTCOUNT, Parameter.TYPE_MONEY, Parameter.TYPE_TEXT, Parameter.TYPE_BLOB);

    private int id;
    private String title;
    private String config;
    private Set<Integer> processTypeIds = new HashSet<>();
    private ConfigMap configMap;

    private List<Column> columnList;
    private Map<Integer, Column> columnMap;

    private SortSet sortSet = new SortSet();

    private final FilterList filterList = new FilterList();

    private final Map<Integer, Processor> processorMap = new HashMap<>();
    private final List<Action> actionList = new ArrayList<>();
    private final List<IdTitle> createAllowedProcessList = new ArrayList<>();

    private LastModify lastModify = new LastModify();

    public Queue() {}

    public Queue(int id, String title) {
        super(id, title);
    }

    public String getConfig() {
        return config;
    }

    private List<String> getMediaColumns(String media) {
        ConfigMap configMap = getConfigMap();

        List<String> result = Utils.toList(configMap.get("media." + media + ".columns"));

        // fallback for media 'html' only
        if (result.isEmpty() && MEDIA_HTML.equals(media))
            result = columnMap.keySet().stream().map(String::valueOf).collect(Collectors.toList());

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

    public List<MediaColumn> getMediaColumnList(String media) throws NotFoundException {
        return getMediaColumnList(getMediaColumns(media));
    }

    public List<MediaColumn> getMediaColumnList(List<String> columnIds) throws NotFoundException {
        var result = new ArrayList<MediaColumn>(columnIds.size());

        for (String columnIdStr : columnIds) {
            var column = columnMap.get(Utils.parseInt(columnIdStr));
            int cellIndex = columnList.indexOf(column) + 1;

            if (column == null)
                throw new NotFoundException(Log.format("Not found media queue column with ID: {}", columnIdStr));

            result.add(new MediaColumn(column, cellIndex));
        }

        return result;
    }

    public void replaceRowsForMedia(DynActionForm form, String media, List<Object[]> list) throws SQLException, NotFoundException {
        List<MediaColumn> mediaColumns = getMediaColumnList(media);

        final boolean isHtmlMedia = MEDIA_HTML.equals(media) || MEDIA_HTML_OPEN.equals(media);

        replaceRowsForMediaColumns(form, list, mediaColumns, isHtmlMedia);
    }

    /**
     * Replaces initial rows with cells ordered by configured columns to rows ordered by media columns. During that also extracted additional cell data for some cases.
     * @param form user HTTP request form.
     * @param list initial rows, ordered by configured columns.
     * @param mediaColumns media columns, ordered by displaying.
     * @param isHtmlMedia is the target media HTML.
     */
    public void replaceRowsForMediaColumns(DynActionForm form, List<Object[]> list, List<MediaColumn> mediaColumns, boolean isHtmlMedia) throws SQLException {
        final int columnsForMedia = mediaColumns.size();

        // array sized, 0 element is occupied by Process object for showing in HTML
        final int size = isHtmlMedia ? columnsForMedia + 1 : columnsForMedia;

        final int records = list.size();

        for (int k = 0; k < records; k++) {
            Object[] row = list.get(k);
            Object[] mediaRow = new Object[size];

            if (isHtmlMedia) {
                mediaRow[0] = row[0];
                for (int i = 1; i < size; i++) {
                    MediaColumn mediaColumn = mediaColumns.get(i - 1);
                    if (mediaColumn != null)
                        mediaRow[i] = mediaColumn.getValue(form, isHtmlMedia, row);
                }
            } else {
                for (int i = 0; i < size; i++) {
                    MediaColumn mediaColumn = mediaColumns.get(i);
                    if (mediaColumn != null)
                        mediaRow[i] = mediaColumn.getValue(form, isHtmlMedia, row);
                }
            }

            list.set(k, mediaRow);
        }
    }

    public ConfigMap getConfigMap() {
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

    public List<Column> getColumnList() {
        return columnList;
    }

    public Map<Integer, Column> getColumnMap() {
        return columnMap;
    }

    public FilterList getFilterList() {
        return filterList;
    }

    /**
     * Selects a processor by ID.
     * @param id the ID.
     * @return the found processor or {@code null}.
     */
    public Processor getProcessor(int id) {
        return processorMap.get(id);
    }

    /**
     * Processors for interface.
     * @param iface value from {@link org.bgerp.app.servlet.Interface}.
     * @return list of processors.
     */
    public List<Processor> getProcessors(String iface) {
        return processorMap.values().stream()
            .filter(p -> p.getIface().equals(iface))
            .collect(Collectors.toList());
    }

    /**
     * Selects a processor by a page URL.
     * @param url the page URL.
     * @return the found processor or {@code null}.
     */
    public Processor getProcessorByPageUrl(String url) {
        return processorMap.values().stream()
            .filter(p -> url.equals(p.getPageUrl()))
            .findFirst()
            .orElse(null);
    }

    public SortSet getSortSet() {
        return sortSet;
    }

    public List<Action> getActionList() {
        return actionList;
    }

    /**
     * @return list of process types, allowed to be created in 'usermob' interface.
     */
    @Dynamic
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

    public void extractFiltersAndSorts() throws Exception {
        log.debug("Extract queue id: {}; title: {}", id, title);

        var config = new Preferences(this.config);

        columnMap = new TreeMap<>();
        for (var me : config.subIndexed("column.").entrySet())
            columnMap.put(me.getKey(), Column.of(String.valueOf(me.getKey()), me.getValue()));
        columnList = new ArrayList<>(columnMap.values());

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("filter.").entrySet()) {
            int id = me.getKey();
            ConfigMap filter = me.getValue();

            try {
                String type = filter.get("type", "");

                if (StringUtils.equalsAny(type, "status", "groups", "executors", "close_date", "create_date",
                        "status_date", "code", "description", "message:systemId", "create_user", "close_user")) {
                    filterList.add(new Filter(id, filter));
                } else if ("type".equals(type)) {
                    filterList.add(new FilterProcessType(id, filter));
                } else if ("openClose".equals(type)) {
                    filterList.add(new FilterOpenClose(id, filter));
                } else if ("grex".equals(type)) {
                    filterList.add(new FilterGrEx(id, filter));
                } else if ("quarter".equals(type)) {
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

                    if (SUPPORTED_PARAM_TYPES.contains(paramType)) {
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

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("processor.").entrySet()) {
            Processor p = new Processor(me.getKey(), me.getValue());
            if (Utils.notBlankString(p.getClassName())) {
                Class<?> clazz = Bean.getClass(p.getClassName());
                p = (Processor) clazz.getDeclaredConstructor(int.class, ConfigMap.class).newInstance(me.getKey(), me.getValue());
            } else if (Utils.isBlankString(p.getPageUrl())) {
                log.error("For processor {} in process queue {} defined neither 'page.url' or 'className'", me.getKey(), id);
                continue;
            }
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
                Column column = columnMap.get(columnId);

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
                log.debug("Sort mode add: {}", mode);
            }
        }

        for (ConfigMap actionConfig : config.subIndexed("action.").values()) {
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
