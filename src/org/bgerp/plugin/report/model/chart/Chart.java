package org.bgerp.plugin.report.model.chart;

import org.bgerp.app.l10n.Localizer;
import org.bgerp.app.l10n.Titled;
import org.bgerp.plugin.report.model.Data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


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

    /**
     * Adds array 'color' with 138 different HEX colors.
     * @param series parent object.
     */
    protected void colors(final ObjectNode series) {
        series.putArray("color")
                .add("#5470c6").add("#91cc75").add("#fac858").add("#ee6666").add("#73c0de").add("#3ba272").add("#fc8452").add("#9a60b4")
                .add("#ea7ccc").add("#cd9575").add("#fdd9b5").add("#78dbe2").add("#87a96b").add("#ffa474").add("#fae7b5").add("#9f8170")
                .add("#fd7c6e").add("#ace5ee").add("#1f75fe").add("#a2a2d0").add("#6699cc").add("#0d98ba").add("#7366bd").add("#de5d83")
                .add("#cb4154").add("#b4674d").add("#ff7f49").add("#ea7e5d").add("#b0b7c6").add("#ffff99").add("#1cd3a2").add("#ffaacc")
                .add("#dd4492").add("#1dacd6").add("#bc5d58").add("#dd9475").add("#9aceeb").add("#ffbcd9").add("#fddb6d").add("#2b6cc4")
                .add("#efcdb8").add("#6e5160").add("#ceff1d").add("#71bc78").add("#6dae81").add("#c364c5").add("#cc6666").add("#e7c697")
                .add("#fcd975").add("#a8e4a0").add("#95918c").add("#1cac78").add("#1164b4").add("#f0e891").add("#ff1dce").add("#b2ec5d")
                .add("#5d76cb").add("#ca3767").add("#3bb08f").add("#fefe22").add("#fcb4d5").add("#fff44f").add("#ffbd88").add("#f664af")
                .add("#aaf0d1").add("#cd4a4c").add("#edd19c").add("#979aaa").add("#ff8243").add("#c8385a").add("#ef98aa").add("#fdbcb4")
                .add("#1a4876").add("#30ba8f").add("#c54b8c").add("#1974d2").add("#ffa343").add("#bab86c").add("#ff7538").add("#ff2b2b")
                .add("#f8d568").add("#e6a8d7").add("#414a4c").add("#ff6e4a").add("#1ca9c9").add("#ffcfab").add("#c5d0e6").add("#fddde6")
                .add("#158078").add("#fc74fd").add("#f78fa7").add("#8e4585").add("#7442c8").add("#9d81ba").add("#fe4eda").add("#ff496c")
                .add("#d68a59").add("#714b23").add("#ff48d0").add("#e3256b").add("#ee204d").add("#ff5349").add("#c0448f").add("#1fcecb")
                .add("#7851a9").add("#ff9baa").add("#fc2847").add("#76ff7a").add("#9fe2bf").add("#a5694f").add("#8a795d").add("#45cea2")
                .add("#fb7efd").add("#cdc5c2").add("#80daeb").add("#eceabe").add("#ffcf48").add("#fd5e53").add("#faa76c").add("#18a7b5")
                .add("#ebc7df").add("#fc89ac").add("#dbd7d2").add("#17806d").add("#deaa88").add("#77dde7").add("#ffff66").add("#926eae")
                .add("#324ab2").add("#f75394").add("#ffa089").add("#8f509d").add("#ff43a4").add("#fc6c85").add("#cda4de").add("#fce883")
                .add("#c5e384").add("#ffae42");
    }
}
