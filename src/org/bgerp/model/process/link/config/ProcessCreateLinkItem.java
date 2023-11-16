package org.bgerp.model.process.link.config;

import org.bgerp.app.cfg.Config;
import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.process.Process;

public class ProcessCreateLinkItem extends Config {
    private final int id;
    private final String title;
    private final int processTypeId;
    private final String linkType;
    private final String expression;
    private final String checkErrorMessage;
    private final String copyParamsMapping;
    private final String copyLinks;

    ProcessCreateLinkItem(int id, ConfigMap config) throws InitStopException {
        super(null);
        this.id = id;
        this.title = config.get("title", "??? " + id);
        this.processTypeId = config.getInt("processTypeId", -1);
        this.linkType = config.get("linkType", Process.LINK_TYPE_LINK);
        this.expression = config.get(Expression.CHECK_EXPRESSION_CONFIG_KEY);
        this.checkErrorMessage = config.get(Expression.CHECK_ERROR_MESSAGE_CONFIG_KEY);
        this.copyParamsMapping = config.get("copyParams");
        this.copyLinks = config.get("copyLinks");
        initWhen(processTypeId > 0);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getProcessTypeId() {
        return processTypeId;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getExpression() {
        return expression;
    }

    public String getCheckErrorMessage() {
        return checkErrorMessage;
    }

    public String getCopyParamsMapping() {
        return copyParamsMapping;
    }

    public String getCopyLinks() {
        return copyLinks;
    }
}