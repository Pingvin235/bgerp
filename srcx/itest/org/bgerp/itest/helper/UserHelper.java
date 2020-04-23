package org.bgerp.itest.helper;

import org.testng.Assert;

import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.model.user.Group;

public class UserHelper {

    public static int addGroup(UserGroupDAO dao, String title, int parentId) throws Exception {
        Group group = new Group();
        group.setTitle(title);
        group.setComment("");
        group.setConfig("");
        group.setParentId(parentId);
        dao.updateGroup(group);
        Assert.assertTrue(group.getId() > 0);
        return group.getId();
    }
    
}