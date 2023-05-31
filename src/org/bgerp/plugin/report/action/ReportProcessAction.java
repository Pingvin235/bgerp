package org.bgerp.plugin.report.action;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.app.l10n.Localization;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.plugin.report.Plugin;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.plugin.report.model.chart.Chart;
import org.bgerp.plugin.report.model.chart.ChartBar;
import org.bgerp.plugin.report.model.chart.ChartPie;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/report/report/process")
public class ReportProcessAction extends ReportActionBase {
    private static final Column COL_ID = new Column.ColumnInteger("id", "ID", null);
    private static final Column COL_TYPE_TITLE = new Column.ColumnString("type_title", null, "Тип");

    private static final Columns COLUMNS = new Columns(
        COL_ID,
        COL_TYPE_TITLE,
        new Column.ColumnString("user_id", null, null),
        new Column.ColumnString("user_title", null, "User"),
        new Column.ColumnDateTime("time", null, "Время", TimeUtils.FORMAT_TYPE_YMDHM),
        new Column.ColumnString("process_description", null, "Описание")
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

    /**
     * This overwritten method is required because of action specification.
     */
    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet) throws Exception {
        return super.unspecified(form, conSet);
    }

    @Override
    public String getTitle() {
        return new Localizer(Localization.getLang(), org.bgerp.plugin.kernel.Plugin.INSTANCE.geLocalization()).l("Процессы");
    }

    @Override
    protected String getJsp() {
        return Plugin.PATH_JSP_USER + "/report/process.jsp";
    }

    @Override
    protected String getHref() {
        return "report/process";
    }

    @Override
    public Columns getColumns() {
        return COLUMNS;
    }

    @Override
    public List<Chart> getCharts() {
        return CHARTS;
    }

    @Override
    protected Selector getSelector() {
        return new Selector() {
            @Override
            protected void select(ConnectionSet conSet, Data data) throws Exception {
                final var form = data.getForm();

                final var dateFrom = form.getParamDate("dateFrom", new Date(), true);
                final var dateTo = form.getParamDate("dateTo", new Date(), true);

                final var mode = form.getParam("mode", "create", true, null);
                if (!StringUtils.equalsAny(mode, "create", "close"))
                    throw new BGIllegalArgumentException();

                final var typeIds = form.getSelectedValues("type");

                final var pq = new PreparedQuery(conSet.getSlaveConnection());
                pq.addQuery(SQL_SELECT_COUNT_ROWS + " id, type_id, " + mode + "_user_id, " + mode + "_dt, description " + SQL_FROM + Tables.TABLE_PROCESS);
                pq.addQuery(SQL_WHERE);
                pq.addQuery(mode + "_dt");
                pq.addQuery(" BETWEEN ? AND ?");
                pq.addDate(dateFrom);
                pq.addDate(TimeUtils.getNextDay(dateTo));
                if (!typeIds.isEmpty())
                    pq.addQuery(SQL_AND + "type_id IN (" + Utils.toString(typeIds) + ")");
                pq.addQuery(SQL_ORDER_BY);
                pq.addQuery(mode + "_dt");
                pq.addQuery(getPageLimit(form.getPage()));

                var processTypes = new TreeSet<IdTitle>();

                var rs = pq.executeQuery();
                while (rs.next()) {
                    final var record = data.addRecord();
                    record.add(rs.getInt(record.pos()));

                    var processType = ProcessTypeCache.getProcessTypeSafe(rs.getInt(record.pos()));
                    record.add(processType.getTitle());
                    processTypes.add(processType);

                    final int userId = rs.getInt(record.pos());
                    record.add(userId);
                    record.add(UserCache.getUser(userId).getTitle());
                    record.add(rs.getTimestamp(record.pos()));
                    record.add(rs.getString(record.pos()));
                }

                form.setResponseData("types", processTypes.stream()
                    .sorted((t1, t2) -> t1.getTitle().compareTo(t2.getTitle()))
                    .collect(Collectors.toList()));

                setRecordCount(form.getPage(), pq.getPrepared());
            }
        };
    }
}
