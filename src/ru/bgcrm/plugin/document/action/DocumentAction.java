package ru.bgcrm.plugin.document.action;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import ru.bgcrm.cache.ProcessTypeCache;
import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.dao.process.ProcessDAO;
import ru.bgcrm.event.EventProcessor;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.SearchResult;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.plugin.document.Config;
import ru.bgcrm.plugin.document.Plugin;
import ru.bgcrm.plugin.document.dao.DocumentDAO;
import ru.bgcrm.plugin.document.event.DocumentGenerateEvent;
import ru.bgcrm.plugin.document.model.Document;
import ru.bgcrm.plugin.document.model.Pattern;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.action.BaseAction;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

@Action(path = "/user/plugin/document/document")
public class DocumentAction extends BaseAction {
    private static final String JSP_PATH = Plugin.PATH_JSP_USER;
    
    public ActionForward documentList(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        Config config = setup.getConfig(Config.class);

        DocumentDAO documentDao = new DocumentDAO(con);

        String scope = form.getParam("scope");
        String objectType = form.getParam("objectType");
        String objectTitle = form.getParam("objectTitle");
        int objectId = Utils.parseInt(form.getParam("objectId"));

        documentDao.searchObjectDocuments(new SearchResult<Document>(form), objectType, objectId);
        Collection<Pattern> patterns = config.getPatterns(scope, objectType, objectTitle);

        if (Process.OBJECT_TYPE.equals(scope)) {
            ProcessType type = ProcessTypeCache.getProcessType(new ProcessDAO(con).getProcess(objectId).getTypeId());
            Set<Integer> allowedPatternIds = Utils.toIntegerSet(
                    type.getProperties().getConfigMap().get("document:processCreateDocumentsAllowedTemplates"));

            patterns = patterns.stream().filter(p -> allowedPatternIds.contains(p.getId()))
                    .collect(Collectors.toList());
        }

        form.getHttpRequest().setAttribute("patternList", patterns);

        return data(con, form, JSP_PATH + "/document_list.jsp");
    }

    public ActionForward uploadDocument(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        String objectType = form.getParam("objectType");
        int objectId = Utils.parseInt(form.getParam("objectId"));
        FormFile file = form.getFile();

        new DocumentDAO(con).add(objectType, objectId, file.getFileData(), file.getFileName());

        return status(con, form);
    }

    public ActionForward deleteDocument(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        DocumentDAO docDao = new DocumentDAO(con);

        Document doc = docDao.getDocumentById(form.getId());
        if (doc != null)
            docDao.delete(doc);

        return status(con, form);
    }

    public ActionForward generateDocument(ActionMapping mapping, DynActionForm form, ConnectionSet conSet) throws Exception {
        Config config = setup.getConfig(Config.class);

        String scope = form.getParam("scope");
        String objectType = form.getParam("objectType");
        int objectId = Utils.parseInt(form.getParam("objectId"));
        int patternId = Utils.parseInt(form.getParam("patternId"));

        Pattern pattern = config.getPattern(scope, patternId);
        if (pattern == null) {
            throw new BGException("Не найден шаблон.");
        }

        DocumentGenerateEvent event = new DocumentGenerateEvent(form, pattern, objectType,
                Collections.singletonList(objectId));
        if (!EventProcessor.processEvent(event, pattern.getScript(), conSet)) {
            throw new BGException("Не найден класс генерации.");
        }

        Document document = event.getResultDocument();
        
        HttpServletResponse response = form.getHttpResponse();

        /*
         * Что-то уфанетоспецифичное. Вроде как сгенерировать и сразу открыть?
         */
        if ("document".equals(form.getResponseType())) {
            FileData fileData = new DocumentDAO(conSet.getConnection()).getDocumentById(document.getId()).getFileData();

            Utils.setFileNameHeades(response, fileData.getTitle());

            OutputStream out = response.getOutputStream();
            IOUtils.copy(new FileInputStream(new FileDataDAO(conSet.getConnection()).getFile(fileData)), out);
            out.flush();

            return null;
        }

        // режим stream
        if (event.getResultBytes() != null) {
            // режим отладки
            if (event.isDebug()) {
                response.setContentType("text/plain; charset=" + Utils.UTF8.name());
            } else if (pattern.getType() == Pattern.TYPE_JSP_HTML || pattern.getType() == Pattern.TYPE_XSLT_HTML) {
                response.setContentType("text/html; charset=" + Utils.UTF8.name());
            } else {
                Utils.setFileNameHeades(response, pattern.getDocumentTitle());
            }

            OutputStream out = response.getOutputStream();
            out.write(event.getResultBytes());
            out.flush();

            return null;
        } else {
            form.getResponse().setData("document", document);
            return status(conSet, form);
        }
    }

    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm actionForm, Connection con) throws Exception {
        return documentList(mapping, actionForm, con);
    }
}
