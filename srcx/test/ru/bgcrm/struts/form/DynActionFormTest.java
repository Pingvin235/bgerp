package ru.bgcrm.struts.form;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.model.BGIllegalArgumentException;
import ru.bgcrm.util.Utils;

public class DynActionFormTest {
    @Test
    public void testParamsToQueryString() throws Exception {
        var form = new DynActionForm("a=1&a=2&b=4");
        String paramsQuery = form.paramsToQueryString();

        Assert.assertTrue(paramsQuery.contains("a=1"));
        Assert.assertTrue(paramsQuery.contains("a=2"));
        Assert.assertTrue(paramsQuery.contains("b=4"));
        Assert.assertEquals(11, paramsQuery.length());

        form = new DynActionForm();
        String value = "=+12-";
        form.setParam("a", value);
        paramsQuery = form.paramsToQueryString();

        Assert.assertEquals("a=" + URLEncoder.encode(value, StandardCharsets.UTF_8.name()), paramsQuery);

        form = new DynActionForm(paramsQuery);

        Assert.assertEquals(value, form.getParam("a"));
    }

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
    public void testGetOverwrittenParam() {
        var form = new DynActionForm("url?a=1&!a=2");
        Assert.assertEquals("2", form.getParam("a"));

        form.set("a", new String[] { "val1" });
        Assert.assertEquals("val1", form.getParam("a"));

        form.set("!a", new String[] { "val1", "val2" });
        Assert.assertEquals("val2", form.getParam("a"));
        Assert.assertNull(form.getParam("!a"));
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
