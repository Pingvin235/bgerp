package org.bgerp.model.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.bgerp.app.exception.BGException;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class SessionTemporaryFiles {
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
                        throw new BGException("Incorrect ID of tmp file: " + tmpFileId);
                    }

                    String path = SessionTemporaryFiles.getStoreFilePath(session, tmpFileIdInt);
                    String fileTitle = files.fileTitleMap.get(tmpFileIdInt);
                    if (Utils.isBlankString(fileTitle)) {
                        throw new BGException("Undefined title for file with ID: " + tmpFileIdInt);
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