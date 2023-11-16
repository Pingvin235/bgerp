package org.bgerp.model.process.link.config;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

public class ProcessLinkCategoryConfig extends Config {
    private final SortedMap<Integer, ProcessLinkCategory> categories;

    protected ProcessLinkCategoryConfig(ConfigMap config) throws InitStopException {
        super(null);
        categories = loadItems(config);
        initWhen(categories.size() > 0);
    }

    private SortedMap<Integer, ProcessLinkCategory> loadItems(ConfigMap config) {
        var result = new TreeMap<Integer, ProcessLinkCategory>();

        for (var me : config.subIndexed("process.link.category.").entrySet()) {
            try {
                var item = new ProcessLinkCategory(me.getKey(), me.getValue());
                result.put(item.getId(), item);
            } catch (InitStopException e) {}
        }

        return Collections.unmodifiableSortedMap(result);
    }

    public SortedMap<Integer, ProcessLinkCategory> getCategories() {
        return categories;
    }
}
