package ru.bgcrm.model;

import org.bgerp.l10n.Localizer;
import org.bgerp.l10n.Titled;

/**
 * Item with localized title.
 *
 * @author Shamil Vakhitov
 */
public class IdTitled extends Id implements Titled {
    private final String ltitle;

    public IdTitled(int id, String ltitle) {
        this.id = id;
        this.ltitle = ltitle;
    }

    @Override
    public String getTitle(Localizer l) {
        return l.l(ltitle);
    }
}
