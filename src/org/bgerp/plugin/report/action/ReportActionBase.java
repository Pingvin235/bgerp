package org.bgerp.plugin.report.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.BaseAction;
import org.bgerp.action.TitledAction;
import org.bgerp.action.TitledActionFactory;
import org.bgerp.app.exception.BGIllegalArgumentException;
import org.bgerp.model.base.iface.Title;
import org.bgerp.plugin.report.Plugin;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.bgerp.plugin.report.model.chart.Chart;
import org.bgerp.util.Log;
import org.reflections.Reflections;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;


/**
 * Parent action for all reports.
 *
 * @author Shamil Vakhitov
 */
public abstract class ReportActionBase extends BaseAction implements Title {

    /**
     * Selects children classes of {@link ReportActionBase} over all plugins.
     * <p> Used for generation permission actions list and UI menu items.
     * <p> To see usages search for 'org.bgerp.plugin.report.action.ReportActionBase$Factory' in project's files.
     */
    public static final class Factory implements TitledActionFactory {
        private static final Log log = Log.getLog();

        @Override
        public List<TitledAction> create() {
            List<TitledAction> result = new ArrayList<>();

            try {
                // place 'Report' plugin at the first place
                var pluginList = PluginManager.getInstance().getPluginList().stream()
                    .sorted((p1, p2) -> {
                        if (Plugin.ID.equals(p1.getId()))
                            return -1;
                        if (Plugin.ID.equals(p2.getId()))
                            return 1;
                        return 0;
                    })
                    .collect(Collectors.toList());

                for (var p : pluginList) {
                    var r = new Reflections(p.getClass().getPackageName());
                    for (Class<?> reportClass : r.getSubTypesOf(ReportActionBase.class)) {
                        var reportInstance = (ReportActionBase) reportClass.getConstructor().newInstance();
                        log.debug("Loading report: {}", reportInstance);
                        result.add(new TitledAction(
                            reportInstance,
                            reportInstance,
                            reportInstance.getHref()
                        ));
                    }
                }
            } catch (Exception ex) {
                log.error(ex);
            }

            return result;
        }
    }

    /**
     * URL suffix for displaying the report in 'user' interface.
     * @return
     */
    protected abstract String getHref();

    /**
     * Report's columns.
     * @return
     */
    public abstract Columns getColumns();

    /**
     * Supported charts. Position in the list identifies a chart.
     * @return
     */
    public List<Chart> getCharts() {
        return Collections.emptyList();
    };

    /**
     * JSP template for rendering the report.
     * @return
     */
    protected abstract String getJsp();

    /**
     * Selector for extracting the report's data from SQL.
     * @return
     */
    protected abstract Selector getSelector();

    /**
     * Wrapper class around {@link CommonDAO} for SQL work.
     */
    protected static abstract class Selector extends CommonDAO {
        protected abstract void select(ConnectionSet conSet, Data data) throws Exception;
    }

    @Override
    public ActionForward unspecified(DynActionForm form, ConnectionSet conSet)
            throws Exception {
        // TODO: Support export to CSV.

        // Data object places 'list' key to 'response.data' in the constructor call
        final var data = new Data(this, form, getColumns());
        // here 'data' key is placed in request attribute
        form.setRequestAttribute("data", data);

        // report specific data selection
        getSelector().select(conSet, data);

        charts(form, data);

        return html(conSet, form, getJsp());
    }

    /**
     * Prepare chart's data.
     * @param form
     * @param data
     * @throws BGIllegalArgumentException
     */
    private void charts(DynActionForm form, Data data) throws BGIllegalArgumentException {
        final var charts = getCharts();
        if (charts.isEmpty())
            return;

        int chartIndex = form.getParamInt("chartIndex");
        if (chartIndex <= 0)
             return;

        if (charts.size() < chartIndex)
            throw new BGIllegalArgumentException();

        final var chart = charts.get(chartIndex - 1);
        form.setResponseData("chart", chart.json(form.l, data));
    }
}
