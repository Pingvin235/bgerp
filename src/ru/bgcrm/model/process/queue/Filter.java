package ru.bgcrm.model.process.queue;

import java.util.List;
import java.util.Set;

import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

// параметры фильтра в очереди
public class Filter {
    public static final String VALUES = "values";
    public static final String ON_EMPTY_VALUES = "onEmptyValues";

    private final ParameterMap configMap;

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

    public Filter(int id, ParameterMap filter) {
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

    public ParameterMap getConfigMap() {
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

    public Set<Integer> getDefaultValues() {
        return defaultValues;
    }

    public List<Integer> getAvailableValues() {
        return availableValues;
    }

    public Set<String> getValues() {
        return values;
    }

    public Set<Integer> getOnEmptyValues() {
        return onEmptyValues;
    }

    public boolean isShow() {
        return show;
    }

    public String getWidth() {
        return width;
    }
}