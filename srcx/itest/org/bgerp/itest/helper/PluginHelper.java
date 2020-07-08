package org.bgerp.itest.helper;

import org.bgerp.itest.kernel.db.DbTest;

import ru.bgcrm.plugin.Plugin;

public class PluginHelper {

    public static String initPlugin(Plugin p) throws Exception {
        try (var con = DbTest.conPoolRoot.getDBConnectionFromPool()) {
            p.init(con);
            return p.getName() + ":enable=1\n\n";
        }
    }

}