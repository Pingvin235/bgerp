package org.bgerp.util;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;

public class URLTotalEncoderTest {
    @Test
    public void testEncode() {
        Assert.assertEquals("%D0%94%D0%BE%D0%B3%D0%BE%D0%B2%D0%BE%D1%80%20%E2%84%96" + //
                "%20%35%30%30%30%35%30%33%38%34%39%37%20%28%D0%AF%D0%BD%D0%B2" + //
                "%D0%B0%D1%80%D1%8C%29%2E%78%6C%73%78", URLTotalEncoder.encode("Договор № 50005038497 (Январь).xlsx", StandardCharsets.UTF_8));
    }
}
