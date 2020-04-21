package ru.bgcrm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    
    @Test
    public void testOf() {
        ParameterMap map = ParameterMap.of("key1", "1", "key2", "value2", "key3");
        assertEquals(2, map.size());
        assertEquals(1, map.getInt("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testSok() throws Exception {
        var map = ParameterMap.of("key.old", "1", "key.new", "2");
        var value = map.getSok("key.new", "key.old");
        assertEquals("2", value);

        map = ParameterMap.of("key.old", "1");
        value = map.getSok("key.new", "key.old");
        assertEquals("1", value);

        value = map.getSok("key.wrong1", "key.wrong2");
        assertNull(value);

        value = map.getSok("default", false, "key.wrong");
        assertEquals("default", value);
        
        var thrown = false;
        try {
            map = ParameterMap.of("key.old", "0");
            map.getSok(null, true, "key.new", "key.old");
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

}
