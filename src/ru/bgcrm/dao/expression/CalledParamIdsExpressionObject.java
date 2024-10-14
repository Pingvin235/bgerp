package ru.bgcrm.dao.expression;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.bgcrm.model.param.ParameterAddressValue;

/**
 * Collects used in expression parameter IDs
 *
 * @author Shamil Vakhitov
 */
public class CalledParamIdsExpressionObject extends ProcessParamExpressionObject {
    private final Set<Integer> paramIds = new HashSet<>();

    public CalledParamIdsExpressionObject() {
        super(null, 0);
    }

    public Set<Integer> getParamIds() {
        return paramIds;
    }

    @Override
    public String val(int paramId) {
        paramIds.add(paramId);
        return "";
    }

    @Override
    protected Collection<ParameterAddressValue> getParamAddressValues(int paramId, String formatName) {
        paramIds.add(paramId);
        return List.of();
    }
}