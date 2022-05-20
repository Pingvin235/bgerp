package org.bgerp.plugin.report.model.chart;

import org.bgerp.l10n.Localizer;
import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Data;

public class ChartPie extends Chart2D {
    public ChartPie(String ltitle, Column categories, Column values) {
        super(ltitle, categories, values);
    }

    /**
     * Example of data:
     * https://echarts.apache.org/examples/en/editor.html?c=pie-borderRadius
     */
    @Override
    public Object json(Localizer l, Data data) {
        final var chart = MAPPER.createObjectNode();

        chart.putObject("tooltip")/* .put("trigger", "item") */;
        chart.putObject("legend").put("top", "5%").put("left", "center");

        final var series = chart.putObject("series");
        chart.putObject("title").put("text", getTitle(l));
        series
            .put("type", "pie")
            .putArray("radius").add("40%").add("70%");
        series.put("avoidLabelOverlap", false);
        series.putObject("itemStyle")
            .put("borderRadius", 10).put("borderColor", "#fff").put("borderWidth", 2);
        series.putObject("label")
            .put("show", false).put("position", "center");
        series.putObject("emphasis").putObject("label")
            .put("show", true).put("fontSize", "1.2em").put("fontWeight", "bold");
        series.putObject("labelLine").put("show", false);

        final var seriesData = series.putArray("data");

        for (var me : prepareData(data).entrySet()) {
            seriesData.addObject()
                .put("name", me.getKey()).put("value", me.getValue());
        }

        return chart;
    }
}
