package ru.bgcrm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ru.bgcrm.model.BGMessageException;
import ru.bgcrm.model.config.IsolationConfig;

public class ParameterMapTest {
    
    @Test
    public void testGetConfig() {
        var config = ParameterMap.of("isolation.process", "executor");
        IsolationConfig isolation = config.getConfig(IsolationConfig.class);
        assertNotNull(isolation);
        assertEquals(IsolationConfig.IsolationProcess.EXECUTOR, isolation.getIsolationProcess());
    }
    
    @Test
    public void testValidateConfig() {
        boolean thrown = false;
        try {
            var config = ParameterMap.of("isolation.process", "wrong");
            config.validateConfig(IsolationConfig.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    private static class TestConfigCache extends Config {
        private static final AtomicInteger constructorCalled = new AtomicInteger();

        protected TestConfigCache(ParameterMap setup, boolean validate) {
            super(setup, validate);
            constructorCalled.incrementAndGet();
        }
    }

    @Test
    public void testConfigCache() {
        var params = ParameterMap.EMPTY;
        var config = params.getConfig(TestConfigCache.class);
        assertNotNull(config);
        assertEquals(1, TestConfigCache.constructorCalled.get());
        
        config = params.getConfig(TestConfigCache.class);
        assertNotNull(config);
        assertEquals(1, TestConfigCache.constructorCalled.get());
        
        params.removeConfig(TestConfigCache.class);
        config = params.getConfig(TestConfigCache.class);
        assertNotNull(config);
        assertEquals(2, TestConfigCache.constructorCalled.get());
    }

    private static class TestConfigValidate extends Config {
        private final String value;

        protected TestConfigValidate(ParameterMap setup, boolean validate) throws Exception {
            super(setup, validate);
            if ((value = setup.getSok(null, validate, "key.new", "key.old")) == null)
                throwValidationException("Validation error");
        }
    }

    @Test
    public void testValidateConfigGet() {
        var setup = ParameterMap.EMPTY;
        var config = setup.getConfig(TestConfigValidate.class);
        assertNull(config);

        setup = ParameterMap.of("key.wrong", "value");
        config = setup.getConfig(TestConfigValidate.class);
        assertNull(config);

        setup = ParameterMap.of("key.old", "value");
        config = setup.getConfig(TestConfigValidate.class);
        assertNotNull(config);
        assertEquals("value", config.value);

        boolean thrown = false;
        try {
            setup = ParameterMap.of("key.new", "value");
            setup.validateConfig(TestConfigValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertFalse(thrown);

        try {
            setup = ParameterMap.of("key.old", "value");
            setup.validateConfig(TestConfigValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    private static class TestConfigInit extends Config {
        private final String value;

        protected TestConfigInit(ParameterMap setup, boolean validate) throws Exception {
            super(setup, validate);
            initWhen(setup.getBoolean("config.init", false));
            if ((value = setup.getSok(null, validate, "key.new", "key.old")) == null)
                throwValidationException("Validation error");
        }
    }

    @Test
    public void testConfigInit() throws Exception {
        var setup = ParameterMap.EMPTY;
        var config = setup.getConfig(TestConfigInit.class);
        assertNull(config);

        config = ParameterMap.of("config.init", 1, "key.old", "test").getConfig(TestConfigInit.class);
        assertNotNull(config);
        assertEquals("test", config.value);

        // no exception
        ParameterMap.EMPTY.validateConfig(TestConfigInit.class);

        boolean thrown = false;
        try {
            ParameterMap.of("config.init", 1).validateConfig(TestConfigInit.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            ParameterMap.of("config.init", 1, "key.wrong", "").validateConfig(TestConfigInit.class);
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

    @Test
    public void testSokLong() throws Exception {
        var map = ParameterMap.of("key.old", "1", "key.new", "2");
        var value = map.getSokLong(22, "key.old");
        assertEquals(1, value);
        value = map.getSokLong(22, "key.wrong1", "key.wrong2");
        assertEquals(22, value);
        var thrown = false;
        try {
            map = ParameterMap.of("key.old", "10");
            map.getSokLong(0, true, "key.new", "key.old");
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

}
