package org.bgerp.model.process.link.config;

import java.util.Collections;
import java.util.Set;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.model.process.Process;
import ru.bgcrm.util.Utils;

public class ProcessLinkCategory extends Config{
    private final int id;
    private final String title;
    private final boolean link;
    private final String linkType;
    private final Set<Integer> processTypeIds;
    private final boolean add;
    private final Set<Integer> addProcessStatusIds;

    ProcessLinkCategory(int id, ConfigMap config) throws InitStopException {
        super(null);
        this.id = id;
        this.title = config.get("title", "??? " + id);
        this.link = config.getBoolean("link", true);
        this.linkType = config.get("link.type", Process.LINK_TYPE_LINK);
        initWhen(linkType.equals(Process.LINK_TYPE_DEPEND) || linkType.equals(Process.LINK_TYPE_LINK) || linkType.equals(Process.LINK_TYPE_MADE));
        this.processTypeIds = Collections.unmodifiableSet(Utils.toIntegerSet(config.get("process.types")));
        this.add = config.getBoolean("add", true);
        this.addProcessStatusIds = !add ? null : Collections.unmodifiableSet(Utils.toIntegerSet(config.get(("add.process.statuses"))));
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isLink() {
        return link;
    }

    public String getLinkType() {
        return linkType;
    }

    public Set<Integer> getProcessTypeIds() {
        return processTypeIds;
    }

    public boolean isAdd() {
        return add;
    }

    public Set<Integer> getAddProcessStatusIds() {
        return addProcessStatusIds;
    }
}
