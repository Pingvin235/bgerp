package ru.bgcrm.model.process.config;

import java.util.ArrayList;
import java.util.List;

import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class ProcessCardConfig extends Config {
    private List<Item> itemList = new ArrayList<Item>();

    public ProcessCardConfig(ParameterMap setup) throws BGException {
        super(setup);
        for (ParameterMap config : setup.subIndexed("processCard.").values()) {
            itemList.add(new Item(config));
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

        public Item(ParameterMap config) {
            this.mode = config.get("mode");
            this.componentList = Utils.toList(config.get("components"));
        }

        public List<String> getComponentList() {
            return componentList;
        }
    }
}