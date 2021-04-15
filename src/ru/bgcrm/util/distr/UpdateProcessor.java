package ru.bgcrm.util.distr;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ru.bgcrm.util.distr.InstallProcessor.FileInfo;
import ru.bgerp.util.Log;

/**
 * Process for update packages for a change.
 * 
 * @author Shamil Vakhitov
 */
public class UpdateProcessor {
    private static final Log log = Log.getLog();

    private static final String UPDATE_TO_CHANGE_URL = "https://bgerp.org/update/";

    private final List<String> changeIds = new ArrayList<>();
    private final List<String> updateFiles = new ArrayList<>(2);

    /**
     * Constructor loads IDs of changes, directory names under {@link #UPDATE_TO_CHANGE_URL}.
     * The result is available after using {@link #getChangeIds()}.
     * @throws IOException
     */
    public UpdateProcessor() throws IOException {
        changeIds();
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

    private void changeIds() throws IOException {
        final String changeFolder = UPDATE_TO_CHANGE_URL;
        Document doc = Jsoup.connect(changeFolder).get();
        for (Element link : doc.select("a")) {
            changeIds.add(link.attr("href").replace("/", ""));
        }
    }

    private void updateFiles(String changeId) throws IOException {
        final String changeFolder = UPDATE_TO_CHANGE_URL + changeId;
        Document doc = Jsoup.connect(changeFolder).get();
        for (Element link : doc.select("a")) {
            String href = link.attr("href");
            if (FileInfo.isValidFileName(href)) {
                log.info("Downloading: %s", href);
                FileUtils.copyURLToFile(new URL(changeFolder + "/" + href), new File(href));
                updateFiles.add(href);
            }
        }
    }

    public List<String> getChangeIds() {
        return changeIds;
    }

    public List<String> getUpdateFiles() {
        return updateFiles;
    }
}
