package ru.bgcrm.util.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.bgcrm.util.ParameterMap;

public class TrashDatabaseSelector {
    private List<TableMapRow> configRowList = new ArrayList<TableMapRow>();

    public TrashDatabaseSelector(ParameterMap setup) {
        ParameterMap configRows = setup.sub("trash.table.map.");
        for (Map.Entry<String, String> me : configRows.entrySet()) {
            String prefix = me.getKey();
            int pos = prefix.indexOf('.');
            if (pos > 0) {
                prefix = prefix.substring(pos + 1);
            } else {
                continue;
            }

            TableMapRow row = new TableMapRow();
            row.tablePrefix = prefix;
            row.database = me.getValue();
            configRowList.add(row);
        }
    }

    public String getDatabaseName(String tableName) {
        String result = null;
        for (TableMapRow row : configRowList) {
            if (tableName.startsWith(row.tablePrefix)) {
                result = row.database;
                break;
            }
        }
        return result;
    }

    private static class TableMapRow {
        public String tablePrefix;
        public String database;
    }
}
