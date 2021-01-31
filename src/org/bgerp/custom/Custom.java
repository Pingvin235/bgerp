package org.bgerp.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.commons.io.IOUtils;
import org.bgerp.custom.java.CompilationResult;
import org.bgerp.custom.java.CompilerWrapper;
import org.bgerp.custom.java.CompilerWrapper.CompilationFailedException;

import ru.bgcrm.Server;
import ru.bgerp.util.Log;

/**
 * Customization manager.
 * @author Shamil Vakhitov
 */
public class Custom {
    private static final Log log = Log.getLog();

    private static final String CUSTOM_DIR_NAME = "custom";
    private static final File CUSTOM_DIR = new File(CUSTOM_DIR_NAME);

    private static final File SRC_DIR = new File(CUSTOM_DIR, "src");
    private static final File WEBAPPS_DIR = new File(CUSTOM_DIR, Server.WEBAPPS_DIR_NAME);

    private static final File CUSTOM_JAR_FILE = new File("lib/app/custom.jar");
    
    public static Custom getInstance() {
        return new Custom();
    }

    public void webapps(String catalinaHome, StandardContext context) {
        if (!WEBAPPS_DIR.isDirectory())
            return;

        log.info("Connecting custom webapps: %s", WEBAPPS_DIR);
        var webResourceRoot = new StandardRoot(context);
        webResourceRoot.addPreResources(new DirResourceSet(webResourceRoot, "/",
            catalinaHome + "/" + CUSTOM_DIR_NAME + "/" + Server.WEBAPPS_DIR_NAME, "/"));
        context.setResources(webResourceRoot);
    }

    /**
     * Compiles all Java sources in {@link #CUSTOM_DIR}/src.
     * @return null or compilation result.
     */
    public CompilationResult compileJava() throws CompilationFailedException, IOException {
        if (!SRC_DIR.isDirectory()) {
            // TODO: Message about missing src directory.
            return null;
        }

        var srcFiles = new ArrayList<String>(100);
        traverse(srcFiles, SRC_DIR);

        var javac = new CompilerWrapper(SRC_DIR, new File(System.getProperty("java.io.tmpdir")));
        log.info("Compiling %s java files to %s", srcFiles.size(), javac.getOutputDir());
        
        CompilationResult result = javac.compile(srcFiles).getFirst();
        result.addLog(String.format("Compiling %s java files to %s", srcFiles.size(), javac.getOutputDir()));

        buildCustomJar(javac, result);
        javac.deleteClassDir();

        return result;
    }

    private void buildCustomJar(CompilerWrapper javac, CompilationResult result) throws IOException {
        CUSTOM_JAR_FILE.getParentFile().mkdirs();
        try (var zipOut = new ZipOutputStream(new FileOutputStream(CUSTOM_JAR_FILE))) {
            for (var file : javac.getOutputDir().listFiles())
                zipFile(file, file.getName(), zipOut);
        }

        result.addLog("Built: " + CUSTOM_JAR_FILE + " " + CUSTOM_JAR_FILE.length() + " bytes");
    }

    private void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        if (fileToZip.isDirectory()) {
            if (fileName.endsWith("/")) {
                zipOut.putNextEntry(new ZipEntry(fileName));
                zipOut.closeEntry();
            } else {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            }
            File[] children = fileToZip.listFiles();
            for (File childFile : children) {
                zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
            }
            return;
        }
        
        zipOut.putNextEntry(new ZipEntry(fileName));
        IOUtils.copy(new FileInputStream(fileToZip), zipOut);
    }

    /**
     * Collects absolute paths to all java source files.
     * @param files
     * @param directory
     */
    private void traverse(List<String> files, File directory) {
        for (var file : directory.listFiles()) {
            // exclude hidden dir like .git or .svn
            if (file.isDirectory() && !file.getName().startsWith(".")) {
                traverse(files, file);
            } else if (file.getName().endsWith(".java")) {
                files.add(file.getAbsolutePath());
            }
        }
    }
}
