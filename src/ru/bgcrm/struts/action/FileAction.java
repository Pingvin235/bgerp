package ru.bgcrm.struts.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.model.BGException;
import ru.bgcrm.model.FileData;
import ru.bgcrm.model.IdTitle;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class FileAction extends BaseAction {
    @Override
    protected ActionForward unspecified(ActionMapping mapping, DynActionForm form, Connection con) throws Exception {
        var response = form.getHttpResponse();

        FileData data = new FileData();
        data.setId(form.getId());
        data.setSecret(form.getParam("secret"));
        data.setTitle(form.getParam("title"));

        File file = new FileDataDAO(con).getFile(data);
        if (file != null) {
            OutputStream out = response.getOutputStream();

            Utils.setFileNameHeades(response, data.getTitle());
            IOUtils.copy(new FileInputStream(file), out);

            out.flush();
        }
        return null;
    }

    public ActionForward temporaryUpload(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        try {
            FormFile file = form.getFile();

            if (log.isDebugEnabled()) {
                log.debug("Uploading temporary file: " + file.getFileName() + ", type: " + file.getContentType());
            }

            SessionTemporaryFiles files = null;

            HttpSession session = form.getHttpRequest().getSession(true);

            synchronized (this) {
                files = (SessionTemporaryFiles) session.getAttribute(SessionTemporaryFiles.STORE_KEY);
                if (files == null) {
                    session.setAttribute(SessionTemporaryFiles.STORE_KEY, files = new SessionTemporaryFiles());
                }
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

        return processJsonForward(con, form);
    }

    public ActionForward temporaryDelete(ActionMapping mapping, DynActionForm form, Connection con) throws BGException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting temporary file: " + form.getId());
        }

        HttpSession session = form.getHttpRequest().getSession(true);
        SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(SessionTemporaryFiles.STORE_KEY);

        if (files == null) {
            throw new BGException("Не найдены временные файлы.");
        }

        int fileId = form.getId();

        String storeFileName = SessionTemporaryFiles.getStoreFilePath(session, fileId);

        new File(storeFileName).delete();
        files.fileTitleMap.remove(fileId);

        return processJsonForward(con, form);
    }

    public static class SessionTemporaryFiles {
        public static final String STORE_KEY = "SessionTemporaryFiles";

        public AtomicInteger fileIndex = new AtomicInteger(1);
        public Map<Integer, String> fileTitleMap = new ConcurrentHashMap<Integer, String>();

        public static String getStoreFilePath(HttpSession session, int fileId) {
            return Utils.getTmpDir() + "/" + session.getId() + "-" + fileId;
        }

        public static Map<Integer, FileInfo> getFiles(DynActionForm form, String paramName) throws BGException {
            Map<Integer, FileInfo> result = new HashMap<Integer, FileInfo>();

            if (form.getHttpRequest() != null) {
                HttpSession session = form.getHttpRequest().getSession(true);
                SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(STORE_KEY);

                String[] tmpFileIds = form.getParamArray("tmpFileId");
                if (tmpFileIds != null) {
                    for (String tmpFileId : tmpFileIds) {
                        int tmpFileIdInt = Utils.parseInt(tmpFileId);
                        if (tmpFileIdInt <= 0) {
                            throw new BGException("Неверный код временного файла.");
                        }

                        String path = SessionTemporaryFiles.getStoreFilePath(session, tmpFileIdInt);
                        String fileTitle = files.fileTitleMap.get(tmpFileIdInt);
                        if (Utils.isBlankString(fileTitle)) {
                            throw new BGException("Не определено название файла с кодом: " + tmpFileIdInt);
                        }

                        try {
                            result.put(tmpFileIdInt, new FileInfo(fileTitle, new FileInputStream(path)));
                        } catch (FileNotFoundException e) {
                            throw new BGException(e);
                        }
                    }
                }
            }

            return result;
        }

        public static void deleteFiles(DynActionForm form, Set<Integer> ids) {
            if (form.getHttpRequest() != null) {
                HttpSession session = form.getHttpRequest().getSession(true);
                SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(STORE_KEY);

                // после успешного добавления в БД - удаление временных файлов
                for (int tmpFileId : ids) {
                    files.fileTitleMap.remove(tmpFileId);

                    String path = SessionTemporaryFiles.getStoreFilePath(session, tmpFileId);
                    new File(path).delete();
                }
            }
        }
    }

    public static class FileInfo {
        public String title;
        public FileInputStream inputStream;

        public FileInfo(String title, FileInputStream inputStream) {
            this.title = title;
            this.inputStream = inputStream;
        }
    }
}