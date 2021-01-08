package ru.bgcrm.struts.form;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.util.Utils;

public class DynActionFormTest {
    @Test
    public void testGetParam() {
        var form = new DynActionForm("url?a=1");
        Assert.assertEquals("1", form.getParam("a"));
        Assert.assertNull(form.getParam("b"));
        
        BGIllegalArgumentException e = null;
        try {
            form.getParam("b", Utils::notBlankString);
        } catch (BGIllegalArgumentException ex) {
            e = ex;
        }
        Assert.assertNotNull(e);
        Assert.assertEquals("b", e.getName());

        e = null;
        try {
            form.getParam("a", v -> !"1".equals(v) );
        } catch (BGIllegalArgumentException ex) {
            e = ex;
        }
        Assert.assertNotNull(e);
        Assert.assertEquals("a", e.getName());
    }
}
