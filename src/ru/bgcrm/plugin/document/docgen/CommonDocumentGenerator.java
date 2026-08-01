package ru.bgcrm.plugin.document.docgen;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.lf5.util.StreamUtils;
import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.servlet.util.ServletResponseInterceptor;
import org.bgerp.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import ru.bgcrm.model.Pair;
import ru.bgcrm.plugin.document.dao.DocumentDAO;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.RegexpStringUtils;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

public class CommonDocumentGenerator implements EventListener<Event> {
    private static final Logger log = Logger.getLogger(CommonDocumentGenerator.class);

    protected ConnectionSet conSet;
    protected DocumentGenerateEvent event;

    public CommonDocumentGenerator() {
    }

    public CommonDocumentGenerator(Event e, ConnectionSet conSet) {
        this.event = (DocumentGenerateEvent) e;
        this.conSet = conSet;
    }

    @Override
    public void notify(Event e, ConnectionSet conSet) {
        this.event = (DocumentGenerateEvent) e;
        this.conSet = conSet;

        try {
            Pattern pattern = event.getPattern();

            ByteArrayOutputStream result = new ByteArrayOutputStream(1000000);

            // the document contains the XSLT transformation result, if there is a template
            final int type = pattern.getType();
            if (event.isDebug()) {
                OutputStreamWriter writer = new OutputStreamWriter(result, StandardCharsets.UTF_8);
                writer.write("Генерация отчёта в режиме отладки.\n\n");

                writer.write("Событие, переданное в скрипт:\n");
                writer.write(event.toString());
                writer.write("\n\n");

                writer.write("Шаблон: ");
                writer.write(pattern.toString());

                if (type == Pattern.TYPE_JSP_HTML || type == Pattern.TYPE_XSLT_HTML) {
                    writer.write("\n\nДля данных типов отладка может производится в результат.");
                    writer.flush();
                } else {
                    int objectId = event.getObjectId();

                    writer.write("\n\n");
                    writer.write("Код объекта: ");
                    writer.write(String.valueOf(objectId));
                    writer.write("\n\n");

                    try {
                        Pair<Document, String> processResult = processToDocument(objectId);

                        writer.write("Подготовленные данные:\n");

                        writer.flush();
                        XMLUtils.serialize(processResult.getFirst(), result, null, true);

                        if (processResult.getSecond() != null) {
                            writer.write("\n\n");
                            writer.write("Отладка:\n");

                            writer.flush();
                            writer.write(processResult.getSecond());
                        }
                    } catch (Exception ex) {
                        writer.write("Ошибка выполнения:\n");
                        writer.flush();
                        ex.printStackTrace(new PrintStream(result, true, StandardCharsets.UTF_8.name()));

                        log.error(ex.getMessage(), ex);
                    }
                }

                writer.flush();

            } else if (type == Pattern.TYPE_PDF_FORM) {
                Document docData = processToDocument(event.getObjectId()).getFirst();

                final BaseFont bf = BaseFont.createFont("jar:" + this.getClass().getResource("/ru/bitel/fonts/arial.ttf").getPath(),
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                PdfReader reader = new PdfReader(pattern.getFile());
                PdfStamper stamper = new PdfStamper(reader, result);

                AcroFields fields = stamper.getAcroFields();
                fields.addSubstitutionFont(bf);

                for (String key : fields.getFields().keySet()) {
                    if (docData != null && docData.getDocumentElement() != null) {
                        String value = XMLUtils.selectText(docData.getDocumentElement(), "/data/field[@name='" + key + "']/text()");
                        if (Utils.notBlankString(value)) {
                            fields.setField(key, value);
                        }
                    }
                }

                if (pattern.getParams().getBoolean("flattening", false)) {
                    stamper.setFormFlattening(true);
                }

                stamper.close();
            } else if (type == Pattern.TYPE_DOCX_FORM || type == Pattern.TYPE_ODT_FORM) {
                Document docData = processToDocument(event.getObjectId()).getFirst();

                ZipInputStream zis = new ZipInputStream(new FileInputStream(pattern.getFile()));
                ZipOutputStream zos = new ZipOutputStream(result);

                Set<String> replacedFileSet = new HashSet<>();
                if (type == Pattern.TYPE_DOCX_FORM) {
                    replacedFileSet.add("word/document.xml");
                } else
                // TYPE_ODT_FORM
                {
                    replacedFileSet.add("content.xml");
                    replacedFileSet.add("styles.xml");
                }

                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    if (!replacedFileSet.contains(ze.getName())) {
                        zos.putNextEntry(new ZipEntry(ze.getName()));
                        StreamUtils.copy(zis, zos, 1024);
                    } else {
                        String replaced = new String(StreamUtils.getBytes(zis), StandardCharsets.UTF_8);

                        //search for templates like ${macros}
                        List<String> variables = RegexpStringUtils.findMatchesByTemplate(replaced, "\\$\\{[a-zA-Zа-яА-Я0-9_.]+\\}");
                        for (String var : variables) {
                            var = var.substring(2, var.length() - 1);
                            String value = "";
                            value = XMLUtils.selectText(docData.getDocumentElement(), "/data/field[@name='" + var + "']/text()", "");
                            replaced = replaced.replaceAll("\\$\\{" + var + "\\}", value);
                        }

                        zos.putNextEntry(new ZipEntry(ze.getName()));
                        zos.write(replaced.getBytes(StandardCharsets.UTF_8));
                    }
                }

                zos.close();
                zis.close();
            } else if (type == Pattern.TYPE_XSLT_HTML) {
                // otherwise - need to glue together the obtained HTML documents
                // not entirely correct, but backward compatible path
                for (int objectId : event.getObjectIds()) {
                    XMLUtils.serialize(processToDocument(objectId).getFirst(), result, null, true);
                }
            } else if (type == Pattern.TYPE_JSP_HTML) {
                // the template itself must handle objectIds
                ServletResponseInterceptor resp = new ServletResponseInterceptor(event.getForm().getHttpResponse(), result);

                HttpServletRequest req = event.getForm().getHttpRequest();

                req.setAttribute("event", event);
                req.setAttribute("conSlave", conSet.getSlaveConnection());

                req.getRequestDispatcher(pattern.getJsp()).include(req, resp);
                resp.flush();
            }

            // document creation, if needed
            if (!DynActionForm.RESPONSE_TYPE_STREAM.equals(event.getForm().getResponseType())) {
                DocumentDAO docDao = new DocumentDAO(conSet.getConnection());
                OutputStream out = docDao.createDocumentFile(event.setResultDocument(), pattern.getDocumentTitle());

                out.write(result.toByteArray());
                out.flush();
                out.close();
            } else {
                event.setResultBytes(result.toByteArray());
            }
        } catch (Exception ex) {
            throw new BGException(ex);
        }
    }

    private Pair<Document, String> processToDocument(int objectId) throws Exception {
        Pair<Document, String> result = new Pair<>();

        result.setFirst(XMLUtils.newDocument());

        Pattern pattern = event.getPattern();

        // JSP template is used for data preparation, data is formed by the field object
        // and the template output is used for debugging
        if (Utils.notBlankString(pattern.getJsp())) {
            Element dataElement = XMLUtils.newElement(result.getFirst(), "data");

            ByteArrayOutputStream bos = new ByteArrayOutputStream(200);

            ServletResponseInterceptor resp = new ServletResponseInterceptor(event.getForm().getHttpResponse(), bos);

            HttpServletRequest req = event.getForm().getHttpRequest();

            req.setAttribute("event", event);
            req.setAttribute("objectId", objectId);
            req.setAttribute("conSlave", conSet.getSlaveConnection());
            req.setAttribute("field", new FieldSetter(dataElement));

            req.getRequestDispatcher(pattern.getJsp()).include(req, resp);
            resp.flush();

            result.setSecond(new String(bos.toByteArray(), StandardCharsets.UTF_8));
        } else {
            throw new BGException("JSP for pattern is not defined.");
        }

        return result;
    }

    public static final class FieldSetter {
        private final Element dataNode;

        private FieldSetter(Element rootNode) {
            this.dataNode = rootNode;
        }

        public void set(String field, String value) {
            Element fieldEl = XMLUtils.newElement(dataNode, "field");
            fieldEl.setAttribute("name", field);
            XMLUtils.createTextNode(fieldEl, value);
        }
    }

}