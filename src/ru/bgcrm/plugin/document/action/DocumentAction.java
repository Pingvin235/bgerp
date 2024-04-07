package ru.bgcrm.plugin.document.action;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.bgerp.action.BaseAction;
import org.bgerp.action.FileAction;
import org.bgerp.app.bean.Bean;
import org.bgerp.app.event.iface.Event;
import org.bgerp.app.event.iface.EventListener;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ProcessTypeCache;
import org.bgerp.model.Pageable;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.plugin.document.Config;
import ru.bgcrm.plugin.document.Plugin;
import ru.bgcrm.plugin.document.dao.DocumentDAO;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.plugin.document.model.Document;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/document/document")
public class DocumentAction extends BaseAction {
    private static final String PATH_JSP = Plugin.PATH_JSP_USER;

    public ActionForward documentList(DynActionForm form, Connection con) throws Exception {
        Config config = setup.getConfig(Config.class);

        DocumentDAO documentDao = new DocumentDAO(con);

        String scope = form.getParam("scope");
        String objectType = form.getParam("objectType");
        String objectTitle = form.getParam("objectTitle");
        int objectId = Utils.parseInt(form.getParam("objectId"));

        documentDao.searchObjectDocuments(new Pageable<Document>(form), objectType, objectId);
        Collection<Pattern> patterns = config.getPatterns(scope, objectType, objectTitle);

        if (Process.OBJECT_TYPE.equals(scope)) {
            ProcessType type = ProcessTypeCache.getProcessType(new ProcessDAO(con).getProcess(objectId).getTypeId());
            Set<Integer> allowedPatternIds = Utils.toIntegerSet(
                    type.getProperties().getConfigMap().get("document:processCreateDocumentsAllowedTemplates"));

            patterns = patterns.stream().filter(p -> allowedPatternIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }

        form.getHttpRequest().setAttribute("patternList", patterns);

        return html(con, form, PATH_JSP + "/document_list.jsp");
    }

    public ActionForward uploadDocument(DynActionForm form, Connection con) throws Exception {
        String objectType = form.getParam("objectType");
        int objectId = Utils.parseInt(form.getParam("objectId"));
        FormFile file = form.getFile();

        FileAction.uploadFileCheck(file);

        new DocumentDAO(con).add(objectType, objectId, file.getFileData(), file.getFileName());

        return json(con, form);
    }

    public ActionForward deleteDocument(DynActionForm form, Connection con) throws Exception {
        DocumentDAO docDao = new DocumentDAO(con);

        Document doc = docDao.getDocumentById(form.getId());
        if (doc != null)
            docDao.delete(doc);

        return json(con, form);
    }

    @SuppressWarnings("unchecked")
    public ActionForward generateDocument(DynActionForm form, ConnectionSet conSet) throws Exception {
        Config config = setup.getConfig(Config.class);

        String scope = form.getParam("scope");
        String objectType = form.getParam("objectType");
        int objectId = Utils.parseInt(form.getParam("objectId"));
        int patternId = Utils.parseInt(form.getParam("patternId"));

        Pattern pattern = config.getPattern(scope, patternId);
        if (pattern == null) {
            throw new BGException("Patten not found.");
        }

        DocumentGenerateEvent event = new DocumentGenerateEvent(form, pattern, objectType, Collections.singletonList(objectId));
        ((EventListener<Event>) Bean.newInstance(pattern.getScript())).notify(event, conSet);

        Document document = event.getResultDocument();

        HttpServletResponse response = form.getHttpResponse();

        /*
         * Что-то уфанетоспецифичное. Вроде как сгенерировать и сразу открыть?
         */
        if ("document".equals(form.getResponseType())) {
            FileData fileData = new DocumentDAO(conSet.getConnection()).getDocumentById(document.getId()).getFileData();

            Utils.setFileNameHeaders(response, fileData.getTitle());

            OutputStream out = response.getOutputStream();
            IOUtils.copy(new FileInputStream(new FileDataDAO(conSet.getConnection()).getFile(fileData)), out);
            out.flush();

            return null;
        }

        // режим stream
        if (event.getResultBytes() != null) {
            // режим отладки
            if (event.isDebug()) {
                response.setContentType("text/plain; charset=" + StandardCharsets.UTF_8.name());
            } else if (pattern.getType() == Pattern.TYPE_JSP_HTML || pattern.getType() == Pattern.TYPE_XSLT_HTML) {
                response.setContentType("text/html; charset=" + StandardCharsets.UTF_8.name());
            } else {
                Utils.setFileNameHeaders(response, pattern.getDocumentTitle());
            }

            OutputStream out = response.getOutputStream();
            out.write(event.getResultBytes());
            out.flush();

            return null;
        } else {
            form.getResponse().setData("document", document);
            return json(conSet, form);
        }
    }

    @Override
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        return documentList(form, con);
    }
}
