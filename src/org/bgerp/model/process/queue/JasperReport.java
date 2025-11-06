package org.bgerp.model.process.queue;

import java.awt.Color;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bgerp.model.process.queue.column.MediaColumn;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalTextAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalTextAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.type.StretchTypeEnum;
import net.sf.jasperreports.engine.type.TextAdjustEnum;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import ru.bgcrm.model.process.queue.Queue;
import ru.bgcrm.model.process.queue.config.PrintQueueConfig.PrintType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;

/**
 * PDF print form generator
 *
 * @author Shamil Vakhitov
 */
public class JasperReport {
    private static final Style DEFAULT_STYLE;
    private static final Style TITLE_STYLE;
    private static final Style HEADER_STYLE;
    private static final Style DESCRIPTION_STYLE;
    private static final Style ODD_ROW_STYLE;

    static {
        final var defaultFont = new Font();
        defaultFont.setFontSize(8);

        final var headerFont = new Font();
        headerFont.setFontSize(9);

        final var titleFont = new Font();
        titleFont.setFontSize(12);

        DEFAULT_STYLE = new Style("normal");
        DEFAULT_STYLE.setFont(defaultFont);
        DEFAULT_STYLE.SetTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        DEFAULT_STYLE.setStretchType(StretchTypeEnum.ELEMENT_GROUP_HEIGHT);
        DEFAULT_STYLE.setVerticalTextAlign(VerticalTextAlign.MIDDLE);
        DEFAULT_STYLE.setHorizontalTextAlign(HorizontalTextAlign.CENTER);

        DESCRIPTION_STYLE = new Style("description");
        DESCRIPTION_STYLE.setFont(defaultFont);
        DESCRIPTION_STYLE.SetTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        DESCRIPTION_STYLE.setStretchType(StretchTypeEnum.ELEMENT_GROUP_HEIGHT);
        DESCRIPTION_STYLE.setVerticalTextAlign(VerticalTextAlign.MIDDLE);
        DESCRIPTION_STYLE.setHorizontalTextAlign(HorizontalTextAlign.LEFT);

        HEADER_STYLE = new Style("header");
        HEADER_STYLE.setFont(headerFont);
        HEADER_STYLE.SetTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);
        HEADER_STYLE.setVerticalTextAlign(VerticalTextAlign.MIDDLE);
        HEADER_STYLE.setHorizontalTextAlign(HorizontalTextAlign.CENTER);
        HEADER_STYLE.setBackgroundColor(new Color(230, 230, 230));
        HEADER_STYLE.setTransparency(Transparency.OPAQUE);

        TITLE_STYLE = new Style("title");
        TITLE_STYLE.setFont(titleFont);
        TITLE_STYLE.SetTextAdjust(TextAdjustEnum.STRETCH_HEIGHT);

        ODD_ROW_STYLE = new Style();
        ODD_ROW_STYLE.setBorder(Border.NO_BORDER());
        ODD_ROW_STYLE.setBackgroundColor(new Color(235, 235, 235));
        ODD_ROW_STYLE.setTransparency(Transparency.OPAQUE);
    }

    public static void addPrintQueueDocumentToOutputStream(DynActionForm form, List<Object[]> data, Queue queue, PrintType printType,
            OutputStream ostream) throws Exception {
        FastReportBuilder drb = new FastReportBuilder();
        drb.setTitle(form.l.l("Очередь") + " : " + queue.getTitle())
                .setSubtitle(form.l.l("Дата генерации") + " " + TimeUtils.format(new Date(), TimeUtils.FORMAT_TYPE_YMD))
                .setPrintBackgroundOnOddRows(true).setOddRowBackgroundStyle(ODD_ROW_STYLE).setColumnsPerPage(1).setUseFullPageWidth(true)
                .setColumnSpace(5).setAllowDetailSplit(false).setMargins(0, 0, 0, 0)
                .setDefaultStyles(TITLE_STYLE, TITLE_STYLE, HEADER_STYLE, DEFAULT_STYLE);

        if (printType != null) {
            if (printType.getOrientation().equals(PrintType.ORIENTATION_LANDSCAPE)) {
                drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
            } else {
                drb.setPageSizeAndOrientation(Page.Page_A4_Portrait());
            }
        } else {
            drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
        }

        List<MediaColumn> printColumns = null;

        if (printType != null) {
            printColumns = queue.getMediaColumnList(printType.getColumnIds());

            List<Integer> widths = printType.getColumnWidths();

            for (int i = 0; i < printColumns.size(); i++) {
                MediaColumn col = printColumns.get(i);
                Integer width = widths.get(i);

                var column = ColumnBuilder.getNew().setColumnProperty(String.valueOf(col.getColumn().getId()), String.class.getName())
                        .setTitle(col.getColumn().getTitle()).setWidth(width).setStyle(DEFAULT_STYLE).setHeaderStyle(HEADER_STYLE).build();

                drb.addColumn(column);
            }
        } else {
            printColumns = queue.getMediaColumnList("print");

            for (MediaColumn col : printColumns) {
                int width = 50;
                Style style = DEFAULT_STYLE;
                if ("description".equals(col.getColumn().getValue())) {
                    width = 300;
                    style = DESCRIPTION_STYLE;
                }

                AbstractColumn column = ColumnBuilder.getNew().setColumnProperty(col.getColumn().getId(), String.class.getName())
                        .setTitle(col.getColumn().getTitle()).setWidth(width).setStyle(style).setHeaderStyle(HEADER_STYLE).build();

                drb.addColumn(column);
            }
        }

        final int size = printColumns.size();

        List<Map<String, Object>> jdata = new ArrayList<>();

        for (Object[] objects : data) {
            Map<String, Object> map = new HashMap<>(size);
            jdata.add(map);

            for (int i = 0; i < size; i++) {
                MediaColumn col = printColumns.get(i);
                map.put(String.valueOf(col.getColumn().getId()), objects[i]);
            }
        }

        DynamicReport dr = drb.build();

        JRDataSource ds = new JRBeanCollectionDataSource(jdata);
        JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jp));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(ostream));
        exporter.exportReport();
    }
}