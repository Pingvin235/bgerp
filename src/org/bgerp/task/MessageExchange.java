package org.bgerp.task;

import java.util.Collections;
import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.scheduler.Task;
import org.bgerp.util.Log;

import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

@Bean(oldClasses = "ru.bgcrm.worker.MessageExchange")
public class MessageExchange extends Task {
    private static final Log log = Log.getLog();

    private final Set<Integer> types;

    public MessageExchange() {
        super(null);
        types = Collections.emptySet();
    }

    public MessageExchange(ParameterMap config) {
        super(config);
        types = Utils.toIntegerSet(config.get("messageTypeIds"));
    }

    @Override
    public void run() {
        if (!types.isEmpty())
            log.info("Message types: {}", types);

        MessageTypeConfig config = Setup.getSetup().getConfig(MessageTypeConfig.class);
        for (MessageType type : config.getTypeMap().values()) {
            if (!types.isEmpty() && !types.contains(type.getId()))
                continue;

            try {
                type.process();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }
}
