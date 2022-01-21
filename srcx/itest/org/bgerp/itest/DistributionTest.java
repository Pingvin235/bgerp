package org.bgerp.itest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
@Test(groups = "distribution")
public class DistributionTest {
    public static File zip;

    @Test
    public void initZip() throws Exception {
        // find zip
        File[] files = new File("build/bgerp").listFiles(f -> f.getName().matches("bgerp.+zip"));
        Assert.assertEquals(files.length, 1);
        zip = files[0];
    }

}