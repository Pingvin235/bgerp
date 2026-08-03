package org.bgerp.plugin.pln.kanban.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bgerp.app.cfg.Preferences;
import org.junit.Test;

public class KanbanConfigTest {
    @Test
    public void testPreviewEnabledByDefault() {
        var config = new Config(new Preferences());

        assertTrue(config.isPreviewEnabled());
    }

    @Test
    public void testPreviewDisabled() {
        var config = new Config(new Preferences("kanban:preview.enable=0\n"));

        assertFalse(config.isPreviewEnabled());
    }

    @Test
    public void testConfiguredColorUsed() {
        var config = new Config(new Preferences("kanban:status.5.color=#111111\n"));

        assertEquals("#111111", config.getColor(1, 5, 0));
    }

    @Test
    public void testDefaultPaletteFallbackByPosition() {
        var config = new Config(new Preferences());

        assertEquals("#e0e6ed", config.getColor(1, 99, 0));
        assertEquals("#a4d4ff", config.getColor(1, 99, 1));
    }

    @Test
    public void testPaletteWrapsAround() {
        var config = new Config(new Preferences());

        assertEquals(config.getColor(1, 99, 0), config.getColor(1, 99, 6));
    }

    @Test
    public void testConfiguredPaletteUsed() {
        var config = new Config(new Preferences("kanban:palette.0.color=#222222\n"));

        assertEquals("#222222", config.getColor(1, 99, 0));
    }

    @Test
    public void testQueueStatusColorOverridesGlobal() {
        var config = new Config(new Preferences(
            "kanban:status.5.color=#111111\n" +
            "kanban:queue.7.status.5.color=#333333\n"
        ));

        assertEquals("#333333", config.getColor(7, 5, 0));
        assertEquals("#111111", config.getColor(1, 5, 0));
    }

    @Test
    public void testQueuePaletteOverridesGlobalByPosition() {
        var config = new Config(new Preferences(
            "kanban:queue.7.palette.1.color=#444444\n"
        ));

        // overridden position for the configured queue
        assertEquals("#444444", config.getColor(7, 99, 1));
        // other positions of the same queue still fall back to the default palette
        assertEquals("#e0e6ed", config.getColor(7, 99, 0));
        // other queues are unaffected
        assertEquals("#a4d4ff", config.getColor(1, 99, 1));
    }
}
