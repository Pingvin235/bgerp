package ru.bgcrm.model.param;

import org.junit.Assert;
import org.junit.Test;

public class ParameterEmailValueTest {
    @Test
    public void testToString() {
        String emails = "Ivan Drago <ivan@bgerp.org>, error, somebody@bgerp.org";
        Assert.assertEquals("Ivan Drago <ivan@bgerp.org>, somebody@bgerp.org", ParameterEmailValue.toString(ParameterEmailValue.of(emails)));
    }
}
