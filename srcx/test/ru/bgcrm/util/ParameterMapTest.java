package ru.bgcrm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.config.IsolationConfig;

public class ParameterMapTest {
    
    @Test
    public void testGetConfig() {
        ParameterMap config = new Preferences("isolation.process=executor\n");
        IsolationConfig isolation = config.getConfig(IsolationConfig.class);
        assertNotNull(isolation);
        assertEquals(IsolationConfig.IsolationProcess.EXECUTOR, isolation.getIsolationProcess());
    }
    
    @Test
    public void testValidateConfig() {
        boolean thrown = false;
        try {
            ParameterMap config = new Preferences("isolation.process=wrong\n");
            config.validateConfig(IsolationConfig.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

}
