package ru.bgcrm.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.model.config.IsolationConfig;
import org.junit.Test;

import ru.bgcrm.model.BGMessageException;

public class ParameterMapTest {

    @Test
    public void testGetConfig() {
        var config = ParameterMap.of("isolation.process", "executor");
        IsolationConfig isolation = config.getConfig(IsolationConfig.class);
        assertNotNull(isolation);
        assertEquals(IsolationConfig.IsolationProcess.EXECUTOR, isolation.getIsolationProcess());
    }

    private static class TestConfigCache extends Config {
        private static final AtomicInteger constructorCalled = new AtomicInteger();

        protected TestConfigCache(ParameterMap config) {
            super(null);
            constructorCalled.incrementAndGet();
        }
    }

    @Test
    public void testGetConfigCache() {
        var configMap = ParameterMap.EMPTY;

        var config = configMap.getConfig(TestConfigCache.class);
        assertNotNull(config);
        assertEquals(1, TestConfigCache.constructorCalled.get());

        config = configMap.getConfig(TestConfigCache.class);
        assertNotNull(config);
        assertEquals(1, TestConfigCache.constructorCalled.get());

        configMap.removeConfig(TestConfigCache.class);
        config = configMap.getConfig(TestConfigCache.class);
        assertNotNull(config);
        assertEquals(2, TestConfigCache.constructorCalled.get());
    }

    private static class TestConfigValidate extends Config {
        private final String value;

        protected TestConfigValidate(ParameterMap config, boolean validate) throws Exception {
            super(null, validate);
            if ((value = config.getSok(null, validate, "key.new", "key.old")) == null)
                throwValidationException("Validation error");
        }
    }

    @Test
    public void testGetConfigValidate() {
        var configMap = ParameterMap.EMPTY;
        var config = configMap.getConfig(TestConfigValidate.class);
        assertNull(config);

        configMap = ParameterMap.of("key.wrong", "value");
        config = configMap.getConfig(TestConfigValidate.class);
        assertNull(config);

        configMap = ParameterMap.of("key.old", "value");
        config = configMap.getConfig(TestConfigValidate.class);
        assertNotNull(config);
        assertEquals("value", config.value);

        boolean thrown = false;
        try {
            configMap = ParameterMap.of("key.new", "value");
            configMap.validateConfig(TestConfigValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertFalse(thrown);

        try {
            configMap = ParameterMap.of("key.old", "value");
            configMap.validateConfig(TestConfigValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    private static class TestConfigInitWhenAndValidate extends Config {
        private final String value;

        protected TestConfigInitWhenAndValidate(ParameterMap config, boolean validate) throws Exception {
            super(null, validate);
            initWhen(config.getBoolean("config.init", false));
            if ((value = config.getSok(null, validate, "key.new", "key.old")) == null)
                throwValidationException("Validation error");
        }
    }

    @Test
    public void testGetConfigInitWhenAndValidate() throws Exception {
        var config = ParameterMap.EMPTY.getConfig(TestConfigInitWhenAndValidate.class);
        assertNull(config);

        config = ParameterMap.of("config.init", 1, "key.old", "test").getConfig(TestConfigInitWhenAndValidate.class);
        assertNotNull(config);
        assertEquals("test", config.value);

        // no exception
        ParameterMap.EMPTY.validateConfig(TestConfigInitWhenAndValidate.class);

        boolean thrown = false;
        try {
            ParameterMap.of("config.init", 1).validateConfig(TestConfigInitWhenAndValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            ParameterMap.of("config.init", 1, "key.wrong", "").validateConfig(TestConfigInitWhenAndValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    private static class TestConfigInitWhen extends Config {
        private final String value;

        protected TestConfigInitWhen(ParameterMap config) throws InitStopException {
            super(null);
            value = config.get("key");
            initWhen(StringUtils.isNoneBlank(value));
        }
    }

    @Test
    public void testGetConfigInitWhen() {
        var configMap = ParameterMap.of("key.wrong", "value");
        var config = configMap.getConfig(TestConfigInitWhen.class);
        assertNull(config);

        configMap = ParameterMap.of("key", "value");
        config = configMap.getConfig(TestConfigInitWhen.class);
        assertNotNull(config);
        assertEquals("value", config.value);
    }

    private static class TestConfigWithException extends Config {
        protected TestConfigWithException(ParameterMap config) {
            super(null);
            if (Utils.isEmptyString(config.get("correct.key")))
                throw new ClassCastException("Some exception during a config creation");
        }
    }

    @Test
    public void testGetConfigWithException() {
        var config = ParameterMap.of().getConfig(TestConfigWithException.class);
        assertNull(config);
    }

    @Test
    public void testOf() {
        ParameterMap map = ParameterMap.of("key1", "1", "key2", "value2", "key3");
        assertEquals(2, map.size());
        assertEquals(1, map.getInt("key1"));
        assertEquals("value2", map.get("key2"));
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

    @Test
    public void tesGetSok() throws Exception {
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
    public void testGetSokLong() throws Exception {
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

    @Test
    public void testGetSokBoolean() throws Exception {
        var map = ParameterMap.of("key.old", "1", "key.new", "2");
        var value = map.getSokBoolean(false, "key.old");
        assertEquals(true, value);
        value = map.getSokBoolean(true, "key.wrong1", "key.wrong2");
        assertEquals(true, value);
        var thrown = false;
        try {
            map = ParameterMap.of("key.old", "0");
            map.getSokBoolean(false, true, "key.new", "key.old");
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }
}
