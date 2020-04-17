package ru.bgcrm.model.process.queue;

import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.util.ParameterMap;

public class FilterParam extends Filter {
    private Parameter parameter;

    public FilterParam(int id, ParameterMap filter, Parameter parameter) {
        super(id, filter);
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
