package ru.bgcrm.model;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.model.base.IdTitle;

public class ParamList extends IdTitle {
    private final List<IdTitle> values = new ArrayList<>();

    public void addValue(IdTitle paramListValue) {
        if (paramListValue.getId() == id) {
            title = paramListValue.getTitle();
        }
        values.add(paramListValue);
    }

    public List<IdTitle> getValues() {
        return values;
    }
}
