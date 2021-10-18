package org.bgerp.itest.helper;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
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
        Group group = new Group();
        group.setTitle(title);
        group.setComment("");
        group.setConfig(GROUP_CONFIG_ISOLATION);
        group.setParentId(parentId);
        new UserGroupDAO(DbTest.conRoot).updateGroup(group);

        Assert.assertTrue(group.getId() > 0);

        return group.getId();
    }

    public static void addGroupQueues(int groupId, Set<Integer> queueIds) throws Exception {
        var dao = new UserGroupDAO(DbTest.conRoot);
        var group = dao.getGroupById(groupId);

        Assert.assertNotNull(group);

        group.getQueueIds().addAll(queueIds);

        dao.updateGroup(group);
    }

    public static User addUser(String title, String login, Iterable<UserGroup> groups) throws Exception {
        var dao = new UserDAO(DbTest.conRoot);

        var user = new User();
        user.setTitle(title);
        user.setLogin(login);
        user.setPassword(login);
        dao.updateUser(user);

        Assert.assertTrue(user.getId() > 0);

        if (groups != null)
            for (var group : groups)
                dao.addUserGroup(user.getId(), group);

        return user;
    }

    public static final void addUserProcessQueues(int userId, Collection<Integer> queueIds) throws SQLException {
        var dao = new UserDAO(DbTest.conRoot);

        var user = dao.getUser(userId);

        Assert.assertNotNull(user);

        user.getQueueIds().addAll(queueIds);

        dao.updateUser(user);
    }

    public static void addUserGroups(int userId, Iterable<UserGroup> groups) throws Exception {
        var dao = new UserDAO(DbTest.conRoot);
        for (var group : groups)
            dao.addUserGroup(userId, group);
    }

    public static void addUserGroups(int userId, int... groupIds) throws Exception {
        var dao = new UserDAO(DbTest.conRoot);
        for (var groupId : groupIds)
            dao.addUserGroup(userId, new UserGroup(groupId, new Date(), null));
    }

}