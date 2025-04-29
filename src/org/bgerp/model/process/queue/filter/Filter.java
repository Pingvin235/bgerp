package org.bgerp.model.process.queue.filter;

import java.util.List;
import java.util.Set;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.util.Dynamic;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Process queue filter
 *
 * @author Shamil Vakhitov
 */
public class Filter extends CommonDAO {
    public static final String VALUES = "values";
    public static final String ON_EMPTY_VALUES = "onEmptyValues";

    protected final ConfigMap configMap;

    private final int id;
    // произвольное наименование фильтра
    private final String title;
    // фильтр по допустимым значениям
    private final List<Integer> availableValues;
    // значения по-умолчанию
    private final Set<Integer> defaultValues;
    // жёстко определённые значения
    private final Set<String> values;
    // значения, если ни одно значение фильтра не выбрано
    private final Set<Integer> onEmptyValues;
    // отображать фильтр, он может быть скрытым, в этом случае всё время передаются defaultValues
    private final boolean show;
    // ширина фильтра
    private final String width;
    //тип фильтра
    private final String type;
    //мап параметров

    public Filter(int id, ConfigMap filter) {
        this.id = id;
        this.type = filter.get("type");
        this.title = filter.get("title");
        this.configMap = filter;
        this.onEmptyValues = Utils.toIntegerSet(filter.get(ON_EMPTY_VALUES));
        this.defaultValues = Utils.emptyToNull(Utils.toIntegerSet(filter.get("defaultValues")));
        this.values = Utils.toSet(filter.get(VALUES));
        this.availableValues = Utils.emptyToNull(Utils.toIntegerList(filter.get("availableValues")));
        this.show = Utils.parseBoolean(filter.get("show"), true);
        this.width = filter.get("width");
    }

    public ConfigMap getConfigMap() {
        return configMap;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    /**
     * @return default values, selected in UI.
     */
    @Dynamic
    public Set<Integer> getDefaultValues() {
        return defaultValues;
    }

    /**
     * @return available values in UI.
     */
    @Dynamic
    public List<Integer> getAvailableValues() {
        return availableValues;
    }

    /**
     * @return predefined values.
     */
    public Set<String> getValues() {
        return values;
    }

    /**
     * @return values when no others sent from UI in request.
     */
    public Set<Integer> getOnEmptyValues() {
        return onEmptyValues;
    }

    public boolean isShow() {
        return show;
    }

    public String getWidth() {
        return width;
    }

    /**
     * Takes comma separated list of values from request, taking on account {@link Filter#getValues()} and {@link Filter#getOnEmptyValues()}.
     * @param form
     * @param paramName HTTP request parameter.
     * @return
     */
    public String getValues(DynActionForm form, String paramName) {
        String result = Utils.toString(form.getParamValues(paramName));
        if (Utils.isBlankString(result) && !onEmptyValues.isEmpty()) {
            result = Utils.toString(onEmptyValues);
        }
        if (!result.isEmpty()) {
            result = Utils.toString(values);
        }
        return result;
    }

    public void apply(DynActionForm form, QueueSelectParams params) {
        throw new UnsupportedOperationException();
    }
}