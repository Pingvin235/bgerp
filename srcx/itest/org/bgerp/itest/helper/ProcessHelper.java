package org.bgerp.itest.helper;

import org.testng.Assert;

import ru.bgcrm.dao.process.ProcessTypeDAO;
import ru.bgcrm.dao.process.StatusDAO;
import ru.bgcrm.model.process.ProcessType;
import ru.bgcrm.model.process.Status;
import ru.bgcrm.model.process.TypeProperties;
import ru.bgcrm.model.user.User;

public class ProcessHelper {

    public static int addStatus(StatusDAO dao, String title, int pos) throws Exception {
        var status = new Status(-1, title);
        status.setPos(pos);
        dao.updateStatus(status);
        Assert.assertTrue(status.getId() > 0);
        return status.getId();
    }
    
    public static int addType(ProcessTypeDAO dao, String title, int parentId, boolean useParentProperties,TypeProperties props) throws Exception{
        var type = new ProcessType(-1, title);
        type.setParentId(parentId);
        type.setUseParentProperties(useParentProperties);
        dao.updateProcessType(type, User.USER_SYSTEM_ID);
        Assert.assertTrue(type.getId() > 0);
        
        if (!useParentProperties) {
            type.setProperties(props);
            dao.updateTypeProperties(type);
        }
        
        return type.getId();
    }
}