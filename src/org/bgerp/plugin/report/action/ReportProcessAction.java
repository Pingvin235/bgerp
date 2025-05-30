package org.bgerp.plugin.report.action;

import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForward;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.cache.UserCache;
import org.bgerp.plugin.report.Plugin;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.plugin.report.model.chart.Chart;
import org.bgerp.plugin.report.model.chart.ChartBar;
import org.bgerp.plugin.report.model.chart.ChartPie;
import org.bgerp.util.sql.PreparedQuery;

import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/report/report/process")
public class ReportProcessAction extends ReportActionBase {
    private static final Column COL_ID = new Column.ColumnInteger("id", "ID", null);
    private static final Column COL_TYPE_TITLE = new Column.ColumnString("type_title", null, "Type");
    private static final Column COL_EXECUTORS = new Column.ColumnString("executors", null, "Executors").withCommaSeparatedValues();

    private static final Columns COLUMNS = new Columns(
        COL_ID,
        COL_TYPE_TITLE,
        new Column.ColumnString("user_id", null, null),
        new Column.ColumnString("user_title", null, "User"),
        new Column.ColumnDateTime("time", null, "Time", TimeUtils.FORMAT_TYPE_YMDHM),
        new Column.ColumnString("process_description", null, "Description"),
        COL_EXECUTORS
    );

    private final List<Chart> CHARTS = List.of(
        new ChartBar(
            "Quantities by process types",
            COL_TYPE_TITLE
        ),
        new ChartPie(
            "Quantities by process types",
            COL_TYPE_TITLE
        ),
        new ChartBar(
            "Quantities by executors",
            COL_EXECUTORS
        ),
        new ChartPie(
            "Quantities by executors",
            COL_EXECUTORS
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
        return org.bgerp.plugin.kernel.Plugin.INSTANCE.getLocalizer().l("Процессы");
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

                final boolean isModeCreate = "create".equals(mode);

                final var typeIds = form.getParamValues("type");

                try (final var pq = new PreparedQuery(conSet.getSlaveConnection())) {
                    pq.addQuery(SQL_SELECT_COUNT_ROWS + "*" + SQL_FROM + Tables.TABLE_PROCESS);
                    pq.addQuery(SQL_WHERE);
                    pq.addQuery(mode + "_dt");
                    pq.addQuery(" BETWEEN ? AND ?");
                    pq.addDate(dateFrom);
                    pq.addDate(TimeUtils.getNextDay(dateTo));
                    if (!typeIds.isEmpty())
                        pq.addQuery(SQL_AND + "type_id IN (" + Utils.toString(typeIds) + ")");
                    pq.addQuery(SQL_ORDER_BY);
                    pq.addQuery(mode + "_dt");
                    pq.addQuery(form.getPage().getLimitSql());

                    var processTypes = new TreeSet<ProcessType>();

                    var rs = pq.executeQuery();
                    while (rs.next()) {
                        final var record = data.addRecord();

                        var process = ProcessDAO.getProcessFromRs(rs);

                        record.add(process.getId());

                        var processType = process.getType();
                        record.add(processType.getTitle());
                        processTypes.add(processType);

                        final int userId = isModeCreate ? process.getCreateUserId() : process.getCloseUserId();
                        record.add(userId);
                        record.add(UserCache.getUser(userId).getTitle());
                        record.add(isModeCreate ? process.getCreateTime() : process.getCloseTime());
                        record.add(process.getDescription());
                        record.add(Utils.getObjectTitles(UserCache.getUserList(), process.getExecutorIds()));
                    }

                    form.setResponseData("types", processTypes.stream()
                        .sorted((t1, t2) -> t1.getTitle().compareTo(t2.getTitle()))
                        .collect(Collectors.toList()));

                    form.getPage().setRecordCount(pq.getPrepared());
                }
            }
        };
    }
}
