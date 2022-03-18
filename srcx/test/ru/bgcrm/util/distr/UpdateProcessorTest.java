package ru.bgcrm.util.distr;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

public class UpdateProcessorTest {
    @Test
    public void testChanges() throws IOException {
        var processor = new UpdateProcessor() {
            @Override
            protected Document changes(String url) throws IOException {
                return Jsoup.parse(IOUtils.toString(
                        InstallProcessorTest.class.getResourceAsStream("UpdateProcessorTest.changes.html"),
                        StandardCharsets.UTF_8));
            }
        };

        var changes = processor.getChanges();
        Assert.assertEquals(13, changes.size());

        Assert.assertEquals("00000", changes.get(0).getId());
        Assert.assertEquals("00000 12-Mar-2022 17:25", changes.get(0).getTitle());

        Assert.assertEquals("14748", changes.get(12).getId());
        Assert.assertEquals("14748 12-Mar-2022 17:30", changes.get(12).getTitle());
    }

    @Test
    public void testChange() throws IOException {
        var cnt = new AtomicInteger();

        var processor = new UpdateProcessor("14353") {
            @Override
            protected Document changes(String url) throws IOException {
                Assert.assertEquals("https://bgerp.org/update/14353", url);
                cnt.incrementAndGet();
                return Jsoup.parse(IOUtils.toString(
                        InstallProcessorTest.class.getResourceAsStream("UpdateProcessorTest.change.html"),
                        StandardCharsets.UTF_8));
            }

            @Override
            protected void download(String url, String href) throws IOException, MalformedURLException {
                cnt.incrementAndGet();
                Assert.assertEquals("https://bgerp.org/update/14353", url);
                Assert.assertEquals("update_3.0_1385.zip", href);
            }
        };

        Assert.assertEquals("Incorrect calls count", 2, cnt.get());

        var files = processor.getUpdateFiles();
        Assert.assertEquals(1, files.size());
    }
}
