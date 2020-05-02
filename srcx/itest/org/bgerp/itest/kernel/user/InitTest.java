package org.bgerp.itest.kernel.user;

import static org.bgerp.itest.kernel.db.DbTest.conPoolRoot;

import java.util.Collections;
import java.util.Date;

import com.google.common.collect.Lists;

import org.bgerp.itest.helper.UserHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;

@Test(groups = "initUser", dependsOnGroups = "dbInit")
public class InitTest {
    private static final int USER_ADMIN_ID = 1;

    public static volatile int permsetAdminsId;

    @Test
    public void addPermissionSets() throws Exception {
        // TODO: Enable permissions check before.
        try (var con = conPoolRoot.getDBConnectionFromPool()) {
            var admins = new Permset();
            admins.setTitle("Administrators");
            admins.setComment("Full access, check all permissions");
            new UserPermsetDAO(con).updatePermset(admins);

            permsetAdminsId = admins.getId();
            Assert.assertTrue(permsetAdminsId > 0);

            var userDao = new UserDAO(con);

            User admin = userDao.getUser(USER_ADMIN_ID);
            Assert.assertNotNull(admin);
            admin.setPermsetIds(Collections.singletonList(permsetAdminsId));
            userDao.updateUser(admin);

            con.commit();
        }
    }

    public static volatile int groupManagementId;
    public static volatile int groupSupportId;

    @Test(dependsOnMethods = { "addPermissionSets" })
    public void addGroups() throws Exception {
        groupManagementId = UserHelper.addGroup("Management", 0, null);
        groupSupportId = UserHelper.addGroup("Support", 0, null);
    }

    @Test(dependsOnMethods = { "addPermissionSets", "addGroups" })
    public void addUsers() throws Exception {
        UserHelper.addUser("Karl Marx", "karl", Lists.newArrayList(new UserGroup(groupManagementId, new Date(), null)));
    }
}
