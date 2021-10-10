package ru.bgcrm.worker;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bgerp.util.Log;

import ru.bgcrm.Scheduler;
import ru.bgcrm.Scheduler.ConfigurableTask;
import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.dao.message.config.MessageTypeConfig;
import ru.bgcrm.util.ParameterMap;
import ru.bgcrm.util.Setup;
import ru.bgcrm.util.Utils;

public class MessageExchange extends ConfigurableTask {
    private static final AtomicBoolean run = new AtomicBoolean(false);

    private static final Log log = Log.getLog();

    private final Set<Integer> types;

    // пустой конструктор для запуска извне без конфигурации
    public MessageExchange() {
        super(null);
        types = Collections.emptySet();
    }

    public MessageExchange(ParameterMap config) {
        super(config);
        types = Utils.toIntegerSet(config.get("messageTypeIds"));
    }

    private MessageExchange(Set<Integer> types) {
        super(null);
        this.types = types;
    }

    @Override
    public void run() {
        if (run.get()) {
            log.info("Task already working..");
            return;
        }

        long time = System.currentTimeMillis();

        synchronized (run) {
            run.set(true);

            if (!types.isEmpty())
                log.info("Message types: " + types);

            try {
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
            } finally {
                run.set(false);

                Scheduler.logExecutingTime( this, time );
            }
        }
    }

    public static void main(String[] args) {
        new MessageExchange(Utils.toIntegerSet(args[0])).run();
    }
}
