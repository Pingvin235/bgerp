package org.bgerp.app.dist.inst.call;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.bgerp.util.Log;

import ru.bgcrm.util.Setup;

public class RemoveFile implements InstallationCall {
    private static final Log log = Log.getLog();

    @Override
    public boolean call(Setup setup, File zip, String param) {
        boolean result = false;
        try {
            File file = new File(param);
            if (file.exists()) {
                log.info("Removing file: {}", param);
                FileUtils.deleteQuietly(file);
                result = true;
            }
        } catch (Exception e) {
            log.error(e);
        }
        return result;
    }
}
