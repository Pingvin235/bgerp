package org.bgerp.dao.message.process;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.bean.Bean;
import org.bgerp.util.Log;

import ru.bgcrm.util.Utils;

public class MessagePossibleProcessConfig extends Config {
    private static final Log log = Log.getLog();

    private final SortedMap<Integer, MessagePossibleProcessSearch> searches;

    protected MessagePossibleProcessConfig(ConfigMap config) throws InitStopException {
        super(null);
        searches = parseSearches(config);
        initWhen(!searches.isEmpty());
    }

    private SortedMap<Integer, MessagePossibleProcessSearch> parseSearches(ConfigMap config) throws InitStopException {
        var result = new TreeMap<Integer, MessagePossibleProcessSearch>();

        for (var me : config.subIndexed("message.possible.process.").entrySet()) {
            var searchConfigMap = me.getValue();

            String className = searchConfigMap.get("class");
            if (Utils.isBlankString(className))
                continue;

            try {
                Class<?> clazz = Bean.getClass(className);
                var search = (MessagePossibleProcessSearch) clazz.getConstructor(int.class, ConfigMap.class)
                    .newInstance(me.getKey(), me.getValue());
                result.put(me.getKey(), search);
            } catch (Exception e) {
                log.error(e);
            }
        }

        if (result.isEmpty())
            result.put(1, new MessagePossibleProcessSearchMessageFrom(1, ConfigMap.EMPTY));

        return Collections.unmodifiableSortedMap(result);
    }

    /**
     * @return search types map with keys equal config IDs.
     */
    public SortedMap<Integer, MessagePossibleProcessSearch> getSearches() {
        return searches;
    }
}
