package org.bgerp.plugin.report.model.chart;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bgerp.l10n.Localizer;
import org.bgerp.l10n.Titled;
import org.bgerp.plugin.report.model.Data;


/**
 * Base class for all charts.
 * <p> Names of sub-classes used in JSP templates. E.g. .name.endsWith('Bar')
 *
 * @author Shamil Vakhitov
 */
public abstract class Chart implements Titled {
    static final ObjectMapper MAPPER = new ObjectMapper();

    protected final String ltitle;

    protected Chart(String ltitle) {
        this.ltitle = ltitle;
    }

    @Override
    public String getTitle(Localizer l) {
        return l.l(ltitle);
    }

    /**
     * Generates JSON data for EChart on frontend.
     * @param l
     * @param data
     * @return
     */
    public abstract Object json(Localizer l, Data data);
}
