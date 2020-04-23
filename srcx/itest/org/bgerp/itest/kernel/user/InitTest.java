package org.bgerp.itest.kernel.user;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import java.util.Collections;

import org.bgerp.itest.helper.UserHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserGroupDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.model.user.User;

@Test(groups = "initUser", dependsOnGroups = "dbInit")
public class InitTest {
    private static final int USER_ADMIN_ID = 1;

    public static volatile int permsetAdminsId;

    @Test
    public void addPermissionSets() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var admins = new Permset();
            admins.setTitle("Administrators");
            admins.setComment("Full access, check all permissions");
            new UserPermsetDAO(con).updatePermset(admins);

            permsetAdminsId = admins.getId();
            Assert.assertTrue(permsetAdminsId > 0);

            con.commit();
        }
    }

    public static volatile int groupManagementId;
    public static volatile int groupSupportId;

    @Test(dependsOnMethods = { "addPermissionSets"})
    public void addGroups() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var dao = new UserGroupDAO(con);
            
            groupManagementId = UserHelper.addGroup(dao, "Management", 0);
            groupSupportId = UserHelper.addGroup(dao, "Support", 0);

            con.commit();
        }
    }

    @Test(dependsOnMethods = { "addPermissionSets", "addGroups" })
    public void addUsers() throws Exception {
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var userDao = new UserDAO(con);

            User admin = userDao.getUser(USER_ADMIN_ID);
            Assert.assertNotNull(admin);
            admin.setPermsetIds(Collections.singletonList(permsetAdminsId));
            userDao.updateUser(admin);

            con.commit();
        }
    }
}
