package org.bgerp.plugin.msg.sms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bgerp.app.cfg.ConfigMap;

public class Config extends org.bgerp.app.cfg.Config {
    /** Senders map, 0 - key for a default Sender. */
    private final Map<Integer, Sender> senders;

    protected Config(ConfigMap config) {
        super(null);
        config = config.sub(Plugin.ID + ":");
        this.senders = loadSenders(config);
    }

    private Map<Integer, Sender> loadSenders(ConfigMap config) {
        var result = new HashMap<Integer, Sender>();

        var defaultSender = Sender.of(config);
        if (defaultSender != null)
            result.put(0, defaultSender);

        for (var me : config.subIndexed("").entrySet()) {
            var sender = Sender.of(me.getValue());
            if (sender != null)
                result.put(me.getKey(), sender);
        }

        return Collections.unmodifiableMap(result);
    }

    Map<Integer, Sender> getSenders() {
        return senders;
    }
}
