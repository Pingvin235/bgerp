package org.bgerp.plugin.pln.grpl.model;

import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.iface.IdTitle;

import ru.bgcrm.util.Utils;

public class ColumnConfig extends Config implements IdTitle<Integer> {
    private final int id;
    private final String title;
    private final Set<Integer> cityIds;

    ColumnConfig(int id, ConfigMap config) {
        super(null);
        this.id = id;
        title = config.get("title", "???");
        cityIds = Utils.toIntegerSet(config.get("cities"));
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public Set<Integer> getCityIds() {
        return cityIds;
    }
}
