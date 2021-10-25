package org.bgerp.itest.kernel.iface.user;

import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.bgerp.itest.kernel.db.DbTest;
import org.testng.annotations.Test;

import ru.bgcrm.cache.UserCache;
import ru.bgcrm.dao.user.UserDAO;

@Test(groups = "userIface", dependsOnGroups = { "user" })
public class UserIfaceTest {

    @Test
    public void addUser() throws Exception {
        var user = UserHelper.addUser("Kernel User Iface", "uiface", null);

        user.setConfig(ResourceHelper.getResource(this, "user.config.txt"));
        new UserDAO(DbTest.conRoot).updateUser(user);

        UserCache.flush(null);
    }
}
