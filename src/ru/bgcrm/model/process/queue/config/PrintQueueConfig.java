package ru.bgcrm.model.process.queue.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.util.Utils;

public class PrintQueueConfig extends Config {
    private LinkedHashMap<Integer, PrintType> printTypes = new LinkedHashMap<>();

    public PrintQueueConfig(ConfigMap config) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("media.print.").entrySet()) {
            PrintType type = new PrintType(me.getKey(), me.getValue());
            printTypes.put(type.getId(), type);
        }
    }

    public Collection<PrintType> getPrintTypes() {
        return printTypes.values();
    }

    public PrintType getPrintType(int id) {
        return printTypes.get(id);
    }

    public static class PrintType extends IdTitle {
        public static final String TYPE_PDF = "pdf";

        public static final String ORIENTATION_PORTRAIT = "portrait";
        public static final String ORIENTATION_LANDSCAPE = "landscape";

        // в реальности пока не используется, может генерироваться только PDF
        private final String type;
        private final String orientation;
        private final String fileName;

        private final List<String> columnIds = new ArrayList<>();
        private final List<Integer> columnWidths = new ArrayList<>();

        private PrintType(int id, ConfigMap config) {
            super(id, config.get("title"));

            this.type = config.get("type", TYPE_PDF);
            this.orientation = config.get("orientation", ORIENTATION_PORTRAIT);
            this.fileName = config.get("fileName", "queue.pdf");

            for (String pair : Utils.toList(config.get("columns"))) {
                String[] tokens = pair.split(":");
                if (tokens.length != 2) {
                    continue;
                }

                columnIds.add(tokens[0]);
                columnWidths.add(Utils.parseInt(tokens[1]));
            }
        }

        public String getType() {
            return type;
        }

        public String getOrientation() {
            return orientation;
        }

        public String getFileName() {
            return fileName;
        }

        public List<String> getColumnIds() {
            return columnIds;
        }

        public List<Integer> getColumnWidths() {
            return columnWidths;
        }
    }
}