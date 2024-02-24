package org.bgerp.app.dist.inst;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Test;

public class InstallerModulesTest {
    @Test
    public void testLoadRemoteFiles() {
        var p = new InstallerModules("3.0") {
            @Override
            protected Document getRemoteHtml(String updateUrl) throws IOException {
                return Jsoup.parse(IOUtils.toString(
                        InstallerModulesTest.class.getResourceAsStream("InstallerModulesTest.download.html"),
                        StandardCharsets.UTF_8));
            }
        };

        var map = p.getRemoteFileMap();
        Assert.assertEquals(2, map.size());

        var update = map.get(InstalledModule.MODULE_UPDATE);
        Assert.assertEquals("https://bgerp.org/release/3.0/update_3.0_1373.zip", update.url.toString());
        Assert.assertEquals("1373", update.buildNumber);
        Assert.assertEquals("update_3.0_1373.zip", update.fileName);

        var updateLib = map.get(InstalledModule.MODULE_UPDATE_LIB);
        Assert.assertEquals("https://bgerp.org/release/3.0/update_lib_3.0_77.zip", updateLib.url.toString());
        Assert.assertEquals("77", updateLib.buildNumber);
        Assert.assertEquals("update_lib_3.0_77.zip", updateLib.fileName);
    }
}
