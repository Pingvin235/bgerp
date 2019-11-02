package ru.bgerp.itest;

import org.testng.Assert;
import org.testng.annotations.Test;

@Test (dependsOnGroups = "runServer")
public class InitUsersTest {
    
    @Test
    public void addUsers() {
        Assert.assertEquals(true, true);
    }

}
