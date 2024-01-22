package org.bgerp.model.process.queue.filter;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.param.Parameter;

public class FilterParam extends Filter {
    private Parameter parameter;

    public FilterParam(int id, ConfigMap filter, Parameter parameter) {
        super(id, filter);
        this.parameter = parameter;
    }

    public Parameter getParameter() {
        return parameter;
    }
}
