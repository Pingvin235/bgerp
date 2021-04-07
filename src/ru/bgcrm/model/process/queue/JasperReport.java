package ru.bgcrm.model.process.queue;

import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Stretching;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import ru.bgcrm.model.process.Queue;
import ru.bgcrm.model.process.Queue.ColumnConf;
import ru.bgcrm.model.process.queue.config.PrintQueueConfig.PrintType;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.TimeUtils;

public class JasperReport {
    private static Font defaultFont;
    private static Font headerFont;
    private static Font titleFont;
    private static Style defaultStyle;
    private static Style titleStyle;
    private static Style headerStyle;
    private static Style descriptionStyle;
    private static Style oddRowStyle;

    public JasperReport() throws IOException {
        String fontPath = "jar:" + this.getClass().getResource( "/ru/bitel/fonts/arial.ttf" ).getPath();

        defaultFont = new Font( 8, "Serif", fontPath,
                                Font.PDF_ENCODING_CP1251_Cyrillic, true );

        headerFont = new Font( 9, "Serif", fontPath,
                               Font.PDF_ENCODING_CP1251_Cyrillic, true );

        titleFont = new Font( 12, "Serif", fontPath,
                              Font.PDF_ENCODING_CP1251_Cyrillic, true );

        defaultStyle = new Style( "normalStyle" );
        defaultStyle.setStretchWithOverflow( true );
        defaultStyle.setStreching( Stretching.RELATIVE_TO_TALLEST_OBJECT );
        defaultStyle.setVerticalAlign( VerticalAlign.MIDDLE );
        defaultStyle.setHorizontalAlign( HorizontalAlign.CENTER );
        defaultStyle.setFont( defaultFont );

        headerStyle = new Style( "headerStyle" );
        headerStyle.setStretchWithOverflow( true );
        headerStyle.setVerticalAlign( VerticalAlign.MIDDLE );
        headerStyle.setHorizontalAlign( HorizontalAlign.CENTER );
        headerStyle.setBackgroundColor( new Color( 230, 230, 230 ) );
        headerStyle.setTransparency( Transparency.OPAQUE );
        headerStyle.setFont( headerFont );

        descriptionStyle = new Style( "descriptionStyle" );
        descriptionStyle.setStretchWithOverflow( true );
        descriptionStyle.setStreching( Stretching.RELATIVE_TO_TALLEST_OBJECT );
        descriptionStyle.setVerticalAlign( VerticalAlign.MIDDLE );
        descriptionStyle.setHorizontalAlign( HorizontalAlign.LEFT );
        descriptionStyle.setFont( defaultFont );

        titleStyle = new Style( "titleStyle" );
        titleStyle.setStretchWithOverflow( true );
        titleStyle.setFont( titleFont );

        oddRowStyle = new Style();
        oddRowStyle.setBorder( Border.NO_BORDER() );
        oddRowStyle.setBackgroundColor( new Color( 235, 235, 235 ) );
        oddRowStyle.setTransparency( Transparency.OPAQUE );
    }

    public void addPrintQueueDocumentToOutputStream(DynActionForm form, List<Object[]> data, Queue queue, PrintType printType,
            OutputStream ostream) throws Exception {
        FastReportBuilder drb = new FastReportBuilder();
        drb.setTitle(form.l.l("Очередь") + " : " + queue.getTitle() )
           .setSubtitle(form.l.l("Дата генерации") + " " + TimeUtils.format(new Date(), TimeUtils.FORMAT_TYPE_YMD))
           .setPrintBackgroundOnOddRows( true )
           .setOddRowBackgroundStyle( oddRowStyle )
           .setColumnsPerPage( 1 )
           .setUseFullPageWidth( true )
           .setColumnSpace( 5 )
           .setAllowDetailSplit( false )
           .setMargins( 0, 0, 0, 0 )
           .setDefaultStyles( titleStyle, titleStyle, headerStyle, defaultStyle );

        if (printType != null) {
            if (printType.getOrientation().equals(PrintType.ORIENTATION_LANDSCAPE)) {
                drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
            } else {
                drb.setPageSizeAndOrientation(Page.Page_A4_Portrait());
            }
        } else {
            drb.setPageSizeAndOrientation(Page.Page_A4_Landscape());
        }

        List<ColumnConf> printColumns = null;

        if (printType != null) {
            printColumns = queue.getColumnConfList(printType.getColumnIds());

            List<Integer> widths = printType.getColumnWidths();

            for (int i = 0; i < printColumns.size(); i++) {
                ColumnConf col = printColumns.get( i );
                Integer width = widths.get( i );

                var column = ColumnBuilder.getNew()
                        .setColumnProperty(String.valueOf(col.getColumnId()), String.class.getName())
                        .setTitle(col.getColumnConf().get("title"))
                        .setWidth(width)
                        .setStyle(defaultStyle)
                        .setHeaderStyle(headerStyle).build();

                drb.addColumn( column );
            }
        } else {
            printColumns = queue.getMediaColumnList("print");

            for (ColumnConf col : printColumns) {
                int width = 50;
                Style style = defaultStyle;
                if ("description".equals(col.getColumnConf().get("value"))) {
                    width = 300;
                    style = descriptionStyle;
                }

                AbstractColumn column = ColumnBuilder.getNew()
                        .setColumnProperty(String.valueOf(col.getColumnId()), String.class.getName())
                        .setTitle(col.getColumnConf().get("title"))
                        .setWidth(width)
                        .setStyle(style)
                        .setHeaderStyle(headerStyle)
                        .build();

                drb.addColumn(column);
            }
        }

        final int size = printColumns.size();

        List<Map<String, Object>> jdata = new ArrayList<Map<String, Object>>();

        for (Object[] objects : data) {
            Map<String, Object> map = new HashMap<String, Object>(size);
            jdata.add(map);

            for (int i = 0; i < size; i++) {
                ColumnConf col = printColumns.get(i);
                map.put(String.valueOf(col.getColumnId()), objects[i]);
            }
        }

        DynamicReport dr = drb.build();

        JRDataSource ds = new JRBeanCollectionDataSource(jdata);
        JasperPrint jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);

        JRExporter exporter = new JRPdfExporter();
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
        exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, ostream);
        exporter.setParameter(JRExporterParameter.CHARACTER_ENCODING, Font.PDF_ENCODING_CP1251_Cyrillic);
        exporter.exportReport();
    }
}