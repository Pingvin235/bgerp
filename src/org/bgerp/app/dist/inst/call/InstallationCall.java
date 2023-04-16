package org.bgerp.app.dist.inst.call;

import java.io.File;

import ru.bgcrm.util.Setup;

public interface InstallationCall {
    public boolean call(Setup setup, File zip, String param);
}
