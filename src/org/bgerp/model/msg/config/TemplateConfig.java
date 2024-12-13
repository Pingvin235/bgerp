package org.bgerp.model.msg.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.model.base.IdTitle;

import ru.bgcrm.util.Utils;

/**
 * Message templates
 *
 * @author Shamil Vakhitov
 */
public class TemplateConfig extends Config {
    private final Map<Integer, Template> templates;

    protected TemplateConfig(ConfigMap config) {
        super(null);
        templates = loadTemplates(config);
    }

    private Map<Integer, Template> loadTemplates(ConfigMap config) {
        Map<Integer, Template> result = new LinkedHashMap<>();

        for (var me : config.subIndexed("message.template.").entrySet()) {
            var pattern = new Template(me.getKey(), me.getValue());
            if (Utils.notBlankStrings(pattern.getTitle(), pattern.subject, pattern.text))
                result.put(pattern.getId(), pattern);
        }

        return Collections.unmodifiableMap(result);
    }

    public Map<Integer, Template> getTemplates() {
        return templates;
    }

    public static final class Template extends IdTitle {
        private final String subject;
        private final String text;

        private Template(int id, ConfigMap config) {
            super(id, config.get("title"));
            subject = config.get("subject");
            text = config.get("text");
        }

        public String getSubject() {
            return subject;
        }

        public String getText() {
            return text;
        }
    }
}
