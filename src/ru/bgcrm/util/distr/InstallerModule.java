package ru.bgcrm.util.distr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;

import com.google.common.annotations.VisibleForTesting;

import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.ZipUtils;
import ru.bgcrm.util.distr.call.InstallationCall;

public class InstallerModule {
    protected static final String MESSAGE_ERROR = "ERROR";
    protected static final String MESSAGE_OK = "OK";

    /** Все файлы пакета. */
    private final List<String> files = new ArrayList<>();
    /** Изменившиеся файлы. */
    private final List<String> replacedFiles;

    @VisibleForTesting
    protected InstallerModule(File zip, List<String> replacedFiles) {
        this.replacedFiles = replacedFiles;
        ModuleInf mi = getModuleInf(zip);
        if (mi != null && !mi.hasErrors()) {
            boolean result = true;
            if (result) {
                executeCalls(mi, Setup.getSetup(), zip);
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
            
            removeExcessLibs(mi);
        }
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
                if (ze.getName().equals("module.properties")) {
                    infFound = true;
                    break;
                }
            }
            if (infFound) {
                byte[] infFile = IOUtils.toByteArray(zis);
                mi = new ModuleInf(new String(infFile, Utils.UTF8));
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

    private static final String contentPrefix = "content";
    private static final int contentPrefixLength = contentPrefix.length();

    @VisibleForTesting
    protected boolean copyFiles(File zip, ModuleInf mi) {
        boolean result = false;

        String name = null;
        try {
            FileInputStream fis = new FileInputStream(zip);
            ZipInputStream zis = new ZipInputStream(fis);
            for (Map.Entry<String, byte[]> me : ZipUtils.getFileEntriesFromZipByPrefix(zis, contentPrefix).entrySet()) {
                name = me.getKey().substring(contentPrefixLength + 1);

                // начальная сущность "content/"
                if (name.trim().length() == 0) {
                    continue;
                }

                if (name.endsWith("/")) {
                    new File(name).mkdirs();
                } else {
                    writeFile(name, me.getValue());
                }
            }
            fis.close();

            System.out.println("File's copy finished...");
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
                InstallationCall call = (InstallationCall) Class.forName(fullClassName.toString()).newInstance();
                call.call(setup, zip, param);
                result = true;
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("Result => " + result);
        }
    }

    private void writeFile(String filePath, byte[] value) throws Exception {
        files.add(filePath);
        
        File existFile = new File(filePath);
        // файл уже существует и не эквивалентен новому
        if (existFile.exists() && !equalFiles(value, existFile)) {
            // попытка найти оригинальный файл
            File origFile = new File(filePath + ".orig");
            if (origFile.exists() && equalFiles(value, origFile)) {
                System.out.println("File doesn't changed " + filePath);
                return;
            }

            replacedFiles.add(filePath);

            if (origFile.exists()) {
                // резервная копия
                existFile.renameTo(new File(filePath + ".bak." + System.currentTimeMillis()));
            }
        }

        FileOutputStream fos = new FileOutputStream(filePath);
        fos.write(value);
        fos.close();
    }

    private boolean equalFiles(byte[] newFile, File existFile) {
        boolean result = newFile.length == existFile.length();

        if (result) {
            try {
                FileInputStream fileInputStream = new FileInputStream(existFile);
                byte[] existFileData = IOUtils.toByteArray(fileInputStream);
                fileInputStream.close();

                final int size = newFile.length;
                for (int i = 0; i < size; i++) {
                    result = result && newFile[i] == existFileData[i];
                    if (!result) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }
    
    /** 
     * Удаление отсутствующих в lib/ext библиотек.
     * @param mi
     */
    private void removeExcessLibs(ModuleInf mi) {
        if (!"update_lib".equals(mi.getName())) return; 
        
        System.out.println("Checking of excess files.");
        
        final String path = "lib/ext";
        try {
            for (File file : new File(path).listFiles()) {
                String filePath = file.getPath().replace('\\', '/');
                // файл не был заменён - значит удаление
                if (!files.stream().filter(filePath::endsWith).findFirst().isPresent()) {
                    file.deleteOnExit();
                    System.out.println("DELETE: " + filePath);
                }                    
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void replacedReport(List<String> replacedFiles) {
        if (replacedFiles.size() > 0) {
            System.out.println("REPLACED FILES:");
            for (String file : replacedFiles) {
                System.out.println(file);
            }
        }
    }
}