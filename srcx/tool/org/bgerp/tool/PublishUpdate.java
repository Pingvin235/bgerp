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
 * Uses utilities: 'ssh', 'scp', 'rsync'.
 *
 * @author Shamil Vakhitov
 */
public class PublishUpdate {
    private static final Log LOG = Log.getLog();

    private final static String SSH_LOGIN = "cdn@bgerp.org";
    private final static String SSH_DIR = "/home/cdn/www/update/";
    private final static String[] SSH_OPTIONS = { "-o", "StrictHostKeyChecking=no", "-o", "UserKnownHostsFile=/dev/null" };

    private final String dir;
    private final String version;
    /** Remote dir for the process. */
    private final String sshDir;

    private PublishUpdate(String dir, String version, String changeId) throws Exception {
        LOG.info("Publishing remotely, dir: {}; version: {}; processId: {}", dir, version, changeId);

        this.dir = dir;
        this.version = version;
        this.sshDir = SSH_DIR + changeId;

        LOG.info("Create dir if not exists");
        new RuntimeRunner("ssh", SSH_OPTIONS, SSH_LOGIN, "mkdir -p " + sshDir).run();

        var updateFile = publishUpdateFile("update");
        var updateLibFile = publishUpdateFile("update_lib");

        var docDir = new File(dir + "/../doc");
        if (docDir.exists() && docDir.isDirectory()) {
            LOG.info("Sync doc");
            new RuntimeRunner("rsync", "--delete", "-Pav",
                "-e", "ssh " + String.join(" ", SSH_OPTIONS), docDir.toString(),
                SSH_LOGIN + ":" + sshDir).run();
        }

        var changesName = dir + "/build/changes." + changeId + ".txt";
        var changesFile = new File(changesName);
        if (changesFile.exists()) {
            LOG.info("Copy changes");

            var content = IOUtils.toString(new FileInputStream(changesFile), StandardCharsets.UTF_8)
                .replace("doc/3.0/manual", "update/" + changeId + "/doc");

            changesName = dir + "/target/changes.txt";
            IOUtils.write(content, new FileOutputStream(changesName), StandardCharsets.UTF_8);

            new RuntimeRunner("scp", SSH_OPTIONS, changesName, SSH_LOGIN + ":" + sshDir).run();
        }

        var updateDir = "https://bgerp.org/update/" + changeId;
        LOG.info("Update links:");
        LOG.info(updateDir);
        if (updateFile != null) {
            LOG.info(updateDir + "/" + updateFile);
            if (updateLibFile != null)
                LOG.info(updateDir + "/" + updateLibFile);
            if (docDir.exists())
                LOG.info(updateDir + "/doc");
            if (changesFile.exists())
                LOG.info(updateDir + "/changes.txt");
        }
    }

    /**
     * Copies a file to a remote system. Removes previously existing.
     * @param name directory and name prefix of a copied file.
     * @return name of the copied file or null.
     * @throws Exception
     */
    private String publishUpdateFile(String name) throws Exception {
        String mask = name + "_" + version + "_*.zip";

        LOG.info("Remove existing {}", mask);
        new RuntimeRunner("ssh", SSH_OPTIONS, SSH_LOGIN, "rm -f ", sshDir + "/" + mask).run();

        var files = new File(dir).list(new WildcardFileFilter(mask));
        if (files.length > 0) {
            var file = files[0];
            LOG.info("Copy {}", file);
            new RuntimeRunner("scp", SSH_OPTIONS, dir + "/" + file, SSH_LOGIN + ":" + sshDir).run();

            return file;
        }
        return null;
    }

     public static void main(String[] args) throws Exception {
        new PublishUpdate(args[0], args[1], args[2]);
    }
}

