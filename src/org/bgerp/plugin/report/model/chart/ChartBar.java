package org.bgerp.plugin.report.model.chart;

import org.bgerp.plugin.report.model.Column;
import org.bgerp.plugin.report.model.Data;

import ru.bgerp.l10n.Localizer;

public class ChartBar extends Chart2D {
    public ChartBar(String ltitle, Column categories, Column values) {
        super(ltitle, categories, values);
    }

    /**
     * Example of data:
     * https://echarts.apache.org/examples/en/editor.html?c=bar-simple
     */
    @Override
    public Object json(Localizer l, Data data) {
        final var chart = MAPPER.createObjectNode();

        // single category bar chart with count bars
        chart.putObject("title").put("text", getTitle(l));
        chart.putObject("tooltip");

        var xData = chart.putObject("xAxis").putArray("data");

        chart.putObject("yAxis");
            
        var series = chart.putArray("series").addObject();
        var seriesData = series
            .put("type", "bar")
            .putArray("data");
        
        for (var me : prepareData(data).entrySet()) {
            xData.add(me.getKey());
            seriesData.add(me.getValue());
        }

        return chart;
    }
}
