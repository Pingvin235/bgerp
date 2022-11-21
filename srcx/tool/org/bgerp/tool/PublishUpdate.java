package org.bgerp.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bgerp.util.Log;

/**
 * Logic for Gradle task 'publishUpdate'.
 * Used utilities: 'ssh', 'scp', 'rsync'.
 *
 * @author Shamil Vakhitov
 */
public class PublishUpdate extends PublishCommon {
    private static final Log log = Log.getLog();

    /** Target remote dir, change it to "/home/cdn/www/_update/" when testing. */
    private final static String SSH_DIR = "/home/cdn/www/update/";

    private PublishUpdate(String dir, String version, String changeId) throws Exception {
        super(version, dir, SSH_DIR + changeId);

        log.info("Publishing update: dir: {}, version: {}, processId: {}", dir, version, changeId);

        log.info("Create dir if not exists");
        ssh("mkdir -p " + sshDir);

        var updateFile = publishFile("update");
        var updateLibFile = publishFile("update_lib");

        var docDir = new File(dir + "/../doc");
        if (docDir.exists() && docDir.isDirectory()) {
            log.info("Sync doc");
            new RuntimeRunner("rsync", "--delete", "-Pav",
                "-e", "ssh " + String.join(" ", SSH_OPTIONS), docDir.toString(),
                SSH_LOGIN + ":" + sshDir).run();
        }

        var changesName = dir + "/build/changes." + changeId + ".txt";
        var changesFile = new File(changesName);
        if (changesFile.exists()) {
            log.info("Copy changes");

            var content = IOUtils.toString(new FileInputStream(changesFile), StandardCharsets.UTF_8)
                .replace("doc/" + version + "/manual", "update/" + changeId + "/doc");

            changesName = dir + "/target/changes.txt";
            IOUtils.write(content, new FileOutputStream(changesName), StandardCharsets.UTF_8);

            scp(changesName);
        }

        var updateDir = "https://bgerp.org/update/" + changeId;
        log.info("Update links:");
        log.info(updateDir);
        if (updateFile != null) {
            log.info(updateDir + "/" + updateFile);
            if (updateLibFile != null)
                log.info(updateDir + "/" + updateLibFile);
            if (docDir.exists())
                log.info(updateDir + "/doc");
            if (changesFile.exists())
                log.info(updateDir + "/changes.txt");
        }
    }

    /**
     * Copies a file to a remote system. Removes previously existing.
     * @param name name prefix of a copied from {@link #dir} file.
     * @param removeExisting delete already existing remote file with the same mask.
     * @return name of the copied file or {@code null}.
     * @throws Exception
     */
    protected String publishFile(String name) throws Exception {
        final String mask = name + "_" + version + "_*.zip";

        log.info("Remove existing: {}", mask);
        ssh("rm -f ", sshDir + "/" + mask);

        var files = new File(dir).list(new WildcardFileFilter(mask));
        if (files.length > 0) {
            var file = files[0];
            log.info("Copy: {}", file);
            scp(dir + "/" + file);

            return file;
        }

        return null;
    }

     public static void main(String[] args) throws Exception {
        new PublishUpdate(args[0], args[1], args[2]);
    }
}

