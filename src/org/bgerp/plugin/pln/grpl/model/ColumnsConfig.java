package org.bgerp.plugin.pln.grpl.model;

import java.util.List;

import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.cache.ParameterCache;
import org.bgerp.model.param.Parameter;

import ru.bgcrm.util.Utils;

class ColumnsConfig {
    static enum Type {
        CITY
    }

    final List<Integer> cityIds;
    final Parameter param;
    final Type type;

    ColumnsConfig(ConfigMap config) {
        cityIds = Utils.toIntegerList(config.get("column.cities"));
        param = ParameterCache.getParameter(config.getInt("column.param"));
        type = cityIds.isEmpty() ? null : Type.CITY;
    }
}
