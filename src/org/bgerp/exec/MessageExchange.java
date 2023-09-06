package org.bgerp.exec;

import java.util.Set;

import org.bgerp.app.bean.annotation.Bean;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.cfg.Setup;
import org.bgerp.app.exec.scheduler.Task;
import org.bgerp.app.l10n.Localizer;
import org.bgerp.plugin.kernel.Plugin;
import org.bgerp.util.Log;

import ru.bgcrm.dao.message.MessageType;
import ru.bgcrm.model.message.config.MessageTypeConfig;
import ru.bgcrm.util.Utils;

@Bean(oldClasses = "ru.bgcrm.worker.MessageExchange")
public class MessageExchange extends Task {
    private static final Log log = Log.getLog();

    private final Set<Integer> types;

    public MessageExchange(ConfigMap config) {
        super(null);
        types = Utils.toIntegerSet(config.get("messageTypeIds"));
    }

    @Override
    public String getTitle() {
        Localizer l = Plugin.INSTANCE.getLocalizer();
        return types.isEmpty() ?
            l.l("Kernel Message Exchange") :
            l.l("Kernel Message Exchange for types: {}", types);
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
