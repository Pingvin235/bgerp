package org.bgerp.action;

import org.apache.struts.actions.BaseAction;
import org.bgerp.l10n.Localizer;
import org.bgerp.l10n.Titled;

import ru.bgcrm.servlet.ActionServlet.Action;

/**
 * Action with localized title.
 *
 * @author Shamil Vakhitov
 */
public class TitledAction implements Titled {
    private final Class<?> actionClass;
    private final Titled titled;
    private final String href;

    public TitledAction(BaseAction action, Titled titled, String href) {
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
    public String getTitle(Localizer l) {
        return titled.getTitle(l);
    }

    /**
     * href, used for user interfaces, passed to <ui:menu-item> tag.
     * @return
     */
    public String getHref() {
        return href;
    }
}