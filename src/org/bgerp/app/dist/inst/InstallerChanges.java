package org.bgerp.app.dist.inst;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.base.IdStringTitle;
import org.bgerp.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.bgcrm.util.Utils;

/**
 * Parse changes info out of NGINX directory listing HTML.
 *
 * @author Shamil Vakhitov
 */
public class InstallerChanges {
    private static final Log log = Log.getLog();

    private static final String UPDATE_TO_CHANGE_URL = "https://bgerp.org/update/";
    private static final String CHANGE_PRE_RELEASE = "00000";
    private static final String TMP_DIR_PATH = Utils.getTmpDir();

    private final List<Change> changes = new ArrayList<>();
    private final List<String> updateFiles = new ArrayList<>(2);

    /**
     * Constructor loads list of changes, directory names under {@link #UPDATE_TO_CHANGE_URL}.
     * The result is available after using {@link #getChanges()}.
     * @throws IOException
     * @throws ParseException
     */
    public InstallerChanges() throws IOException, ParseException {
        changes();
    }

    /**
     * Constructor downloads update files from {@link #UPDATE_TO_CHANGE_URL} / {@param changeId}
     * The names of this files are available after with {@link #getUpdateFiles()}.
     * @param changeId string with directory name.
     * @throws IOException
     */
    public InstallerChanges(String changeId) throws IOException {
        updateFiles(changeId);
    }

    private void changes() throws IOException, ParseException {
        Document doc = changes(UPDATE_TO_CHANGE_URL);
        for (Element link : doc.select("a")) {
            String id = link.attr("href").replace("/", "");
            if (id.contains("."))
                continue;
            changes.add(new Change(id, StringUtils.substringBeforeLast(link.nextSibling().toString(), "-").trim()));
        }

        // sorting, first 00000, after reverse sorted by modification time
        Collections.sort(changes, (o1, o2) -> {
            if (CHANGE_PRE_RELEASE.equals(o1.getId()))
                return -1;
            if (CHANGE_PRE_RELEASE.equals(o2.getId()))
                return 1;
            return o2.time.compareTo(o1.time);
        });
    }

    private void updateFiles(String changeId) throws IOException {
        final String url = UPDATE_TO_CHANGE_URL + changeId;
        Document doc = changes(url);
        for (Element link : doc.select("a")) {
            String name = link.attr("href");
            if (ModuleFile.isValidFileName(name)) {
                log.info("Downloading {} to {}", name, TMP_DIR_PATH);
                download(url, name);
                updateFiles.add(name);
            }
        }
    }

    @VisibleForTesting
    protected Document changes(final String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    @VisibleForTesting
    protected void download(final String url, String name) throws IOException, MalformedURLException {
        FileUtils.copyURLToFile(new URL(url + "/" + name), new File(TMP_DIR_PATH, name));
    }

    /**
     * Changes list. Each {@link IdStringTitle} has numeric change ID and ID plus file modification time in title.
     * @return
     */
    public List<Change> getChanges() {
        return changes;
    }

    /**
     * Update ZIP files, stored in {@link Utils#getTmpDir()}
     * @return
     */
    public List<String> getUpdateFiles() {
        return updateFiles;
    }

    /**
     * Change info.
     */
    public static class Change extends IdStringTitle {
        private final Date time;

        private Change(String id, String time) throws ParseException {
            super(id, id + " " + time);
            this.time = (Date) new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.US).parse(time);
        }
    }
}
