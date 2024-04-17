package org.bgerp.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.file.SessionTemporaryFiles;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.FileData;
import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

@Action(path = "/user/file")
public class FileAction extends BaseAction {
    @Override
    public ActionForward unspecified(DynActionForm form, Connection con) throws Exception {
        var response = form.getHttpResponse();

        FileData data = new FileData();
        data.setId(form.getId());
        data.setSecret(form.getParam("secret"));
        data.setTitle(form.getParam("title"));

        File file = new FileDataDAO(con).getFile(data);
        if (file != null) {
            OutputStream out = response.getOutputStream();
            try (var fis = new FileInputStream(file)) {
                Utils.setFileNameHeaders(response, data.getTitle());
                IOUtils.copy(fis, out);
            }
            out.flush();
        }

        return null;
    }

    public ActionForward temporaryUpload(DynActionForm form, Connection con) throws BGMessageException {
        try {
            FormFile file = form.getFile();

            log.debug("Uploading temporary file: {}, type: {}", file.getFileName(), file.getContentType());

            uploadFileCheck(file);

            HttpSession session = form.getHttpRequest().getSession(true);

            SessionTemporaryFiles files = null;
            synchronized (this) {
                files = (SessionTemporaryFiles) session.getAttribute(SessionTemporaryFiles.STORE_KEY);
                if (files == null)
                    session.setAttribute(SessionTemporaryFiles.STORE_KEY, files = new SessionTemporaryFiles());
            }

            int fileId = files.fileIndex.incrementAndGet();

            String storeFileName = SessionTemporaryFiles.getStoreFilePath(session, fileId);

            FileOutputStream fos = new FileOutputStream(storeFileName);
            fos.write(file.getFileData());
            fos.close();

            files.fileTitleMap.put(fileId, file.getFileName());

            form.getResponse().setData("file", new IdTitle(fileId, file.getFileName()));
        } catch (IOException e) {
            throw new BGException(e);
        }

        return json(con, form);
    }

    public static void uploadFileCheck(FormFile file) throws FileNotFoundException, IOException, BGMessageException {
        if (file == null)
            return;

        final long maxSizeMb = Setup.getSetup().getLong("file.upload.max.size.mb", 3);
        if (maxSizeMb * 1000000L < file.getFileData().length)
            throw new BGMessageException("File '{}' is bigger than allowed {} MB", file.getFileName(), maxSizeMb);
    }

    public ActionForward temporaryDelete(DynActionForm form, Connection con) {
        log.debug("Deleting temporary file: {}", form.getId());

        HttpSession session = form.getHttpRequest().getSession(true);

        SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(SessionTemporaryFiles.STORE_KEY);
        if (files == null)
            throw new BGException("Not found tmp files.");

        int fileId = form.getId();

        String storeFileName = SessionTemporaryFiles.getStoreFilePath(session, fileId);

        new File(storeFileName).delete();
        files.fileTitleMap.remove(fileId);

        return json(con, form);
    }
}