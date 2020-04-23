package org.bgerp.itest.configuration.company.branch.software;

import org.testng.annotations.Test;

@Test(groups = "softwareDevelopment", dependsOnGroups = { "processInit", "blowInit", "paramInit" })
public class Development {
    public static volatile int permsetDevId;
    public static volatile int groupDevId;
    
    @Test
    public void init() {
        // deadline, next termin
        
        /*var props = new TypeProperties();
        props.setStatusIds(statusIds);
        props.setCreateStatus(statusCreateId);
        props.setCloseStatusIds(statusCloseIds);
        props.setConfig(config);
        type.setProperties(props);*/
    }
}