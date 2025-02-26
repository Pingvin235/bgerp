package org.bgerp.app.dist.inst.call;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bgerp.app.cfg.Setup;
import org.bgerp.util.Log;

public class RemoveFile implements InstallationCall {
    private static final Log log = Log.getLog();

    @Override
    public void call(Setup setup, File zip, String param) {
        File file = new File(param);
        if (file.exists()) {
            log.info("Removing file: {}", param);
            FileUtils.deleteQuietly(file);
        }
    }
}
