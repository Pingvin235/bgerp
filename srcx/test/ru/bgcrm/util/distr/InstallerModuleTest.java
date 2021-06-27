package ru.bgcrm.util.distr;

import static org.bgerp.util.TestUtils.addDir;
import static org.bgerp.util.TestUtils.addFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.bgcrm.util.ZipUtils;

public class InstallerModuleTest extends InstallerModule {
    private File testDir;

    @Before
    public void before() throws Exception {
        testDir = Files.createTempDirectory("installer-module-test-").toFile();
        FileUtils.cleanDirectory(testDir);
    }

    @After
    public void clean() throws IOException {
        FileUtils.deleteDirectory(testDir);
    }

    private File getUpdateZip(String name, Map<String, String> entries) throws IOException {
        var zip = new File(testDir, name + ".zip");
        if (zip.exists())
            return zip;

        var bos = new ByteArrayOutputStream(1000);
        try (var zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {
            ZipUtils.addEntry(zos, "module.properties",
                "module.version=1.0\n" +
                "name=" + name + "\n");

            ZipUtils.addEntry(zos, "content/", null);
            for (var me : entries.entrySet())
                ZipUtils.addEntry(zos, me.getKey(), me.getValue());
        }

        try (var fos = new FileOutputStream(zip)) {
            IOUtils.write(bos.toByteArray(), fos);
        }

        return zip;
    }

    @Test
    public void testGetModuleInf() throws IOException {
        var zip = getUpdateZip("update_lib", Map.of());
        var mi = getModuleInf(zip);
        Assert.assertNotNull(mi);
        Assert.assertEquals(mi.getName(), "update_lib");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUseWrongModuleInf() throws IOException {
        var zip = getUpdateZip("xxx", Map.of());
        new InstallerModule(null, null, zip);
    }

    @Test
    public void testFilesUpdateLib() throws IOException {
        var zip = getUpdateZip("update_lib", Map.of(
            "content/lib/ext/lib1.jar", "new",
            "content/lib/ext/lib2.jar", "new"
        ));

        var target = addDir(testDir, "test-files-update-lib");

        var libExt = addDir(target, "lib/ext");
        addFile(libExt, "lib1.jar", "old");

        addFile(libExt, "lib2.jar", "old-changed");
        addFile(libExt, "lib2.jar.orig", "old");

        addFile(libExt, "lib4_old.jar", "");

        var report = new InstallerModule(null, target, zip).getReport();
        var replaced = report.getReplaced();
        var removed = report.getRemoved();
        var removeSoon = report.getRemoveSoon();

        Assert.assertEquals(2, replaced.size());
        Assert.assertTrue(replaced.contains("lib/ext/lib1.jar"));
        Assert.assertEquals("new", IOUtils.toString(new File(libExt, "lib1.jar").toURI(), StandardCharsets.UTF_8));

        Assert.assertTrue(replaced.contains("lib/ext/lib2.jar"));
        Assert.assertEquals("new", IOUtils.toString(new File(libExt, "lib2.jar").toURI(), StandardCharsets.UTF_8));
        var lib2bak = libExt.listFiles(f -> f.getName().startsWith("lib2.jar.bak"));
        Assert.assertEquals(1, lib2bak.length);
        Assert.assertEquals("old-changed", IOUtils.toString(lib2bak[0].toURI(), StandardCharsets.UTF_8));
        Assert.assertTrue(removeSoon.contains("lib/ext/lib2.jar.orig"));
        Assert.assertTrue(new File(libExt, "lib2.jar.orig").exists());

        Assert.assertTrue(removed.contains("lib/ext/lib4_old.jar"));
        Assert.assertFalse(new File(libExt, "lib4_old.jar").exists());

        System.out.println(report);
    }

    @Test
    public void testFilesUpdate() throws IOException {
        var zip = getUpdateZip("update", Map.of(
            "content/webapps/js/sub/file3.js", "new"
        ));

        var target = addDir(testDir, "test-files-update");

        var webappsJs = addDir(target, "webapps/js");

        var webappsJsSub = addDir(webappsJs, "sub");
        addFile(webappsJsSub, "file3.js", "new-changed");
        addFile(webappsJsSub, "file3.js.orig", "new");

        var webappsJsOldSubDir = addDir(webappsJs, "old-subdir");
        addFile(webappsJsOldSubDir, "old-file", "old");

        var plugin = addDir(target, "plugin");

        var libExt = addDir(testDir, "lib/ext");
        var libExtLib = addFile(libExt, "test.jar", "jar");

        var report = new InstallerModule(null, target, zip).getReport();
        var replaced = report.getReplaced();
        var removed = report.getRemoved();
        var removeSoon = report.getRemoveSoon();

        Assert.assertEquals(0, replaced.size());
        
        Assert.assertTrue(removeSoon.contains("webapps/js/sub/file3.js.orig"));
        Assert.assertEquals("new", IOUtils.toString(new File(webappsJsSub, "file3.js.orig").toURI(), StandardCharsets.UTF_8));
        Assert.assertEquals("new-changed", IOUtils.toString(new File(webappsJsSub, "file3.js").toURI(), StandardCharsets.UTF_8));

        Assert.assertTrue(removed.contains("webapps/js/old-subdir"));
        Assert.assertFalse(webappsJsOldSubDir.exists());

        Assert.assertFalse(removed.contains("action"));
        Assert.assertTrue(removed.contains("plugin"));
        Assert.assertFalse(plugin.exists());

        Assert.assertTrue(libExt.exists());
        Assert.assertTrue(libExtLib.exists());

        System.out.println(report);
    }
}
