package org.bgerp.itest.kernel.param;

import org.bgerp.itest.helper.ConfigHelper;
import org.bgerp.itest.helper.ResourceHelper;
import org.testng.annotations.Test;

@Test(groups = "param", dependsOnGroups = { "config", "address" })
public class ParamTest {
    public static final String MULTIPLE = "multiple=1";
    public static final String SAVE_ON_FOCUS_LOST = "saveOn=focusLost";
    public static final String ENCRYPTED = "encrypt=encrypted";
    public static final String READONLY = "readonly=1";
    public static final String LIST_VALUES_123 = "1=Value1\n2=Value2\n3=Value3\n";

    @Test
    public void addConfig() throws Exception {
        ConfigHelper.addIncludedConfig("Kernel Parameters", ResourceHelper.getResource(this, "config.txt"));
    }
}
