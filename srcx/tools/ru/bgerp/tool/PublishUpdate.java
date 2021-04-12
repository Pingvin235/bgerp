package ru.bgerp.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Logic for Gradle task 'publishUpdate'.
 * Uses utilities: 'ssh', 'scp', 'rsync'. 
 * 
 * @author Shamil Vakhitov
 */
public class PublishUpdate {
    private final static String SSH_LOGIN = "bgerp-cdn@pzdc.de";
    private final static String SSH_DIR = "/home/bgerp-cdn/www/update/";
    private final static String[] SSH_OPTIONS = { "-o", "StrictHostKeyChecking=no", "-o", "UserKnownHostsFile=/dev/null" };

    private final String projectDir;
    private final String version;
    /** Remote dir for the process. */
    private final String sshDir;

    private PublishUpdate(String projectDir, String version, String processId) throws Exception {
        System.out.println("Publish remotely, projectDir: " + projectDir + "; version: " + version + "; processId: " + processId);

        this.projectDir = projectDir;
        this.version = version;
        this.sshDir = SSH_DIR + processId;

        System.out.println("Create dir if not exists");
        new RuntimeRunner("ssh", SSH_OPTIONS, SSH_LOGIN, "mkdir -p " + sshDir).run();

        var updateFile = publishUpdateFile("update");
        var updateLibFile = publishUpdateFile("update_lib");

        var docDir = new File(projectDir + "/target/doc");
        if (docDir.exists() && docDir.isDirectory()) {
            System.out.println("Sync doc");
            new RuntimeRunner("rsync", "--delete", "-Pav", 
                "-e", "ssh " + String.join(" ", SSH_OPTIONS), docDir.toString(), 
                SSH_LOGIN + ":" + sshDir).run();
        }

        var changesName = projectDir + "/build/changes." + processId + ".txt";
        var changesFile = new File(changesName);
        if (changesFile.exists()) {
            System.out.println("Copy changes");

            var content = IOUtils.toString(new FileInputStream(changesFile), StandardCharsets.UTF_8)
                .replace("doc/3.0/manual", "update/" + processId + "/doc");
            
            changesName = projectDir + "/target/changes.txt";
            IOUtils.write(content, new FileOutputStream(changesName), StandardCharsets.UTF_8);
            
            new RuntimeRunner("scp", SSH_OPTIONS, changesName, SSH_LOGIN + ":" + sshDir).run();
        }

        var updateDir = "https://bgerp.org/update/" + processId;
        System.out.println("Update links:");
        System.out.println(updateDir);
        if (updateFile != null) {
            System.out.println(updateDir + "/" + updateFile);
            if (updateLibFile != null)
                System.out.println(updateDir + "/" + updateLibFile);
            if (docDir.exists())
                System.out.println(updateDir + "/doc");
            if (changesFile.exists())
                System.out.println(updateDir + "/changes.txt");
        }
    }

    /**
     * Copies a file to a remote system. Removes previously existing.
     * @param name directory and name prefix of a copied file.
     * @return name of the copied file or null.
     * @throws Exception
     */
    private String publishUpdateFile(String name) throws Exception {
        var dir = projectDir + "/build/" + name;
        var mask = name + "_" + version + "_*.zip";
        var files = new File(dir).list(new WildcardFileFilter(mask));
        if (files.length > 0) {
            System.out.println("Remove existing " + mask);
            new RuntimeRunner("ssh", SSH_OPTIONS, SSH_LOGIN, "rm -f ", sshDir + "/" + mask).run();

            var file = files[0];
            System.out.println("Copy " + file);
            new RuntimeRunner("scp", SSH_OPTIONS, dir + "/" + file, SSH_LOGIN + ":" + sshDir).run();

            return file;
        }
        return null;
    }

     public static void main(String[] args) throws Exception {
        new PublishUpdate(args[0], args[1], args[2]);
    }
}

