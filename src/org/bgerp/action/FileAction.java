package org.bgerp.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.upload.FormFile;
import org.bgerp.action.base.BaseAction;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.dao.FileDataDAO;
import org.bgerp.model.base.IdTitle;
import org.bgerp.model.file.FileData;
import org.bgerp.model.file.tmp.SessionTemporaryFiles;

import ru.bgcrm.servlet.ActionServlet.Action;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.ConnectionSet;

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

    public ActionForward temporaryUpload(DynActionForm form, ConnectionSet conSet) throws Exception {
        FormFile file = form.getFile();

        log.debug("Uploading temporary file: {}, type: {}", file.getFileName(), file.getContentType());

        uploadFileCheck(file);

        int fileId = SessionTemporaryFiles.upload(form);

        form.setResponseData("file", new IdTitle(fileId, file.getFileName()));

        return json(conSet, form);
    }

    public static void uploadFileCheck(FormFile file) throws FileNotFoundException, IOException, BGMessageException {
        if (file == null)
            return;

        final long maxSizeMb = Setup.getSetup().getLong("file.upload.max.size.mb", 3);
        if (maxSizeMb * 1000000L < file.getFileData().length)
            throw new BGMessageException("File '{}' is bigger than allowed {} MB", file.getFileName(), maxSizeMb);
    }

    public ActionForward temporaryDelete(DynActionForm form, ConnectionSet conSet) {
        log.debug("Deleting temporary file: {}", form.getId());

        SessionTemporaryFiles.deleteFiles(form, Set.of(form.getId()));

        return json(conSet, form);
    }
}