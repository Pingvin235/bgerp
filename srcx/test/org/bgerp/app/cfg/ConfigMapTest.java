package org.bgerp.app.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.exception.BGMessageException;
import org.bgerp.model.process.config.IsolationConfig;
import org.junit.Assert;
import org.junit.Test;

import ru.bgcrm.util.Utils;

public class ConfigMapTest {

    @Test
    public void testGetConfig() {
        var config = SimpleConfigMap.of("isolation.process", "executor");
        IsolationConfig isolation = config.getConfig(IsolationConfig.class);
        assertNotNull(isolation);
        assertEquals(IsolationConfig.IsolationProcess.Type.EXECUTOR, isolation.getIsolationProcess().getType());
    }

    private static class TestConfigCache extends Config {
        private static final AtomicInteger constructorCalled = new AtomicInteger();

        protected TestConfigCache(ConfigMap config) {
            super(null);
            constructorCalled.incrementAndGet();
        }
    }

    @Test
    public void testGetConfigCache() {
        var configMap = ConfigMap.EMPTY;

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

        protected TestConfigValidate(ConfigMap config, boolean validate) throws Exception {
            super(null, validate);
            if ((value = config.getSok(null, validate, "key.new", "key.old")) == null)
                throwValidationException("Validation error");
        }
    }

    @Test
    public void testGetConfigValidate() {
        var configMap = ConfigMap.EMPTY;
        var config = configMap.getConfig(TestConfigValidate.class);
        assertNull(config);

        configMap = SimpleConfigMap.of("key.wrong", "value");
        config = configMap.getConfig(TestConfigValidate.class);
        assertNull(config);

        configMap = SimpleConfigMap.of("key.old", "value");
        config = configMap.getConfig(TestConfigValidate.class);
        assertNotNull(config);
        assertEquals("value", config.value);

        boolean thrown = false;
        try {
            configMap = SimpleConfigMap.of("key.new", "value");
            configMap.validateConfig(TestConfigValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertFalse(thrown);

        try {
            configMap = SimpleConfigMap.of("key.old", "value");
            configMap.validateConfig(TestConfigValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    private static class TestConfigInitWhenAndValidate extends Config {
        private final String value;

        protected TestConfigInitWhenAndValidate(ConfigMap config, boolean validate) throws Exception {
            super(null, validate);
            initWhen(config.getBoolean("config.init", false));
            if ((value = config.getSok(null, validate, "key.new", "key.old")) == null)
                throwValidationException("Validation error");
        }
    }

    @Test
    public void testGetConfigInitWhenAndValidate() throws Exception {
        var config = ConfigMap.EMPTY.getConfig(TestConfigInitWhenAndValidate.class);
        assertNull(config);

        config = SimpleConfigMap.of("config.init", 1, "key.old", "test").getConfig(TestConfigInitWhenAndValidate.class);
        assertNotNull(config);
        assertEquals("test", config.value);

        // no exception
        ConfigMap.EMPTY.validateConfig(TestConfigInitWhenAndValidate.class);

        boolean thrown = false;
        try {
            SimpleConfigMap.of("config.init", 1).validateConfig(TestConfigInitWhenAndValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);

        thrown = false;
        try {
            SimpleConfigMap.of("config.init", 1, "key.wrong", "").validateConfig(TestConfigInitWhenAndValidate.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    private static class TestConfigInitWhen extends Config {
        private final String value;

        protected TestConfigInitWhen(ConfigMap config) throws InitStopException {
            super(null);
            value = config.get("key");
            initWhen(StringUtils.isNoneBlank(value));
        }
    }

    @Test
    public void testGetConfigInitWhen() {
        var configMap = SimpleConfigMap.of("key.wrong", "value");
        var config = configMap.getConfig(TestConfigInitWhen.class);
        assertNull(config);

        configMap = SimpleConfigMap.of("key", "value");
        config = configMap.getConfig(TestConfigInitWhen.class);
        assertNotNull(config);
        assertEquals("value", config.value);
    }

    private static class TestConfigWithException extends Config {
        protected TestConfigWithException(ConfigMap config) {
            super(null);
            if (Utils.isEmptyString(config.get("correct.key")))
                throw new ClassCastException("Some exception during a config creation");
        }
    }

    @Test
    public void testGetConfigWithException() {
        var config = SimpleConfigMap.of().getConfig(TestConfigWithException.class);
        assertNull(config);
    }

    @Test
    public void testOf() {
        ConfigMap map = SimpleConfigMap.of("key1", "1", "key2", "value2", "key3");
        assertEquals(2, map.size());
        assertEquals(1, map.getInt("key1"));
        assertEquals("value2", map.get("key2"));
    }

    @Test
    public void testValidateConfig() {
        boolean thrown = false;
        try {
            var config = SimpleConfigMap.of("isolation.process", "wrong");
            config.validateConfig(IsolationConfig.class);
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void tesGetSok() throws Exception {
        var map = SimpleConfigMap.of("key.old", "1", "key.new", "2");
        var value = map.getSok("key.new", "key.old");
        assertEquals("2", value);

        map = SimpleConfigMap.of("key.old", "1");
        value = map.getSok("key.new", "key.old");
        assertEquals("1", value);

        value = map.getSok("key.wrong1", "key.wrong2");
        assertNull(value);

        value = map.getSok("default", false, "key.wrong");
        assertEquals("default", value);

        var thrown = false;
        try {
            map = SimpleConfigMap.of("key.old", "0");
            map.getSok(null, true, "key.new", "key.old");
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testGetSokLong() throws Exception {
        var map = SimpleConfigMap.of("key.old", "1", "key.new", "2");
        var value = map.getSokLong(22, "key.old");
        assertEquals(1, value);
        value = map.getSokLong(22, "key.wrong1", "key.wrong2");
        assertEquals(22, value);
        var thrown = false;
        try {
            map = SimpleConfigMap.of("key.old", "10");
            map.getSokLong(0, true, "key.new", "key.old");
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testGetSokBoolean() throws Exception {
        var map = SimpleConfigMap.of("key.old", "1", "key.new", "2");
        var value = map.getSokBoolean(false, "key.old");
        assertEquals(true, value);
        value = map.getSokBoolean(true, "key.wrong1", "key.wrong2");
        assertEquals(true, value);
        var thrown = false;
        try {
            map = SimpleConfigMap.of("key.old", "0");
            map.getSokBoolean(false, true, "key.new", "key.old");
        } catch (BGMessageException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testSub() {
        var map = SimpleConfigMap.of("preFixkey1", "value1", "preFixkey2", "value2");
        Assert.assertEquals(Map.of("key1", "value1", "key2", "value2"), map.sub("preFix"));
    }

    @Test
    public void testSubSok() {
        var map = SimpleConfigMap.of("prefix.new.key2", "value2", "prefix.old.key1", "value1");
        Assert.assertEquals(Map.of("key1", "value1", "key2", "value2"), map.subSok("prefix.new.", "prefix.old."));
    }

    @Test
    public void testSubIndexed() {
        var map = SimpleConfigMap.of("1.key", "1", "1.key2", "2", "key3", "3");
        var subIndexed = map.subIndexed("");

        Assert.assertEquals(1, subIndexed.size());
        Assert.assertEquals(Map.of("key", "1", "key2", "2"), subIndexed.get(1));

        map = SimpleConfigMap.of("prefix.1.key1", "11", "prefix.2.key2", "22", "prefix.1.key2", "12", "prefix.new.2.key1", "21", "blabla", "value");
        subIndexed = map.subSokIndexed("prefix.new.", "prefix.");
        var subIndexedIt = subIndexed.values().iterator();

        Assert.assertEquals(2, subIndexed.size());

        Assert.assertEquals(Map.of("key1", "11", "key2", "12"), subIndexedIt.next());
        Assert.assertEquals(Map.of("key1", "21", "key2", "22"), subIndexedIt.next());
        Assert.assertNull(subIndexed.get(3));
    }

    @Test
    public void testSubKeyed() {
        var map = SimpleConfigMap.of("prefix.1.key", "aa", "prefix.3.key2", "bb", "prefix.key3.key4", "dd", "prefix.keyNoDot", "value");
        var subKeyed = map.subKeyed("prefix.");

        Assert.assertEquals(4, subKeyed.size());

        Assert.assertEquals(Map.of("key", "aa"), subKeyed.get("1"));
        Assert.assertEquals(Map.of("key2", "bb"), subKeyed.get("3"));
        Assert.assertEquals(Map.of("key4", "dd"), subKeyed.get("key3"));
        Assert.assertEquals(Map.of("", "value"), subKeyed.get("keyNoDot"));
    }
}
