package ru.bgcrm.struts.form;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.util.Utils;

public class DynActionFormTest {
    @Test
    public void testGetParam() {
        var form = new DynActionForm("url?a=1&k=&");
        checkGetParam(form);

        form = new DynActionForm();
        form.set("a", new String[] { "1" });
        form.set("k", new String[] { "" });
        checkGetParam(form);
    }

    private void checkGetParam(DynActionForm form) {
        Assert.assertEquals("1", form.getParam("a"));
        Assert.assertNull(form.getParam("b"));
        Assert.assertEquals("", form.getParam("k"));
        Assert.assertEquals("tval", form.getParam("t", "tval"));
    }

    @Test
    public void testGetSelectedValues() {
        var form = new DynActionForm("url?a=1&a=2&");
        checkGetSelectedValues(form);

        form = new DynActionForm();
        form.set("a", new String[] { "1", "2" });
        checkGetSelectedValues(form);
    }

    private void checkGetSelectedValues(DynActionForm form) {
        Assert.assertEquals(Set.of(2, 1), form.getSelectedValues("a"));
        Assert.assertEquals(List.of(1, 2), form.getSelectedValuesList("a"));
    }

    @Test
    public void testGetSelectedValuesStr() {
        var form = new DynActionForm();
        form.set("a", new String[] { "v1", "", "v2", null });
        Assert.assertEquals(Set.of("v1", "v2"), form.getSelectedValuesStr("a"));
    }

    @Test
    public void testGetParamValidate() {
        var form = new DynActionForm("url?a=1&k=&");
        checkGetParamValidate(form);

        form = new DynActionForm();
        form.set("a", new String[] { "1" });
        form.set("k", new String[] { "" });
        checkGetParamValidate(form);
    }

    private void checkGetParamValidate(DynActionForm form) {
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
