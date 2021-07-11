package ru.bgcrm.util.distr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.annotations.VisibleForTesting;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.ZipUtils;
import ru.bgcrm.util.distr.call.InstallationCall;

/**
 * Processor of a ZIP file with updates.
 * All the reports are intentionally written to STDOUT.
 * 
 * @author Shamil Vakhitov
 */
public class InstallerModule {
    protected static final String MESSAGE_ERROR = "ERROR";
    protected static final String MESSAGE_OK = "OK";

    private static final String SUFFIX_ORIG = ".orig";
    private static final String INFIX_BAK = ".bak.";

    /** Directory with files. */
    public static final String ENTRY_CONTENT = "content";
    private static final int ENTRY_CONTENT_LENGTH = ENTRY_CONTENT.length();

    public static final String ENTRY_MODULE_PROPERTIES = "module.properties";

    private static final Set<String> CLEANED_DIRS_UPDATE = Set.of(
        // TODO: docpattern, report, scripts
        // dirs for removing
        "action",
        "plugin",
        // report,
        // actual dirs
        "webapps/css",
        "webapps/img",
        "webapps/js",
        "webapps/lib"
    );

    private static final Set<String> CLEANED_DIRS_UPDATE_LIB = Set.of(
        "lib/ext"
    );

    /** A directory to be updated. */
    private final File targetDir;
    /** All overwritten file paths. */
    private final List<String> paths = new ArrayList<>(100);
    /** Path prefixes to be cleaned up of not more presented files. */
    private final Set<String> cleanedDirs;
   
    private final Report report = new Report();

    @VisibleForTesting
    InstallerModule() {
        this.targetDir = null;
        this.cleanedDirs = null;
    }

    InstallerModule(Setup setup, File targetDir, File zip) {
        this.targetDir = targetDir;
        var mi = getModuleInf(zip);
        if (mi == null || mi.hasErrors())
            throw new IllegalArgumentException("Module info was not found or incorrect");

        if ("update".equals(mi.getName()))
            cleanedDirs = CLEANED_DIRS_UPDATE;
        else if ("update_lib".equals(mi.getName()))
            cleanedDirs = CLEANED_DIRS_UPDATE_LIB;
        else
            throw new IllegalArgumentException("Unsupported module: " + mi.getName());

        boolean result = true;

        if (result) {
            executeCalls(mi, setup, zip);
            System.out.println("Execute calls => " + (result ? MESSAGE_OK : MESSAGE_ERROR));
        }
        if (result) {
            result = copyFiles(zip, mi);
            System.out.println("File copy => " + (result ? MESSAGE_OK : MESSAGE_ERROR));
        }
        if (!result) {
            System.out.println("Module was not installed.");
        } else {
            System.out.println("Module " + mi.getName() + " was successfully installed!");
            System.out.println("Please, restart BGERP server.");
        }

        removeExcessFiles();
    }

    @VisibleForTesting
    protected ModuleInf getModuleInf(File zip) {
        ModuleInf mi = null;
        try {
            FileInputStream fis = new FileInputStream(zip);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = null;
            boolean infFound = false;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.getName().equals(ENTRY_MODULE_PROPERTIES)) {
                    infFound = true;
                    break;
                }
            }
            if (infFound) {
                byte[] infFile = IOUtils.toByteArray(zis);
                mi = new ModuleInf(new String(infFile, StandardCharsets.UTF_8));
            } else {
                System.out.println("Error: module.properties not found in zip");
            }
            fis.close();
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
        return mi;
    }

    @VisibleForTesting
    protected boolean copyFiles(File zip, ModuleInf mi) {
        boolean result = false;

        String name = null;
        try (var fis = new FileInputStream(zip);
             var zis = new ZipInputStream(fis);) {
            
            for (Map.Entry<String, byte[]> me : ZipUtils.getFileEntriesFromZipByPrefix(zis, ENTRY_CONTENT).entrySet()) {
                name = me.getKey().substring(ENTRY_CONTENT_LENGTH + 1);

                // starting entry "content/"
                if (name.trim().length() == 0) {
                    continue;
                }

                if (name.endsWith("/")) {
                    new File(targetDir, name).mkdirs();
                } else {
                    writeFile(name, me.getValue());
                }
            }
            fis.close();

            result = true;
        } catch (Exception ex) {
            System.out.println("File's copy error.. File: " + name);
            ex.printStackTrace();
        }

        return result;
    }

    @VisibleForTesting
    protected void executeCalls(ModuleInf mi, Setup setup, File zip) {
        List<String[]> calls = mi.getCalls();
        for (String[] class_param : calls) {
            String callClass = class_param[0];
            String param = class_param[1];
            System.out.println("Executing call " + callClass + "; param: " + param);
            boolean result = false;
            try {
                String fullClassName = "ru.bgcrm.util.distr.call." + callClass;
                InstallationCall call = (InstallationCall) Class.forName(fullClassName.toString()).getDeclaredConstructor().newInstance();
                call.call(setup, zip, param);
                result = true;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("Result => " + result);
        }
    }

    private void writeFile(String path, byte[] content) throws Exception {
        paths.add(path);

        var file = new File(targetDir, path);
        if (file.exists() && !equal(content, file)) {
            var origFile = new File(targetDir, path + SUFFIX_ORIG);
            if (origFile.exists()) {
                if (equal(content, origFile)) {
                    System.out.println("File hasn't changed " + path);
                    return;
                }
                file.renameTo(new File(targetDir, path + INFIX_BAK + System.currentTimeMillis()));
            }
            report.replaced.add(path);
        }

        try (var fos = new FileOutputStream(new File(targetDir, path))) {
            fos.write(content);
        }
    }

    private boolean equal(byte[] newContent, File existing) {
        if (newContent.length == existing.length()) {
            try (FileInputStream fileInputStream = new FileInputStream(existing)) {
                byte[] existingContent = IOUtils.toByteArray(fileInputStream);
                return Arrays.compare(newContent, existingContent) == 0;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void removeExcessFiles() {
        System.out.println("Checking of excess files.");
        for (String dirPath : cleanedDirs) {
            try {
                removeExcessFiles(dirPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeExcessFiles(String dirPath) throws IOException {
        var dir = new File(targetDir, dirPath);

        var innerPaths = paths.stream().filter(p -> p.startsWith(dirPath)).collect(Collectors.toSet());
        if (innerPaths.isEmpty()) {
            delete(dirPath, dir);
            return;
        }

        for (File file : dir.listFiles()) {
            var path = dirPath + "/" + file.getName();

            if (file.isDirectory()) {
                removeExcessFiles(path);
                continue;
            }

            // .orig files
            if (path.endsWith(SUFFIX_ORIG)) {
                report.removeSoon.add(path);
                // checking existence of original
                if (!innerPaths.contains(path.substring(0, path.length() - SUFFIX_ORIG.length())))
                    delete(path, file);
            } 
            // .bak.1234455 files
            else if (path.contains(INFIX_BAK)) {
                report.removeSoon.add(path);
                // checking existence of the original
                if (!innerPaths.contains(StringUtils.substringBeforeLast(path, INFIX_BAK)))
                    delete(path, file);
            }
            // file wasn't rewritten - removing
            else if (!innerPaths.contains(path)) {
               delete(path, file);
            }
        }
    }

    private void delete(String path, File file) {
        if (!file.exists())
            return;

        if (path.contains("custom")) {
            report.removeSoon.add(path);
        } else {
            report.removed.add(path);
            FileUtils.deleteQuietly(file);
        }
    }

    public Report getReport() {
        return report;
    }

    public static class Report {
         /** Updated files. */
        private final Set<String> replaced = new TreeSet<>();
        /** Removed excess files. */
        private final Set<String> removed = new TreeSet<>();
        /** Files, which will be removed in future. */
        private final Set<String> removeSoon = new TreeSet<>();

        public Set<String> getReplaced() {
            return replaced;
        }

        public Set<String> getRemoved() {
            return removed;
        }

        public Set<String> getRemoveSoon() {
            return removeSoon;
        }

        @Override
        public String toString() {
            var report = new StringWriter(2000);
            var writer = new PrintWriter(report);
            subReport(writer, "REPLACED:", replaced);
            subReport(writer, "REMOVED:", removed);
            subReport(writer, "REMOVE SOON:", removeSoon);
            return report.toString();
        }

        private void subReport(PrintWriter writer, String prefix, Collection<String> paths) {
            if (!paths.isEmpty()) {
                writer.println(prefix);
                for (String path : paths) {
                    writer.println(path);
                }
            }
        }
    }
}