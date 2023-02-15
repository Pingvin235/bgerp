package org.bgerp.itest.plugin.custom;

import org.bgerp.custom.Custom;
import org.bgerp.util.RuntimeRunner;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import ru.bgcrm.util.Utils;

@Test(groups = "custom", priority = 100, dependsOnGroups = "config")
public class CustomTest {
    @Test
    public void checkout() throws Exception {
        if (Utils.parseBoolean(System.getProperty("skip.custom"), true))
            throw new SkipException("Custom test is skipped by default and should be enabled with 'skip.custom' system property set to 'false'");

        new RuntimeRunner("git", "clone", "https://github.com/Pingvin235/bgerp-custom.git", "custom").run();
    }

    @Test(dependsOnMethods = "checkout")
    public void compile() throws Exception {
        Custom.getInstance().compileJava();
        var customJar = Custom.CUSTOM_JAR_FILE;
        Assert.assertTrue(customJar.exists() && customJar.length() > 0 && customJar.isFile(), "custom.jar is wrong");
    }
}
