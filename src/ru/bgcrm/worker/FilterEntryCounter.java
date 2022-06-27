package ru.bgcrm.worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bgerp.util.Log;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Setup;

/**
 * Caching counter of quantity of DB records.
 * For pooling them without too often SQL queries.
 */
public class FilterEntryCounter extends Thread {
    private Log log = Log.getLog();

    private static final long TIMEOUT = 60 * 1000L;

    private static FilterEntryCounter instance;

    private FilterEntryCounter() {
        start();
    }

    private static Map<String, CountAndTime> queries = new ConcurrentHashMap<>();

    public static FilterEntryCounter getInstance() {
        if (instance == null) {
            synchronized (FilterEntryCounter.class) {
                if (instance == null) {
                    instance = new FilterEntryCounter();
                }
            }
        }
        return instance;
    }

    private static class CountAndTime {
        public volatile int count;
        public volatile long time;

        public CountAndTime(int count, long time) {
            this.count = count;
            this.time = time;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                updateQueries();
                removeOldQueries();
            } catch (Exception e) {
                log.error(e);
            } finally {
                try {
                    Thread.sleep(TIMEOUT);
                } catch (Exception ex) {}
            }
        }
    }

    private void updateQueries() {
        try (var con = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool()) {
            for (String query : queries.keySet()) {
                countQuery(con, query);
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    private int countQuery(Connection con, String query) throws SQLException {
        int result = 0;

        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            var cnt = new CountAndTime(result = rs.getInt(1), System.currentTimeMillis());
            queries.put(query, cnt);
            log.debug("Put query size after: %s, count: %s, query: %s", queries.size(), cnt.count, query);
        }
        ps.close();

        return result;
    }

    private void removeOldQueries() {
        long interval = Setup.getSetup().getLong("filterEntryStorageInterval", TIMEOUT * 2);
        Long currentTime = Calendar.getInstance().getTimeInMillis();

        for (Map.Entry<String, CountAndTime> me : queries.entrySet()) {
            String query = me.getKey();
            CountAndTime value = me.getValue();

            if ((currentTime - value.time) > interval) {
                queries.remove(query);
                log.debug("Remove query, size after: %s, currentTime: %s, cnt.time: %s, query: %s",
                        queries.size(), currentTime, value.time, query);
            }
        }
    }

    private int getCount(String query) {
        CountAndTime result = queries.get(query);
        // "заявка" на подсчёт количества
        if (result == null) {
            queries.put(query, result = new CountAndTime(-1, System.currentTimeMillis()));
        }
        return result.count;
    }

    public int parseUrlAndGetCount(Queue queue, String url, User user) throws Exception {
        DynActionForm filterForm = new DynActionForm(url);
        filterForm.setUser(user);

        String query = new ProcessDAO(null, filterForm).getCountQuery(queue, filterForm);
        return getCount(query);
    }

    public int parseUrlAndGetCountSync(Queue queue, String url, User user) throws BGException {
        DynActionForm filterForm = new DynActionForm(url);
        filterForm.setUser(user);

        String query = new ProcessDAO(null).getCountQuery(queue, filterForm);

        if (queries.containsKey(query)) {
            return getCount(query);
        } else {
            int result = 0;
            try (var con = Setup.getSetup().getConnectionPool().getDBSlaveConnectionFromPool()) {
                result = countQuery(con, query);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return result;
        }
    }
}