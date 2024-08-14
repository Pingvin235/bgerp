package ru.bgcrm.model.process.config;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.process.Process;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;
import ru.bgcrm.util.sql.SingleConnectionSet;

public class ProcessReferenceConfig extends Config {
    private List<ProcessReferenceConfigItem> itemList = new ArrayList<>();

    public ProcessReferenceConfig(ConfigMap config) {
        super(null);

        for (Map.Entry<Integer, ConfigMap> me : config.subIndexed("processReference.").entrySet()) {
            try {
                itemList.add(new ProcessReferenceConfigItem(me.getValue()));
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    public String getReference(Connection con, DynActionForm form, Process process, String... objectTypes) {
        String result = "";

        MAIN_LOOP: for (ProcessReferenceConfigItem item : itemList) {
            for (String objectType : objectTypes) {
                if (item.objectTypes.contains(objectType)) {
                    Map<String, Object> context = Expression.context(new SingleConnectionSet(con), form, null, process);
                    result = new Expression(context).getString(item.macros);
                    break MAIN_LOOP;
                }
            }
        }

        return result;
    }

    private static class ProcessReferenceConfigItem {
        private Set<String> objectTypes = Collections.emptySet();
        private String macros;

        public ProcessReferenceConfigItem(ConfigMap setup) {
            this.objectTypes = Utils.toSet(setup.get("objectTypes"));
            this.macros = setup.get("stringExpression");

            if (Utils.isBlankString(macros)) {
                throw new BGException("Incorrect config item, not macros.");
            }
        }
    }
}
