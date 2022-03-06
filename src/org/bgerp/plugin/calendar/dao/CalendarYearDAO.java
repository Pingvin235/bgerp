package org.bgerp.plugin.calendar.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.util.sql.PreparedDelay;

/**
 * For which years calendar is initialized.
 *
 */
public class CalendarYearDAO extends CommonDAO {
    public CalendarYearDAO(Connection con) {
        super(con);
    }

    public List<Integer> getYears(int calendarId) throws SQLException {
        return getIds(Tables.TABLE_YEAR, "calendar_id", "year", "year", calendarId);
    }

    public void removeYear(int calendarId, int year) throws SQLException {
        String query = SQL_DELETE + Tables.TABLE_YEAR + SQL_WHERE + "calendar_id=? AND year=?";
        try (var pd = new PreparedDelay(con, query)) {
            pd.addInt(calendarId).addInt(year).executeUpdate();
        }
    }

    public void addYear(int calendarId, int year) throws SQLException {
        updateOrInsert(
            SQL_UPDATE + Tables.TABLE_YEAR + SQL_SET + "year=?" + SQL_WHERE + "calendar_id=? AND year=?",
            SQL_INSERT + Tables.TABLE_YEAR + "(calendar_id, year)",
            calendarId, year);
    }

}
