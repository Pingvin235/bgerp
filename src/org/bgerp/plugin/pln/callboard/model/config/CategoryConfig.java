package org.bgerp.plugin.pln.callboard.model.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.util.Utils;

public class CategoryConfig extends Config {
    private List<Category> list = new ArrayList<>();

    public CategoryConfig(ConfigMap config) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("callboard.worktype.category.").entrySet()) {
            ConfigMap params = me.getValue();
            list.add(new Category(me.getKey(), params.get("title", ""), params.getBoolean("public", false)));
        }
    }

    public List<Category> getCategoryList(Set<Integer> allowOnly) {
        List<Category> result = new ArrayList<>();

        for (Category cat : this.list) {
            if (CollectionUtils.isEmpty(allowOnly) || allowOnly.contains(cat.getId()) || cat.isForAll()) {
                result.add(cat);
            }
        }

        return result;
    }

    public Set<Integer> getCategoryIds(Set<Integer> allowOnly) {
        return Utils.getObjectIdsSet(getCategoryList(allowOnly));
    }

    public static class Category extends IdTitle {
        private final boolean forAll;

        public Category(int id, String title, boolean forAll) {
            super(id, title);
            this.forAll = forAll;
        }

        public boolean isForAll() {
            return forAll;
        }
    }
}
