package org.bgerp.itest.plugin.custom;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.custom.Custom;
import org.bgerp.itest.helper.ResourceHelper;
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
        Custom.INSTANCE.compileJava();
        var customJar = Custom.JAR;
        Assert.assertTrue(customJar.exists() && customJar.length() > 0 && customJar.isFile(), "custom.jar is wrong");
    }

    @Test(dependsOnMethods = "compile")
    @SuppressWarnings("unchecked")
    public void bean() throws Exception {
        boolean thrown = false;
        try {
            Bean.getClass("CustomTestBean");
        } catch (ClassNotFoundException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);

        var classFile = new File("custom/src/org/bgerp/plugin/custom/demo/CustomTestBean.java");

        String classContent = ResourceHelper.getResource(this, "bean.java.txt");
        FileUtils.writeByteArrayToFile(classFile, classContent.getBytes(StandardCharsets.UTF_8));
        Custom.INSTANCE.compileJava();

        Class<?> clazz = Bean.getClass("CustomTestBean");
        Assert.assertNotNull(clazz);
        var bean = (Supplier<String>) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(bean.get(), "VALUE");

        classContent = classContent.replace("VALUE", "VALUE1");
        FileUtils.writeByteArrayToFile(classFile, classContent.getBytes(StandardCharsets.UTF_8));
        Custom.INSTANCE.compileJava();

        clazz = Bean.getClass("CustomTestBean");
        Assert.assertNotNull(clazz);
        bean = (Supplier<String>) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(bean.get(), "VALUE1");
    }

    @Test(dependsOnMethods = "compile")
    @SuppressWarnings("unchecked")
    public void clazz() throws Exception {
        var classFile = new File("custom/src/org/bgerp/plugin/custom/demo/CustomTestClass.java");

        String classContent = ResourceHelper.getResource(this, "class.java.txt");
        FileUtils.writeByteArrayToFile(classFile, classContent.getBytes(StandardCharsets.UTF_8));
        Custom.INSTANCE.compileJava();

        Class<?> clazz = Bean.getClass("org.bgerp.plugin.custom.demo.CustomTestClass");
        Assert.assertNotNull(clazz);
        var bean = (Supplier<String>) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(bean.get(), "VALUE");
    }
}
