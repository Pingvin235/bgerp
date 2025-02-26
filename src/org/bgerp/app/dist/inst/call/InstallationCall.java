package org.bgerp.app.dist.inst.call;

import java.io.File;

import org.bgerp.app.cfg.Setup;

public interface InstallationCall {
    public void call(Setup setup, File zip, String param) throws Exception;
}
