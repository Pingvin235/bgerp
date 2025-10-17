package org.bgerp.app.dist.lic;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LicenseTest {
    private License license;

    @Before
    public void init() throws Exception {
        license = new License(IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.lic.data"), StandardCharsets.UTF_8));
    }

    @Test
    public void testDigest() {
        Assert.assertEquals(64, license.getDigest().length);
    }

    @Test
    public void testErrors() {
        Assert.assertEquals("Signature is undefined", license.getError());
    }

    @Test
    public void testPlugins() {
        Assert.assertEquals(Set.of("bgbilling", "call.3sx"), license.getPlugins());
    }

    @Test
    public void testSignGenerateRsa() throws Exception {
        var key = new Sign("test", IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.id_rsa"), StandardCharsets.UTF_8), null);
        final String sign = "Z/Sc86AumMqpONn6MRmeK9aOLrsta7wX8R9eyl/vV7K3w3lm/LD2DAsv2BuwDvOUyMoVd134Ml/qfHvqgPy5WI8PbH0ufZ1ttXIsFDkNsAcL8sZ/Br7WCu3uBWLHo0rKr200GpqJph1SaGpVjmbuvej2MEZ3WBardm/RFye/iNd+zZIvjKFu0wGjiTBVA4wt1v8GautV2bz5U5D5d+MyzCsFTBBnu/tQginRyUFq9rirH0g+4IciyPkeuQMpNYYA06Tg8wNL9MXRRPUEeZu2P/1coR0qtTR9hnhl9FbXcTzy+7Ie9fZUnH3habpnfns/u3wJ72LPHrLWvXaFjB6zZA==";
        Assert.assertEquals(sign, key.signatureGenerate(license.getDigest()));

        key = new Sign("test", IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.id_rsa.pub"), StandardCharsets.UTF_8));
        Assert.assertTrue(key.signatureVerify(license.getDigest(), sign));
    }

    @Test
    public void testSignGenerateEd() throws Exception {
        var key = new Sign("test", IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.id_25519"), StandardCharsets.UTF_8), "test");
        final String sign = "YEJC/4KS9CK6PQuw9f8HXJzGAqNt0ufRqkHTiYAdEduGz8EweAP3um24s1n7xvMC4fUIZx11NhX5FkAQUGjEDA==";
        Assert.assertEquals(sign, key.signatureGenerate(license.getDigest()));

        key = new Sign("test", IOUtils.toString(this.getClass().getResourceAsStream("LicenseTest.id_25519.pub"), StandardCharsets.UTF_8));
        Assert.assertTrue(key.signatureVerify(license.getDigest(), sign));
    }
}
