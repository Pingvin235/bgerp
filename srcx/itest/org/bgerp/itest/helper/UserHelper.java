package org.bgerp.itest.helper;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import java.util.Set;

import org.testng.Assert;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.model.user.Group;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;

public class UserHelper {

    public static int addGroup(String title, int parentId, Set<Integer> queueIds) throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            Group group = new Group();
            group.setTitle(title);
            group.setComment("");
            group.setConfig("");
            group.setParentId(parentId);
            if (queueIds != null)
                group.setQueueIds(queueIds);
            new UserGroupDAO(con).updateGroup(group);

            Assert.assertTrue(group.getId() > 0);

            con.commit();

            return group.getId();
        }
    }

    public static int addUser(String title, String login, Iterable<UserGroup> groups) throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
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

            con.commit();

            return user.getId();
        }
    }
    
}