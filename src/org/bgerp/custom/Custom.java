package org.bgerp.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bgerp.Server;
import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.custom.java.CompilationResult;
import org.bgerp.custom.java.CompilerWrapper;
import org.bgerp.util.Log;

/**
 * Customization manager.
 *
 * @author Shamil Vakhitov
 */
public class Custom {
    private static final Log log = Log.getLog();

    public static final Custom INSTANCE = new Custom();

    public static final File JAR = new File("lib/app/custom.jar");

    public static final String PACKAGE = "org.bgerp.plugin.custom";

    private static final String DIR_NAME = "custom";
    private static final File DIR = new File(DIR_NAME);

    private static final File DIR_SRC = new File(DIR, "src");
    private static final File DIR_WEBAPPS = new File(DIR, Server.WEBAPPS_DIR);

    private volatile URLClassLoader classLoader;

    /**
     * Configures custom webapps directory.
     * @param catalinaHome
     * @param context
     */
    public void webapps(String catalinaHome, StandardContext context) {
        if (!DIR_WEBAPPS.isDirectory())
            return;

        log.info("Connecting custom webapps: {}", DIR_WEBAPPS);
        var webResourceRoot = new StandardRoot(context);
        webResourceRoot.addPreResources(new DirResourceSet(webResourceRoot, "/",
            catalinaHome + "/" + DIR_NAME + "/" + Server.WEBAPPS_DIR, "/"));
        context.setResources(webResourceRoot);
    }

    /**
     * Compiles all Java sources in {@link #DIR}/src.
     * @return null or compilation result.
     */
    public CompilationResult compileJava() throws IOException, BGMessageException {
        if (!DIR_SRC.isDirectory())
            throw new BGMessageException("src directory is missing");

        var srcFiles = new ArrayList<String>(100);
        traverse(srcFiles, DIR_SRC);

        var compiler = new CompilerWrapper(DIR_SRC);
        log.info("Compiling {} java files to {}", srcFiles.size(), compiler.getOutputDir());

        CompilationResult result = compiler.compile(srcFiles).getFirst();
        result.addLog(Log.format("Compiling {} java files to {}", srcFiles.size(), compiler.getOutputDir()));

        if (result.isResult()) {
            copyResources(DIR_SRC.toPath(), compiler.getOutputDir().toPath());
            buildCustomJar(compiler, result);
        }

        return result;
    }

    /**
     * @return classloader for {@link #JAR} if it was built in the instance.
     */
    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    private void copyResources(Path srcDir, Path outputDir) throws IOException {
        Files
            .walk(srcDir)
            .filter(Files::isRegularFile)
            .filter(path -> !path.toString().endsWith(".java"))
            .forEach(path -> {
                try {
                    log.debug("Copying resource {}", path);
                    FileUtils.copyFile(path.toFile(), outputDir.resolve(srcDir.relativize(path)).toFile());
                } catch (IOException e) {
                    log.error(e);
                }
            });
    }

    private void buildCustomJar(CompilerWrapper compiler, CompilationResult result) throws IOException {
        JAR.getParentFile().mkdirs();
        try (var zipOut = new ZipOutputStream(new FileOutputStream(JAR))) {
            for (var file : compiler.getOutputDir().listFiles())
                zipFile(file, file.getName(), zipOut);
        }

        classLoader = new URLClassLoader(new URL[] { JAR.toURI().toURL() }, ClassLoader.getSystemClassLoader());
        Bean.loadBeanClasses();

        result.addLog(Log.format("Built: {} {} bytes", JAR, JAR.length()));
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
