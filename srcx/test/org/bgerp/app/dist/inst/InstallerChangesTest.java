package org.bgerp.app.dist.inst;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

public class InstallerChangesTest {
    @Test
    public void testChanges() throws Exception {
        var processor = new InstallerChanges() {
            @Override
            protected Document changes(String url) throws IOException {
                return Jsoup.parse(IOUtils.toString(
                        InstallerModulesTest.class.getResourceAsStream("InstallerChangesTest.changes.html"),
                        StandardCharsets.UTF_8));
            }
        };

        var changes = processor.getChanges();
        Assert.assertEquals(13, changes.size());

        Assert.assertEquals("0", changes.get(0).getId());
        Assert.assertEquals("0 12-Mar-2022 17:25", changes.get(0).getTitle());

        Assert.assertEquals("14350", changes.get(1).getId());
        Assert.assertEquals("14350 18-Mar-2022 12:32", changes.get(1).getTitle());

        Assert.assertEquals("14299", changes.get(12).getId());
        Assert.assertEquals("14299 15-Feb-2022 10:47", changes.get(12).getTitle());
    }

    @Test
    public void testChange() throws Exception {
        var cnt = new AtomicInteger();

        var processor = new InstallerChanges("14353") {
            @Override
            protected Document changes(String url) throws IOException {
                Assert.assertEquals("https://bgerp.org/change/14353", url);
                cnt.incrementAndGet();
                return Jsoup.parse(IOUtils.toString(
                        InstallerModulesTest.class.getResourceAsStream("InstallerChangesTest.change.html"),
                        StandardCharsets.UTF_8));
            }

            @Override
            protected void download(String url, String href) throws IOException, URISyntaxException {
                cnt.incrementAndGet();
                Assert.assertEquals("https://bgerp.org/change/14353", url);
                Assert.assertEquals("update_3.0_1385.zip", href);
            }
        };

        Assert.assertEquals("Incorrect calls count", 2, cnt.get());

        var files = processor.getUpdateFiles();
        Assert.assertEquals(1, files.size());
    }
}
