package ru.bgcrm.util.distr;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.bgcrm.model.IdStringTitle;
import ru.bgcrm.util.distr.InstallProcessor.FileInfo;

/**
 * Parse changes info out of NGINX directory listing HTML.
 *
 * @author Shamil Vakhitov
 */
public class UpdateProcessor {
    private static final Log log = Log.getLog();

    private static final String UPDATE_TO_CHANGE_URL = "https://bgerp.org/update/";

    private final List<IdStringTitle> changes = new ArrayList<>();
    private final List<String> updateFiles = new ArrayList<>(2);

    /**
     * Constructor loads list of changes, directory names under {@link #UPDATE_TO_CHANGE_URL}.
     * The result is available after using {@link #getChanges()}.
     * @throws IOException
     */
    public UpdateProcessor() throws IOException {
        changes();
    }

    /**
     * Constructor downloads update files from {@link #UPDATE_TO_CHANGE_URL} / {@param changeId}
     * The names of this files are available after with {@link #getUpdateFiles()}.
     * @param changeId string with directory name.
     * @throws IOException
     */
    public UpdateProcessor(String changeId) throws IOException {
        updateFiles(changeId);
    }

    private void changes() throws IOException {
        Document doc = changes(UPDATE_TO_CHANGE_URL);
        for (Element link : doc.select("a")) {
            String id = link.attr("href").replace("/", "");
            if (id.contains("."))
                continue;
            changes.add(new IdStringTitle(id,
                    id + " " + StringUtils.substringBeforeLast(link.nextSibling().toString(), "-").trim()));
        }
    }

    private void updateFiles(String changeId) throws IOException {
        final String url = UPDATE_TO_CHANGE_URL + changeId;
        Document doc = changes(url);
        for (Element link : doc.select("a")) {
            String href = link.attr("href");
            if (FileInfo.isValidFileName(href)) {
                log.info("Downloading: {}", href);
                download(url, href);
                updateFiles.add(href);
            }
        }
    }

    @VisibleForTesting
    protected Document changes(final String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    @VisibleForTesting
    protected void download(final String url, String href) throws IOException, MalformedURLException {
        FileUtils.copyURLToFile(new URL(url + "/" + href), new File(href));
    }

    /**
     * Changes list. Each {@link IdStringTitle} has numeric change ID and ID plus file modification time in title.
     * @return
     */
    public List<IdStringTitle> getChanges() {
        return changes;
    }

    /**
     * Update ZIP files.
     * @return
     */
    public List<String> getUpdateFiles() {
        return updateFiles;
    }
}
