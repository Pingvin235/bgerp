package org.bgerp.itest.configuration.department.it;

import org.testng.annotations.Test;

@Test(groups = "depIt", priority = 200, dependsOnGroups = { "configProcessNotification", "param" })
public class ItTest {
    /*  Process type about equipment problems, for IT department. */
    public static volatile int groupId;

    /* public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("IT", 0);
        UserHelper.addUserGroups(USER_ADMIN_ID, Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    } */
}