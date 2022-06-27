package ru.bgcrm.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;

/**
 * Монитор, отслеживает изменения таблиц и оповещает подписавшихся слушателей.
 */
public class TableChangeMonitor extends Thread {
    private static final Log log = Log.getLog();

    private static TableChangeMonitor instance = new TableChangeMonitor();

    private static final long CHECK_PERIOD = 60 * 1000L;

    public static void subscribeOnChange(String subscriptionPoint, String tableName, Runnable callback) {
        Map<String, Runnable> tablesMap = instance.subscriberMap.get(subscriptionPoint);
        if (tablesMap == null) {
            instance.subscriberMap.put(subscriptionPoint, tablesMap = new ConcurrentHashMap<String, Runnable>());
        }
        tablesMap.put(tableName, callback);
    }

    // конец статической части

    // первый ключ - строка, идентифицирующая подписчика, второй - таблица
    private Map<String, Map<String, Runnable>> subscriberMap = new ConcurrentHashMap<String, Map<String, Runnable>>();
    private Map<String, String> rowCounts = new HashMap<String, String>();

    private TableChangeMonitor() {
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Connection con = Setup.getSetup().getDBConnectionFromPool();

                // ключ - имя таблицы, значение - вызываемые коллбеки
                Map<String, List<Runnable>> runnableMap = new HashMap<String, List<Runnable>>();

                for (Map.Entry<String, Map<String, Runnable>> me : subscriberMap.entrySet()) {
                    for (Map.Entry<String, Runnable> rme : me.getValue().entrySet()) {
                        String tableName = rme.getKey();
                        Runnable callback = rme.getValue();

                        List<Runnable> runList = runnableMap.get(tableName);
                        if (runList == null) {
                            runnableMap.put(tableName, runList = new ArrayList<Runnable>());
                        }
                        runList.add(callback);
                    }
                }

                // пока простейшая проверка изменённости таблицы подсчётом записей,
                // лучше метода не найдено пока
                for (String tableName : runnableMap.keySet()) {
                    String query = "SELECT COUNT(*) FROM " + tableName;
                    PreparedStatement ps = con.prepareStatement(query);

                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String count = rs.getString(1);

                        String prevCount = rowCounts.put(tableName, count);
                        if (prevCount != null && !prevCount.equals(count)) {
                            if (log.isDebugEnabled()) {
                                log.debug("Table changed: " + tableName);
                            }

                            List<Runnable> runnableList = runnableMap.get(tableName);
                            for (Runnable r : runnableList) {
                                r.run();
                            }
                        }
                    }
                    ps.close();
                }

                SQLUtils.closeConnection(con);

                sleep(CHECK_PERIOD);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}