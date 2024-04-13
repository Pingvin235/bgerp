package org.bgerp.plugin.report.model.chart;

import org.bgerp.app.l10n.Localizer;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Data;

public class ChartPie extends Chart2D {
    public ChartPie(String ltitle, Column categories) {
        super(ltitle, categories);
    }

    /**
     * Example of data:
     * https://echarts.apache.org/examples/en/editor.html?c=pie-borderRadius
     */
    @Override
    public Object json(Localizer l, Data data) {
        final var chart = MAPPER.createObjectNode();

        chart.putObject("tooltip");
        chart.putObject("legend")
            .put("top", "5%")
            .put("left", "center");
        chart.putObject("title")
            .put("text", getTitle(l));

        final var series = chart.putObject("series");

        colors(series);

        series
            .put("type", "pie")
            .put("radius", "40%")
            .put("avoidLabelOverlap", false);
        series.putObject("label")
            .put("show", true)
            .put("formatter", "{b}: {c} ({d}%)");
        series.putObject("emphasis").putObject("label")
            .put("show", true)
            .put("fontSize", "1.2em")
            .put("fontWeight", "bold");
        series.putObject("labelLine")
            .put("show", true);

        final var seriesData = series.putArray("data");

        for (var me : prepareData(data).entrySet()) {
            seriesData.addObject()
                .put("name", me.getKey())
                .put("value", me.getValue());
        }

        return chart;
    }
}
