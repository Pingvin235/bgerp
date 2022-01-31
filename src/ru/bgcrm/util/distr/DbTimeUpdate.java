package ru.bgcrm.util.distr;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.bgerp.util.Log;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.TimeUtils;

/**
 * Developer DB utils, updates all date and time columns till the current day.
 * <p> Do not run on production databases!
 *
 * @author Shamil Vakhitov
 */
public class DbTimeUpdate implements Runnable {
    private static final Log log = Log.getLog();

    public static final String SETUP_KEY = "generation.time";

    int daysDelta;

    @Override
    public void run() {
        var time = TimeUtils.parse(Setup.getSetup().get(SETUP_KEY), TimeUtils.FORMAT_TYPE_YMDHMS);
        log.info("{}: {}", SETUP_KEY, time);
        if (time == null) {
            log.warn("No {} found, isn't this DB a production one?");
            return;
        }

        // TODO: For some data it has to be the same day of week. E.g. callboard.
        daysDelta = TimeUtils.daysDelta(TimeUtils.convertDateToCalendar(time), new GregorianCalendar());
        log.info("daysDelta: {}", daysDelta);
        if (daysDelta <= 0)
            return;

        try (var con = Setup.getSetup().getDBConnectionFromPool()) {
            var query =
                "SELECT table_name, column_name FROM information_schema.columns " +
                "WHERE table_schema=DATABASE() AND data_type IN ('date', 'datetime', 'timestamp') " +
                "ORDER BY table_name";

            String currentTable = null;
            List<String> columns = new ArrayList<>();

            var rs = con.createStatement().executeQuery(query);
            while (rs.next()) {
                final var table = rs.getString(1);
                final var column = rs.getString(2);

                log.debug("Table: {}, column: {}", table, column);

                // new table has started
                if ((currentTable != null && !table.equals(currentTable))) {
                    updateTable(con, currentTable, columns);
                }

                currentTable = table;
                columns.add(column);
            }

            // update the last table
            updateTable(con, currentTable, columns);
        } catch (Exception e) {
            log.error(e);
        }
    }

    private void updateTable(Connection con, String currentTable, List<String> columns) throws SQLException {
        try (var pq = new PreparedQuery(con)) {
            pq.addQuery("UPDATE ").addQuery(currentTable).addQuery(" SET ");

            var first = true;
            for (var col : columns) {
                if (!first)
                    pq.addQuery(", ");

                pq.addQuery(col).addQuery("=DATE_ADD(").addQuery(col).addQuery(", INTERVAL ? DAY)");
                pq.addInt(daysDelta);

                first = false;
            }

            log.info("Running query: {}", pq);
            pq.executeUpdate();

            columns.clear();
        }
    }
}
