package org.bgerp.exec;

import org.bgerp.app.exec.Runnable;
import org.bgerp.util.Log;

import ru.bgcrm.dao.FileDataDAO;
import ru.bgcrm.util.Setup;

/**
 * Migrates old files in 'filestorage' to sub-directories yyyy/MM/dd.
 *
 * @author Shamil Vakhitov
 */
public class MoveToSubDirs implements Runnable {
    private static final Log log = Log.getLog();

    private static final int BATCH_SIZE = 100;

    @Override
    public void run() {
        boolean run = true;
        while (run) {
            try (var con = Setup.getSetup().getDBSlaveConnectionFromPool()) {
                run = new FileDataDAO(con).moveBatch(BATCH_SIZE);
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

}
