package org.bgerp.itest.helper;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import org.testng.Assert;

import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;

public class ProcessHelper {

    public static int addStatus(String title, int pos) throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var status = new Status(-1, title);
            status.setPos(pos);
            new StatusDAO(con).updateStatus(status);
            Assert.assertTrue(status.getId() > 0);

            con.commit();

            return status.getId();
        }
    }
    
    public static int addType(String title, int parentId, boolean useParentProperties,TypeProperties props) throws Exception{
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new ProcessTypeDAO(con);

            var type = new ProcessType(-1, title);
            type.setParentId(parentId);
            type.setUseParentProperties(useParentProperties);
            dao.updateProcessType(type, User.USER_SYSTEM_ID);
            Assert.assertTrue(type.getId() > 0);
            
            if (!useParentProperties) {
                type.setProperties(props);
                dao.updateTypeProperties(type);
            }

            con.commit();
            
            return type.getId();
        }
    }
}