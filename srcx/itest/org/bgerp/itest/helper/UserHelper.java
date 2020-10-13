package org.bgerp.itest.helper;

import java.util.Set;

import org.bgerp.itest.kernel.db.DbTest;
import org.testng.Assert;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;

public class UserHelper {
    public static final String GROUP_CONFIG_ISOLATION = "\nisolation.process=group\n";

    public static int addGroup(String title, int parentId) throws Exception {
        var con = DbTest.conRoot;

        Group group = new Group();
        group.setTitle(title);
        group.setComment("");
        group.setConfig(GROUP_CONFIG_ISOLATION);
        group.setParentId(parentId);
        new UserGroupDAO(con).updateGroup(group);

        Assert.assertTrue(group.getId() > 0);

        return group.getId();
    }

    public static void addGroupQueues(int groupId, Set<Integer> queueIds) throws Exception {
        var con = DbTest.conRoot;

        var dao = new UserGroupDAO(con);
        var group = dao.getGroupById(groupId);
        
        Assert.assertNotNull(group);
        
        group.getQueueIds().addAll(queueIds);
        
        dao.updateGroup(group);
    }

    public static int addUser(String title, String login, Iterable<UserGroup> groups) throws Exception {
        var con = DbTest.conRoot;

        var dao = new UserDAO(con);
        
        var user = new User();
        user.setTitle(title);
        user.setLogin(login);
        user.setPassword(login);
        dao.updateUser(user);

        Assert.assertTrue(user.getId() > 0);

        if (groups != null)
            for (var group : groups)
                dao.addUserGroup(user.getId(), group);

        return user.getId();
    }

    public static void addUserGroups(int userId, Iterable<UserGroup> groups) throws Exception {
        var con = DbTest.conRoot;

        var dao = new UserDAO(con);
        
        for (var group : groups)
            dao.addUserGroup(userId, group);
    }
    
}