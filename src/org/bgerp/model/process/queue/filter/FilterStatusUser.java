package org.bgerp.model.process.queue.filter;

import org.apache.commons.lang3.StringUtils;
import org.bgerp.app.cfg.ConfigMap;
import org.bgerp.app.exception.BGException;
import org.bgerp.cache.ProcessTypeCache;

import ru.bgcrm.model.process.Status;
import ru.bgcrm.util.Utils;

public class FilterStatusUser extends Filter {
    private String statusTitle;
    private int statusId;

    public FilterStatusUser(int id, ConfigMap filter, String type) throws BGException {
        super(id, filter);

        int statusId = Utils.parseInt(StringUtils.substringAfter(type, ":"));
        Status status = ProcessTypeCache.getStatusMap().get(statusId);

        if (status != null) {
            this.statusTitle = status.getTitle();
            this.statusId = status.getId();
        } else {
            throw new BGException("Incorrect status: " + statusId);
        }
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public int getStatusId() {
        return statusId;
    }
}
