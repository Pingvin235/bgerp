package ru.bgcrm.model.process.queue.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.Config;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Utils;

public class PrintQueueConfig extends Config {
    private LinkedHashMap<Integer, PrintType> printTypes = new LinkedHashMap<Integer, PrintType>();

    public PrintQueueConfig(ParameterMap config) {
        super(null);

        for (Map.Entry<Integer, ParameterMap> me : config.subIndexed("media.print.").entrySet()) {
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

        private final List<String> columnIds = new ArrayList<String>();
        private final List<Integer> columnWidths = new ArrayList<Integer>();

        private PrintType(int id, ParameterMap config) {
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