package org.bgerp.model.process.config;

import org.bgerp.app.cfg.ConfigMap;

import ru.bgcrm.dao.expression.Expression;
import ru.bgcrm.model.BGException;
import ru.bgcrm.util.Utils;

public class LinkProcessCreateConfigItem {
    private final int id;
    private final String title;
    private final int processTypeId;
    private final String linkType;
    private final String expression;
    private final String checkErrorMessage;
    private final String copyParamsMapping;
    private final String copyLinks;

    public LinkProcessCreateConfigItem(int id, ConfigMap setup) throws BGException {
        this.id = id;
        this.title = setup.get("title", "");
        this.processTypeId = setup.getInt("processTypeId", -1);
        this.linkType = setup.get("linkType");
        this.expression = setup.get(Expression.CHECK_EXPRESSION_CONFIG_KEY);
        this.checkErrorMessage = setup.get(Expression.CHECK_ERROR_MESSAGE_CONFIG_KEY);
        this.copyParamsMapping = setup.get("copyParams");
        this.copyLinks = setup.get("copyLinks");

        if (id <= 0 || Utils.isBlankString(title) || processTypeId <= 0 || Utils.isBlankString(linkType)) {
            throw new BGException("Error on load create processCreateLink item");
        }
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