package org.bgerp.itest.configuration.company.department.support;

import static org.bgerp.itest.kernel.user.UserTest.USER_ADMIN_ID;

import java.util.Date;

import org.bgerp.itest.configuration.company.department.development.DevelopmentTest;
import org.bgerp.itest.helper.ProcessHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.bgerp.itest.helper.UserHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import ru.bgcrm.model.user.UserGroup;

@Test(groups = "depSupport", priority = 200, dependsOnGroups = { "configProcessNotification", "param", "depDev" })
public class SupportTest {
    public static volatile int groupId;

    public void addGroups() throws Exception {
        groupId = UserHelper.addGroup("Support", 0);
        UserHelper.addUserGroups(USER_ADMIN_ID, Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test(dependsOnMethods = "addGroups")
    public void addUsers() throws Exception {
        UserHelper.addUser("Feliks Dserschinski", "felix", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
        UserHelper.addUser("Vyacheslav Menzhinsky", "vyacheslav", Lists.newArrayList(new UserGroup(groupId, new Date(), null)));
    }

    @Test (dependsOnMethods = "addGroups")
    public void addQueues() throws Exception {
        Assert.assertTrue(DevelopmentTest.processTypeSupportId > 0);
        
        var queueId = ProcessHelper.addQueue("Support", ResourceHelper.getResource(this, "queue.txt"), Sets.newHashSet(DevelopmentTest.processTypeSupportId));
        UserHelper.addGroupQueues(groupId, Sets.newHashSet(queueId));
        
        // TODO: Saved filters and counters.
    }

    @Test (dependsOnMethods = "addUsers")
    public void addProcesses() throws Exception {
        // TODO: Cases
        // 1) Send documentation link.
        // 2) Not assigned yet process.
        // 3) Connected development.
    }
}