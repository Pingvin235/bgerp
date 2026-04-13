package org.bgerp.model.process.queue.filter;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.process.QueueSelectParams;
import ru.bgcrm.dao.process.Tables;
import ru.bgcrm.struts.form.DynActionForm;
import ru.bgcrm.util.Utils;

public class FilterGroups extends Filter {
    public FilterGroups(int id, ConfigMap config) {
        super(id, config);
    }

    @Override
    public void apply(DynActionForm form, QueueSelectParams params) {
        String groupIds = getValues(form, "group");

        if (Utils.notBlankString(groupIds)) {
            StringBuilder joinPart = params.joinPart;
            joinPart.append(SQL_INNER_JOIN)
                .append(Tables.TABLE_PROCESS_GROUP)
                .append("AS ig ON process.id=ig.process_id AND ig.group_id IN(")
                .append(groupIds)
                .append(")");
        }
    }
}
