package org.bgerp.itest.kernel.process;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import org.bgerp.itest.helper.ProcessHelper;
import org.testng.annotations.Test;

import ru.bgcrm.dao.process.StatusDAO;

@Test(groups = "processInit", dependsOnGroups = "dbInit")
public class InitTest {
    public static volatile int statusOpen;
    public static volatile int statusToDo;
    public static volatile int statusProgress;
    public static volatile int statusWait;
    public static volatile int statusDoc;
    public static volatile int statusDone;
    public static volatile int statusRejected;

    @Test
    public void addStatuses() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new StatusDAO(con);
            
            int pos = 0;
            statusOpen = ProcessHelper.addStatus(dao, "Open", pos += 2);
            statusToDo = ProcessHelper.addStatus(dao, "ToDo (postponed)", pos += 2);
            statusProgress = ProcessHelper.addStatus(dao, "Progress", pos += 2);
            statusWait = ProcessHelper.addStatus(dao, "Wait for..", pos += 2);
            statusDoc = ProcessHelper.addStatus(dao, "Doc", pos += 2);
            statusDone = ProcessHelper.addStatus(dao, "Done", pos += 2);
            statusRejected = ProcessHelper.addStatus(dao, "Rejected", pos += 2);

            con.commit();
        }
    }

    @Test
    public void addTypes() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            
        }
    }

}
