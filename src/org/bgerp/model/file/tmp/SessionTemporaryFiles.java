package org.bgerp.model.file.tmp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.apache.struts.upload.FormFile;
import org.bgerp.app.exception.BGException;
import org.bgerp.app.exception.BGMessageException;

import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

/**
 * Temporary files not persisted in the regular file storage
 *
 * @author Shamil Vakhitov
 */
public class SessionTemporaryFiles {
    private static final String STORE_KEY = "SessionTemporaryFiles";

    /**
     * Upload tmp file
     * @param form
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static int upload(DynActionForm form) throws FileNotFoundException, IOException {
        HttpSession session = form.getHttpRequest().getSession(true);

        SessionTemporaryFiles files = null;
        synchronized (session) {
            files = (SessionTemporaryFiles) session.getAttribute(SessionTemporaryFiles.STORE_KEY);
            if (files == null)
                session.setAttribute(SessionTemporaryFiles.STORE_KEY, files = new SessionTemporaryFiles());
        }

        FormFile file = form.getFile();
        final String digest = Utils.getDigest(file.getFileData());

        final int fileId = files.fileIndex.incrementAndGet();

        final String path = Utils.getTmpDir() + "/" + session.getId() + "-" + fileId;

        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(file.getFileData());

        }
        files.files.put(fileId, new FileInfo(file.getFileName(), path, digest));

        return fileId;
    }

    /**
     * Uploaded tmp files for persisting
     * @param form the form
     * @param paramName the http request param name
     * @return
     * @throws BGMessageException duplicated tmp files were found
     */
    public static Map<Integer, FileInfo> getFiles(DynActionForm form, String paramName) throws BGMessageException {
        Map<Integer, FileInfo> result = new HashMap<>();

        Set<String> digests = new TreeSet<>();

        if (form.getHttpRequest() != null) {
            HttpSession session = form.getHttpRequest().getSession(true);
            SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(STORE_KEY);

            for (String tmpFileId : form.getParamValuesListStr("tmpFileId")) {
                int tmpFileIdInt = Utils.parseInt(tmpFileId);
                if (tmpFileIdInt <= 0)
                    throw new BGException("Incorrect ID of tmp file: " + tmpFileId);

                var file = files.files.get(tmpFileIdInt);
                if (file != null) {
                    if (!digests.add(file.getHash()))
                        throw new BGMessageException("File '{}' was already uploaded", file.getTitle());
                    result.put(tmpFileIdInt, file);
                }
            }
        }

        return result;
    }

    /**
     * Delete tmp files after persisting those
     * @param form the request form, with the session
     * @param ids the tmp file IDs
     */
    public static void deleteFiles(DynActionForm form, Set<Integer> ids) {
        if (form.getHttpRequest() != null) {
            HttpSession session = form.getHttpRequest().getSession(true);

            SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(STORE_KEY);
            if (files == null)
                return;

            for (int tmpFileId : ids) {
                var file = files.files.remove(tmpFileId);
                if (file != null)
                    file.delete();
            }
        }
    }

    /**
     * Delete tmp files on session close
     * @param session the session
     */
    public static void deleteFiles(HttpSession session) {
        SessionTemporaryFiles files = (SessionTemporaryFiles) session.getAttribute(SessionTemporaryFiles.STORE_KEY);
        if (files != null)
            files.files.values().stream().forEach(FileInfo::delete);
    }

    // end of static part

    /** File ID generator */
    private final AtomicInteger fileIndex = new AtomicInteger(1);
    /** Tmp files, key - ID */
    private final Map<Integer, FileInfo> files = new ConcurrentHashMap<>();
}