package org.bgerp.plugin.report.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.struts.action.ActionForward;
import org.bgerp.action.TitledAction;
import org.bgerp.action.TitledActionFactory;
import org.bgerp.plugin.report.model.Chart;
import org.bgerp.plugin.report.model.Columns;
import org.bgerp.plugin.report.model.Data;
import org.reflections.Reflections;

import ru.bgcrm.dao.CommonDAO;
import ru.bgcrm.plugin.Plugin;
import ru.bgcrm.plugin.PluginManager;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.sql.ConnectionSet;
import ru.bgerp.l10n.Localizer;
import ru.bgerp.l10n.Titled;
import ru.bgerp.util.Log;

/**
 * Parent action for all reports.
 *
 * @author Shamil Vakhitov
 */
public abstract class ReportActionBase extends BaseAction implements Titled {

    /**
     * Selects children classes of {@link ReportActionBase} over all plugins.
     * <p> Used for generation permission actions list and UI menu items.
     * <p> To see usages search for 'org.bgerp.plugin.report.action.ReportActionBase$Factory' in project's files.
     */
    public static final class Factory implements TitledActionFactory {
        private static final Log log = Log.getLog();

        @Override
        public List<TitledAction> create(Localizer l) {
            List<TitledAction> result = new ArrayList<>();

            try {
                for (Plugin p : PluginManager.getInstance().getPluginList()) {
                    var r = new Reflections(p.getClass().getPackageName());
                    for (Class<?> reportClass : r.getSubTypesOf(ReportActionBase.class)) {
                        var reportInstance = (ReportActionBase) reportClass.getConstructor().newInstance();
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
    protected abstract Columns getColumns();

    /**
     * Supported charts.
     * @return
     */
    public Set<Chart> getCharts() {
        return Collections.emptySet();
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

        getSelector().select(conSet, data);

        return html(conSet, form, getJsp());
    }
}
