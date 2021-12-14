package org.bgerp.plugin.report.action;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.plugin.report.Plugin;
import org.bgerp.plugin.report.model.chart.Chart;
import org.bgerp.plugin.report.model.chart.ChartBar;
import org.bgerp.plugin.report.model.chart.ChartPie;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgcrm.util.sql.PreparedDelay;
import ru.bgerp.l10n.Localizer;

@Action(path = "/user/plugin/report/report/process")
public class ReportProcessAction extends ReportActionBase {
    /**
     * This overwritten method is required because of action specification.
     */
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return super.unspecified(form, conSet);
    }

    @Override
    public String getTitle(Localizer l) {
        return l.l("Процессы");
    }

    @Override
    protected String getJsp() {
        return Plugin.PATH_JSP_USER + "/report/process.jsp";
    }

    @Override
    protected String getHref() {
        return "report/process";
    }

    private static final Column COL_ID = new Column.ColumnInteger("id", "ID", null);
    private static final Column COL_TYPE_TITLE = new Column.ColumnString("type_title", null, "Тип");
    private static final Column COL_USER_ID = new Column.ColumnString("user_id", null, null);
    private static final Column COL_USER_TITLE = new Column.ColumnString("user_title", null, "Пользователь");
    private static final Column COL_TIME = new Column.ColumnDateTime("time", null, "Время", TimeUtils.FORMAT_TYPE_YMDHM);
    private static final Column COL_DESCRIPTION = new Column.ColumnString("process_description", null, "Описание");

    @Override
    protected Selector getSelector() {
        return new Selector() {
            @Override
            protected void select(ConnectionSet conSet, Data data) throws Exception {
                final var form = data.getForm();

                final var dateFrom = form.getParamDate("dateFrom", new Date(), true);
                final var dateTo = form.getParamDate("dateTo", new Date(), true);

                final var type = form.getParam("type", "create", true, null);
                if (!StringUtils.equalsAny(type, "create", "close"))
                    throw new BGIllegalArgumentException();

                final var pd = new PreparedDelay(conSet.getSlaveConnection());
                pd.addQuery(SQL_SELECT_COUNT_ROWS + " id, type_id, " + type + "_user_id, " + type + "_dt, description " + SQL_FROM + Tables.TABLE_PROCESS);
                pd.addQuery(SQL_WHERE);
                pd.addQuery(type + "_dt");
                pd.addQuery(" BETWEEN ? AND ?");
                pd.addDate(dateFrom);
                pd.addDate(TimeUtils.getNextDay(dateTo));
                pd.addQuery(SQL_ORDER_BY);
                pd.addQuery(type + "_dt");
                pd.addQuery(getPageLimit(form.getPage()));

                var rs = pd.executeQuery();
                while (rs.next()) {
                    final var r = data.addRecord();
                    r.add(rs.getInt(r.pos()));
                    r.add(ProcessTypeCache.getProcessType(rs.getInt(r.pos())).getTitle());
                    final int userId = rs.getInt(r.pos());
                    r.add(userId);
                    r.add(UserCache.getUser(userId).getTitle());
                    r.add(rs.getTimestamp(r.pos()));
                    r.add(rs.getString(r.pos()));
                }

                setRecordCount(form.getPage(), pd.getPrepared());
            }
        };
    }

    private static final Columns COLUMNS = new Columns(
        COL_ID,
        COL_TYPE_TITLE,
        COL_USER_ID,
        COL_USER_TITLE,
        COL_TIME,
        COL_DESCRIPTION
    );

    private final List<Chart> CHARTS = List.of(
        new ChartBar(
            "Количества по типам процессов",
            COL_TYPE_TITLE,
            new Column.ColumnCount(COL_ID)
        ),
        new ChartPie(
            "Количества по типам процессов",
            COL_TYPE_TITLE,
            new Column.ColumnCount(COL_ID)
        )
        // TODO: Add obitary param like 'cost' for making summs.
        // TODO: Created by hour of the day.
        // TODO: Closed by executor (support many).
    );

    @Override
    protected Columns getColumns() {
        return COLUMNS;
    }

    @Override
    public List<Chart> getCharts() {
        return CHARTS;
    }
}
