package ru.bgcrm.model.process.config;

import java.util.ArrayList;
import java.util.List;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.util.Utils;

public class ProcessCardConfig extends Config {
    private List<Item> itemList = new ArrayList<>();

    public ProcessCardConfig(ConfigMap config) {
        super(null);
        for (ConfigMap pm : config.subIndexed("processCard.").values()) {
            itemList.add(new Item(pm));
        }
    }

    public Item getItem(String mode) {
        for (Item item : itemList) {
            if (mode.equals(item.mode)) {
                return item;
            }
        }
        return null;
    }

    public static class Item {
        private final String mode;
        private final List<String> componentList;

        public Item(ConfigMap config) {
            this.mode = config.get("mode");
            this.componentList = Utils.toList(config.get("components"));
        }

        public List<String> getComponentList() {
            return componentList;
        }
    }
}