package org.bgerp.itest.kernel.user;

import java.util.Collections;

import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.dao.user.UserPermsetDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.user.Permset;
import ru.bgcrm.model.user.User;

@Test(groups = "user", dependsOnGroups = "dbInit")
public class UserTest {
    public static final int USER_ADMIN_ID = 1;

    public static volatile int paramPhoneId;
    public static volatile int paramCellPhoneId;
    public static volatile int paramEmailId;
    public static volatile int paramTelegramId;
    public static volatile int paramWebSiteId;

    public static volatile int permsetAdminsId;

    public static int pos = 0;

    @Test
    public void addParams() throws Exception {
        paramEmailId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_EMAIL, "E-Mail(s)", pos += 2, ParamTest.MULTIPLE, "");
        paramCellPhoneId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_PHONE, "Cell phone(s)", pos += 2, "", "");
    }

    @Test
    public void addPermissionSets() throws Exception {
        if (1 > 0) return;

        // TODO: Enable permissions check before.
        var con = DbTest.conRoot;

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
    }

    public static volatile int groupAdminsId;

    @Test
    public void addGroups() throws Exception {
        if (1 > 0) return;

        groupAdminsId = UserHelper.addGroup("Administrators", 0);
    }
}
