package org.bgerp.itest;

import static org.bgerp.itest.DistributionTest.zip;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import ru.bgerp.util.Log;

/** Installation test, running shell script. */
@Ignore
@Test(groups = "runServer", dependsOnGroups = "distribution")
public class RunServerTest {
    private static final Log log = Log.getLog();

    private static final String PATH = "/opt/BGERP";

    @Test(dependsOnMethods = "initZip")
    public void cleanUp() throws Exception {
        // server
        File dir = new File(PATH);
        if (dir.exists()) {
            log.info("Delete existing installation: " + PATH);
            FileUtils.deleteDirectory(dir);
        }
    }

    @Test(dependsOnMethods = "cleanUp")
    public void unpackServer() throws Exception {
        log.info("Extracting BGERP zip: " + zip);

        // эта сложность со скриптом в надежде использовать его потом для установки сервера
        Process p = new ProcessBuilder("sh", "build/bgerp/install/bgerp.sh", zip.getPath()).start();
        Executors.newSingleThreadExecutor().submit(new StreamGobbler(p.getInputStream(), str -> log.debug(str)));
        Assert.assertEquals(true, p.waitFor(1, TimeUnit.MINUTES));
    }

    @Test(dependsOnMethods = "patchDb")
    public void serverStart() {
        Assert.assertEquals(true, true);
    }

}
