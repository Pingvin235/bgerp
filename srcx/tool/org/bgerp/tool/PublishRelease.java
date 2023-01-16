package org.bgerp.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

/**
 * Logic for Gradle task 'publishRelease'.
 * Used utilities: 'ssh', 'scp'.
 *
 * @author Shamil Vakhitov
 */
public class PublishRelease extends PublishCommon {
    private static final Log log = Log.getLog();

    /** Target remote dir, change it to "/home/cdn/www/_download" when testing. */
    private final static String SSH_DIR = "/home/cdn/www/download/";

    /** Build number is published to web site. */
    private final static String SSH_LOGIN_BUILD_NUM = "www@bgerp.org";
    /** Target dir for build number, change it to "/home/www/www.bgerp.org/_download" when testing. */
    private final static String SSH_DIR_BUILD_NUM = "/home/www/www.bgerp.org/download";

    private final String build;

    public PublishRelease(String dir, String version, String build) throws Exception {
        super(version, dir, SSH_DIR + version);
        this.build =  build;

        log.info("Publishing release: dir: {}, version: {}, build: {}", dir, version, build);

        String name = publishFile("bgerp");
        if (Utils.notBlankString(name)) {
            log.info("Creating symlink for {}", name);
            ssh("ln", "-sf", sshDir + "/" + name, sshDir + "/bgerp.zip");
        }
        publishFile("update");
        publishFile("update_lib");
        scp(dir + "/../../build/changes.txt");
        scp(dir + "/../../build/changes.xml");
        publishBuildNumber();
    }

    /**
     * Copies a file to a remote system. Moves a previously existing to archive directory.
     * @param name name prefix of a copied from {@link #dir} file.
     * @return name of copied file or {@code null} if was not.
     * @throws Exception
     */
    protected String publishFile(String name) throws Exception {
        final String mask = name + "_" + version + "_*.zip";

        String remote = null;
        try {
            var runner = ssh("cd " + sshDir + "; ls " + mask);
            remote = runner.out().trim();
            log.info("Remote: {}", remote);
        } catch (Exception e) {
            // 'ls bgerp_3.0_*.zip' fails if there is no existing files like the mask
            // but that is a normal case for the first publish of a new version
        }

        var files = new File(dir).list(new WildcardFileFilter(mask));
        if (files.length > 0) {
            var local = files[0];

            log.info("Copy: {}", local);
            scp(dir + "/" + local);

            if (Utils.notBlankString(remote) && !local.equals(remote)) {
                log.info("Moving remote '{}' to archive", remote);
                ssh("mv", sshDir + "/" + remote, sshDir + "/archive/" + remote);
            }

            return local;
        }

        return null;
    }

    private void publishBuildNumber() throws Exception {
        final String file = dir + "/build.number";
        IOUtils.write(build, new FileOutputStream(file), StandardCharsets.UTF_8);
        new RuntimeRunner("scp", SSH_OPTIONS, file, SSH_LOGIN_BUILD_NUM + ":" + SSH_DIR_BUILD_NUM).run();
    }

    public static void main(String[] args) throws Exception {
        new PublishRelease(args[0], args[1], args[2]);
    }
}
