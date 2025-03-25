package org.bgerp.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bgerp.util.Log;

/**
 * Logic for Gradle task 'publishChange'.
 * Used utilities: 'ssh', 'scp', 'rsync'.
 *
 * @author Shamil Vakhitov
 */
public class PublishChange extends PublishBase {
    private static final Log log = Log.getLog();

    /** Target remote dir, change it to "/home/cdn/www/_change/" when testing. */
    private final static String SSH_DIR = "/home/cdn/www/change/";

    private PublishChange(String targetDistrDir, String version, String changeId) throws Exception {
        super(version, targetDistrDir, SSH_DIR + changeId);

        log.info("Publishing change: dir: {}, version: {}, processId: {}", targetDistrDir, version, changeId);

        log.info("Create dir if not exists");
        ssh("mkdir -p " + sshDir);

        var updateFile = publishFile("update");
        var updateLibFile = publishFile("update_lib");

        var docDir = publishDir(targetDistrDir, "doc");
        var javaDocDir = publishDir(targetDistrDir, "javadoc");

        var changesName = targetDistrDir + "/../../build/changes." + changeId + ".txt";
        var changesFile = new File(changesName);
        if (changesFile.exists()) {
            log.info("Copy changes");

            var content = IOUtils.toString(new FileInputStream(changesFile), StandardCharsets.UTF_8)
                .replace("change/0/doc", "change/" + changeId + "/doc");

            changesName = targetDistrDir + "/../changes.txt";
            IOUtils.write(content, new FileOutputStream(changesName), StandardCharsets.UTF_8);

            scp(changesName);
        } else
            log.info("Changes file is missing: {}", changesName);

        var changeDir = "https://bgerp.org/change/" + changeId;
        log.info("Update links:");
        log.info(changeDir);
        if (updateFile != null) {
            log.info(changeDir + "/" + updateFile);
            if (updateLibFile != null)
                log.info(changeDir + "/" + updateLibFile);
            if (docDir.exists())
                log.info(changeDir + "/doc");
            if (javaDocDir.exists())
                log.info(changeDir + "/javadoc");
            if (changesFile.exists())
                log.info(changeDir + "/changes.txt");
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

    /**
     * Copies a directory to remote system.
     * @param targetDistrDir
     * @param name
     * @return
     * @throws Exception
     */
    private File publishDir(String targetDistrDir, String name) throws Exception {
        var dir = new File(targetDistrDir + "/../" + name);

        if (dir.exists() && dir.isDirectory()) {
            log.info("Sync {}", name);
            new RuntimeRunner("rsync", "--delete", "-Pav",
                "-e", "ssh " + String.join(" ", SSH_OPTIONS), dir.toString(),
                SSH_LOGIN + ":" + sshDir).run();
        } else
            log.info("{} directory is missing: {}", name, dir);

        return dir;
    }

    public static void main(String[] args) throws Exception {
        new PublishChange(args[0], args[1], args[2]);
    }
}

