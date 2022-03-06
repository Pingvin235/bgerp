package org.bgerp.plugin.calendar.model;

import ru.bgcrm.model.IdTitle;
import ru.bgcrm.util.ParameterMap;

/**
 * Time type, e.g.: work, holyday, weekend.
 *
 * @author Shamil Vakhitov
 */
public class TimeType extends IdTitle {
    private final Decoration decoration;

    public TimeType(int id, ParameterMap config) {
        super(id, config.get("title", "???"));
        this.decoration = new Decoration(config.get("decoration"));
    }

    /** UI decoration: different font color, borders, popups and so on. */
    public static final class Decoration {
        private Decoration(String decoration) {

        }
    }
}
