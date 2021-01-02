package ru.bgcrm.plugin;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.testng.Assert;

public class LicenseTest {
    private License license;

    @Before
    public void init() throws Exception {
        license = new License(IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.lic.data"), StandardCharsets.UTF_8));
    } 

    @Test
    public void testDigest() {
        Assert.assertEquals(license.getDigest().length, 64);
    }

    @Test
    public void testErrors() {
        Assert.assertEquals(license.getError(), null);
    }

    @Test
    public void testPlugins() {
        Assert.assertEquals(license.getPlugins(), Set.of("bgbilling", "call.3sx"));
    }

    @Test
    public void testSignGenerateRsa() throws Exception {
        var key = new License.Key("test", IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.id_rsa"), StandardCharsets.UTF_8), null);
        Assert.assertEquals(key.signatureGenerate(license.getDigest()),
                "sNudadQhVaQAy9NXFOs89sAaQYmgHEqir8eJ9GyPQ6yr59gZ74xlmAUXZb7759B2sijN3/ubzHIhxALL0qbEBUcORrH3KUYdBuxOome91Z3Xt+Kz2Ir1heVXOew04gLeshnDHZkpfz+dQndbiuJNR1kTEOf3F9CeFWIsCHU711WkMY75nkvi7McRX1MO9D52caMHZYluYK8qMrMnr0KilLcOe/JfdhfQYig3raaFuDhlSxKxle6ZSSRtOXolmcunLltKoLUWXS2XyQbDp8tsmfLZvgQKMd9Y+3UUbWcFtete6zRDWF6WZ92ApPDWlzqlXFfB44goQaXYNOn2Yo1IeA==");
    }

    @Test
    public void testSignGenerateEd() throws Exception {
        var key = new License.Key("test", IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.id_25519"), StandardCharsets.UTF_8), "test");
        Assert.assertEquals(key.signatureGenerate(license.getDigest()),
                "6HAOoG5naN9QAQNOCuQbYcw+dFaQePRbWCdZdYSLPave7FWQ7cQSlYaa3BkuCk/r8LAQZn2GgTPuSuuoH2lbAQ==");
    }
}
