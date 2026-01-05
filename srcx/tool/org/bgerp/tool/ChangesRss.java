package org.bgerp.tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.util.Log;

import ru.bgcrm.util.TimeUtils;
import ru.bgcrm.util.Utils;

/**
 * Convertor changes.txt to RSS changes.xml.
 *
 * @author Shamil Vakhitov
 */
public class ChangesRss {
    private static final Log log = Log.getLog();

    // Mon, 06 Mar 2017 15:05:02 +0500
    private static final DateFormat PUB_DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final String title;
    private final String downloadLink;

    private ChangesRss(String version, String fileIn, String fileOut) throws Exception {
        this.title = "ChangeLog BGERP v." + version + " CI";
        this.downloadLink = "ftp://bgerp.org/pub/bgerp/" + version;

        log.info("Generating RSS for version: {}", version);

        try (BufferedReader changesTxt = new BufferedReader(new InputStreamReader(new FileInputStream(fileIn), StandardCharsets.UTF_8))) {
            SortedMap<Date, Record> sortedRecords = new TreeMap<>();

            Record current = null;

            String line = null;
            while ((line = changesTxt.readLine()) != null) {
                Record record = getRecordByStartLine(line);
                if (record != null) {
                    current = record;
                    sortedRecords.put(current.time, current);
                    continue;
                }

                if (current != null && !StringUtils.isBlank(line))
                    current.appendDescription(line);
            }

            BufferedWriter changesRss = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileOut), StandardCharsets.UTF_8));
            changesRss.write("<rss version='2.0'>\n");
            changesRss.write("<channel>\n");
            changesRss.write("<title>" + title + "</title>\n");
            changesRss.write("<link>" + downloadLink + "</link>\n");

            for (Record record : sortedRecords.values())
                changesRss.append(record.toString());

            changesRss.write("</channel>");
            changesRss.write("</rss>");
            changesRss.close();
        }
    }

    // 04.04.2018 00:38:37 1242
    private Record getRecordByStartLine(String line) {
        String[] tokens = line.split("\\s+");
        if (tokens.length != 3)
            return null;

        Date time = TimeUtils.parse(tokens[0] + " " + tokens[1], TimeUtils.PATTERN_DDMMYYYYHHMMSS);
        int build = Utils.parseInt(tokens[2]);
        if (time == null && build <= 0)
            return null;

        return new Record(time, build);
    }

    private class Record {

        private final Date time;
        private final int build;
        private final StringBuilder description = new StringBuilder(300);

        private Record(Date time, int build) {
            this.time = time;
            this.build = build;
        }

        private void appendDescription(String line) {
            line = line
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");

            if (description.length() == 0)
                description.append("\n");

            if (line.startsWith("A:") || line.startsWith("А:") || line.startsWith("N:"))
                description.append("NEW:").append(line.substring(2));
            else if (line.startsWith("С:") || line.startsWith("C:"))
                description.append("CHANGE:").append(line.substring(2));
            else if (line.startsWith("B:") || line.startsWith("В:"))
                description.append("BREAKING CHANGE:").append(line.substring(2));
            else if (line.startsWith("F:"))
                description.append("FIX:").append(line.substring(2));
            else
                description.append(line);

            description.append("\n");
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(1000);

            result.append("<item>\n");
            result.append("<title>Build " + build + "</title>\n");
            result.append("<link>" + downloadLink + "</link>\n");
            result.append("<guid>" + downloadLink + "#" + build + "</guid>\n");
            result.append("<description>").append(description).append("</description>\n");
            result.append("<pubDate>" + PUB_DATE_FORMAT.format(time) + "</pubDate>\n");
            result.append("</item>\n");

            return result.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        new ChangesRss(args[0], args[1], args[2]);
    }

}