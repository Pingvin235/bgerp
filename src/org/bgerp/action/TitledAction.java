package org.bgerp.action;

import org.apache.struts.actions.BaseAction;

import ru.bgcrm.model.Title;
import ru.bgcrm.servlet.ActionServlet.Action;

/**
 * Action with localized title.
 *
 * @author Shamil Vakhitov
 */
public class TitledAction implements Title {
    private final Class<?> actionClass;
    private final Title titled;
    private final String href;

    public TitledAction(BaseAction action, Title titled, String href) {
        this.actionClass = action.getClass();
        this.titled = titled;
        this.href = href;
    }

    /**
     * Action class and method, separated by semicolon.
     * 'null' - for unspecified method. The same format, as used in action.xml files.
     * @return
     */
    public String getAction() {
        return actionClass.getName() + ":null";
    }

    /**
     * Path, ending with .do.
     * @return
     */
    public String getActionUrl() {
        var a = actionClass.getDeclaredAnnotation(Action.class);
        if (a == null)
            return null;
        return a.path() + ".do";
    }

    @Override
    public String getTitle() {
        return titled.getTitle();
    }

    /**
     * href, used for user interfaces, passed to <ui:menu-item> tag.
     * @return
     */
    public String getHref() {
        return href;
    }
}