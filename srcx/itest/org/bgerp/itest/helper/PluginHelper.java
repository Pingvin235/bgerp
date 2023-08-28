package org.bgerp.itest.helper;

import org.bgerp.itest.kernel.db.DbTest;

import ru.bgcrm.plugin.Plugin;

public class PluginHelper {

    public static void initPlugin(Plugin p) throws Exception {
        var con = DbTest.conRoot;
        con.setAutoCommit(false);
        p.init(con);
        con.setAutoCommit(true);
    }

}