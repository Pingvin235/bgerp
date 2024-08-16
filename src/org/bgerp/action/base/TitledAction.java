package org.bgerp.action.base;

import org.bgerp.model.base.iface.Title;

/**
 * Action with localized title.
 *
 * @author Shamil Vakhitov
 */
public class TitledAction implements Title {
    private final Class<? extends BaseAction> actionClass;
    private final Title titled;
    private final String href;

    public TitledAction(BaseAction action, Title titled, String href) {
        this.actionClass = action.getClass();
        this.titled = titled;
        this.href = href;
    }

    @Override
    public String getTitle() {
        return titled.getTitle();
    }

    /**
     * Action class and method, separated by semicolon.
     * 'null' - for unspecified method. The same format, as used in action.xml files.
     * @return
     */
    public String getAction() {
        return Actions.getByClass(actionClass).getId() + ":null";
    }

    /**
     * href, used for user interfaces, passed to <ui:menu-item> tag.
     * @return
     */
    public String getHref() {
        return href;
    }
}