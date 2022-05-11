package org.bgerp.itest.kernel.user;

import java.util.Date;

import org.bgerp.itest.helper.ParamHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.bgerp.itest.kernel.param.ParamTest;
import org.testng.annotations.Test;

import ru.bgcrm.dao.ParamValueDAO;
import ru.bgcrm.dao.user.UserDAO;
import ru.bgcrm.model.param.Parameter;
import ru.bgcrm.model.param.ParameterEmailValue;
import ru.bgcrm.model.user.User;
import ru.bgcrm.model.user.UserGroup;

@Test(groups = "user", dependsOnGroups = "dbInit")
public class UserTest {
    public static final int USER_ADMIN_ID = 1;

    public static volatile int paramPhoneId;
    public static volatile int paramCellPhoneId;
    public static volatile int paramExtensionId;
    public static volatile int paramEmailId;
    public static volatile int paramTelegramId;
    public static volatile int paramWebSiteId;

    public static volatile int groupAdminsId;
    public static volatile int permsetAdminsId;

    public static int paramPos = 0;

    public static volatile int userKarlId;
    public static volatile int userFriedrichId;
    public static volatile int userVladimirId;
    public static volatile int userLeonId;
    public static volatile int userFelixId;
    public static volatile int userVyacheslavId;

    @Test
    public void param() throws Exception {
        paramEmailId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_EMAIL, "E-Mail(s)", paramPos += 2, ParamTest.MULTIPLE, "");
        paramCellPhoneId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_PHONE, "Cell phone(s)", paramPos += 2, "", "");
        paramExtensionId = ParamHelper.addParam(User.OBJECT_TYPE, Parameter.TYPE_TEXT, "Extension number", paramPos+=2, "", "");
    }

    @Test(dependsOnMethods = "param")
    public void user() throws Exception {
        userKarlId = UserHelper.addUser("Karl Marx", "karl", null).getId();
        userFriedrichId = UserHelper.addUser("Friedrich Engels", "friedrich", null).getId();
        userVladimirId = UserHelper.addUser("Vladimir Lenin", "vladimir", null).getId();
        userLeonId = UserHelper.addUser("Leon Trotsky", "leon", null).getId();
        userFelixId = UserHelper.addUser("Feliks Dserschinski", "felix", null).getId();
        userVyacheslavId = UserHelper.addUser("Vyacheslav Menzhinsky", "vyacheslav", null).getId();
    }

    @Test(dependsOnMethods = {"param", "user"})
    public void userParam() throws Exception {
        var dao = new ParamValueDAO(DbTest.conRoot);
        dao.updateParamEmail(USER_ADMIN_ID, paramEmailId, 0, new ParameterEmailValue("admin@bgerp.org"));
        dao.updateParamText(USER_ADMIN_ID, paramExtensionId, "101");
    }

    /* TODO: Enable permission check before.
    @Test
    public void permissionSet() throws Exception {
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
    } */

    @Test
    public void group() throws Exception {
        groupAdminsId = UserHelper.addGroup("Administrators", 0, "");

        var dao = new UserDAO(DbTest.conRoot);
        dao.addUserGroup(USER_ADMIN_ID, new UserGroup(groupAdminsId, new Date(), null));
    }
}
