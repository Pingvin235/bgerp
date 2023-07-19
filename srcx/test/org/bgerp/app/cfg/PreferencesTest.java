package org.bgerp.app.cfg;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.dao.ConfigDAO;
import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.Config;

public class PreferencesTest {

    @Test
    public void testLoad() {
        String data = "var1=value1\n"
                + "var1+=value2\n"
                + "var2=value4\n"
                + "var3=\n"
                + "index=1\n"
                + "object.{@index}.id=1\n"
                + "object.{@index}.title=Title1\n"
                + "object.{@inc:index}.id=2\n"
                + "object.{@index}.title=Title2\n"
                + "val=<<END\n"
                + "line1\n"
                + "line2\n"
                + "END\n"
                + "afterEnd=value8";

        Preferences prefs = new Preferences(data);
        assertEquals("value1value2", prefs.get("var1"));
        assertEquals("", prefs.get("var3"));
        assertEquals("1", prefs.get("object.1.id"));
        assertEquals("Title1", prefs.get("object.1.title"));
        assertEquals("2", prefs.get("object.2.id"));
        assertEquals("line1\nline2\n", prefs.get("val"));
    }

    @Test
    public void testOverwrite() throws Exception {
        String data = IOUtils.toString(this.getClass().getResourceAsStream("PreferencesTest.testOverwrite"), StandardCharsets.UTF_8);

        Preferences prefs = new Preferences(data);
        String value = prefs.get("rowConfig.1.stringExpressionRow");
        Assert.assertNotNull(value);
        Assert.assertTrue(value.trim().startsWith("tr = \"<tr"));
        Assert.assertTrue(value.trim().endsWith("return tr;"));
    }

    @Test
    public void testProcessIncludes() throws Exception {
        String dataInclude =
                "LIB=<<END\n"
                + "a = 1;\n"
                + "return a + 1;\n"
                + "END\n"
                + "key1=val1\n"
                + "key2={@key1}";

        String data =
                "include.1=1\n"
                + "key3= {@key2}\n"
                + "key4=val4\n"
                + "key5={@LIB}";

        ConfigDAO configDao = new ConfigDAO(null) {
            @Override
            public Config getGlobalConfig(int id) throws SQLException {
                if (id != 1)
                    return null;

                Config result = new Config();
                result.setData(dataInclude);

                return result;
            }
        };

        ConfigMap config = Preferences.processIncludes(configDao, data, true);

        Assert.assertEquals("val1", config.get("key1"));
        Assert.assertEquals("val1", config.get("key2"));
        Assert.assertEquals(" val1", config.get("key3"));
        Assert.assertEquals("val4", config.get("key4"));
        Assert.assertEquals("a = 1;\nreturn a + 1;\n", config.get("key5"));

        data =  "include.2=1\n";
        boolean exception = false;
        try {
            Preferences.processIncludes(configDao, data, true);
        } catch (BGMessageException e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        data = "include.1=1\n"
                + "key3= {@keyX}\n";
        exception = false;
        try {
            Preferences.processIncludes(configDao, data, true);
        } catch (BGMessageException e) {
            exception = true;
        }
        Assert.assertTrue(exception);
    }

    @Test
    public void testTerminatingNonPrintableChars() throws Exception {
        ConfigMap config = new Preferences(
                IOUtils.toString(this.getClass().getResourceAsStream("PreferencesTest.terminating.non.printable.chars.txt"), StandardCharsets.UTF_8));
        Assert.assertEquals(4320, config.getInt("sla:close.before.minutes"));
        Assert.assertEquals(800, config.getInt("sla:color.yellow.when.left.minutes"));

        config = config.sub("sla:");
        Assert.assertEquals(4320, config.getInt("close.before.minutes"));
        Assert.assertEquals(800, config.getInt("color.yellow.when.left.minutes"));
    }

}
